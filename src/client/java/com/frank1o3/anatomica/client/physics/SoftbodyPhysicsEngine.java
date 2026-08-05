package com.frank1o3.anatomica.client.physics;

import com.frank1o3.anatomica.config.BodyConfig;
import com.frank1o3.anatomica.physics.IPhysicsEngine;
import com.frank1o3.anatomica.physics.LivingEntityLike;
import com.frank1o3.franklylib.Vec3;

/**
 * Position-Based-Dynamics soft-body simulation over the fixed grid defined by
 * {@link SoftbodyGridLayout}: predict node positions from velocity + external forces,
 * relax a handful of distance constraints toward their rest length, derive velocity
 * from the resulting position delta, then repeat next tick.
 *
 * <p>
 * Hot per-tick state is kept in flat {@code float[]} arrays rather than {@code Vec3[]}
 * to avoid churn/allocation every tick; {@link Vec3} is only constructed at the public
 * interface boundary (getters) where callers actually need it.
 *
 * <p>
 * This class only implements {@link IPhysicsEngine}'s node-data contract — it has no
 * legacy scalar getters and no knowledge of what mesh (if any) is skinned from it.
 */
public final class SoftbodyPhysicsEngine implements IPhysicsEngine {

    private static final int SOLVER_ITERATIONS = 5;
    private static final float GRAVITY_SETTLE = 0.0f; // cosmetic sim: no gravity, springs alone re-center it
    private static final float MAX_IMPULSE_MAGNITUDE = 0.35f;

    private final SoftbodyGridLayout.Layout layout;
    private final int[][] constraintPairs;
    private final float[] restLengths;

    private final float[] posX, posY, posZ;
    private final float[] prevPosX, prevPosY, prevPosZ;
    private final float[] velX, velY, velZ;
    private final float[] interpX, interpY, interpZ;

    private Vec3 lastMotionDelta = Vec3.ZERO;

    public SoftbodyPhysicsEngine() {
        this.layout = SoftbodyGridLayout.build();
        int n = layout.restPositions().length;

        this.constraintPairs = layout.constraintPairs().toArray(new int[0][]);
        this.restLengths = new float[constraintPairs.length];
        for (int i = 0; i < constraintPairs.length; i++) {
            Vec3 a = layout.restPositions()[constraintPairs[i][0]];
            Vec3 b = layout.restPositions()[constraintPairs[i][1]];
            Vec3 delta = a.subtract(b);
            restLengths[i] = (float) Math.sqrt(delta.dot(delta));
        }

        posX = new float[n];
        posY = new float[n];
        posZ = new float[n];
        prevPosX = new float[n];
        prevPosY = new float[n];
        prevPosZ = new float[n];
        velX = new float[n];
        velY = new float[n];
        velZ = new float[n];
        interpX = new float[n];
        interpY = new float[n];
        interpZ = new float[n];

        reset();
    }

    @Override
    public void reset() {
        for (int i = 0; i < posX.length; i++) {
            Vec3 rest = layout.restPositions()[i];
            posX[i] = prevPosX[i] = interpX[i] = rest.x();
            posY[i] = prevPosY[i] = interpY[i] = rest.y();
            posZ[i] = prevPosZ[i] = interpZ[i] = rest.z();
            velX[i] = velY[i] = velZ[i] = 0f;
        }
        lastMotionDelta = Vec3.ZERO;
    }

    @Override
    public void tick(float deltaTime, LivingEntityLike entity, BodyConfig config) {
        if (!config.physicsEnabled()) {
            // Physics disabled: snap straight back to rest so the mesh reads as static,
            // rather than continuing to simulate an engine nothing is reading from.
            reset();
            return;
        }

        int n = posX.length;
        for (int i = 0; i < n; i++) {
            prevPosX[i] = posX[i];
            prevPosY[i] = posY[i];
            prevPosZ[i] = posZ[i];
        }

        applyDrivingForces(entity, config, deltaTime);
        predictPositions(deltaTime, config);
        solveConstraints(config);
        pinFixedNodes();
        deriveVelocities(deltaTime);

        lastMotionDelta = entity.motionDelta();
    }

    private void applyDrivingForces(LivingEntityLike entity, BodyConfig config, float deltaTime) {
        Vec3 motion = entity.motionDelta();
        Vec3 motionChange = motion.subtract(lastMotionDelta);

        float bounce = config.bounceStrength();
        float walkPhase = entity.walkAnimationPhase();
        float swing = entity.attackSwingProgress(1.0f);

        // Base impulse from sudden motion changes (landing, direction change, jumping).
        float impulseX = -motionChange.x() * bounce * 6.0f;
        float impulseY = -motionChange.y() * bounce * 6.0f;
        float impulseZ = -motionChange.z() * bounce * 6.0f;

        // Walk-cycle oscillation adds a small periodic vertical/forward bob.
        float walkBob = (float) Math.sin(walkPhase * Math.PI * 2.0) * bounce * 0.02f;
        impulseY += walkBob;

        // Arm-swing adds a small forward nudge.
        impulseZ += swing * bounce * 0.03f;

        if (entity.isSwimming() || entity.isFallFlying()) {
            impulseY *= 0.4f; // less pronounced while horizontal-ish
        }
        if (entity.isPassenger()) {
            impulseX *= 0.5f;
            impulseZ *= 0.5f;
        }

        impulseX = clampMagnitude(impulseX, MAX_IMPULSE_MAGNITUDE);
        impulseY = clampMagnitude(impulseY, MAX_IMPULSE_MAGNITUDE);
        impulseZ = clampMagnitude(impulseZ, MAX_IMPULSE_MAGNITUDE);

        for (int i = 0; i < velX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            velX[i] += impulseX;
            velY[i] += impulseY;
            velZ[i] += impulseZ;
        }
    }

    private void predictPositions(float deltaTime, BodyConfig config) {
        // "softness" trades off how loosely the springs behave: higher softness ->
        // less damping per tick -> more visible jiggle that takes longer to settle.
        float damping = 1.0f - (0.25f + config.softness() * 0.35f);
        damping = Math.max(0.0f, Math.min(1.0f, damping));

        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            velX[i] *= damping;
            velY[i] *= damping;
            velZ[i] *= damping;

            velY[i] += GRAVITY_SETTLE * deltaTime;

            posX[i] += velX[i] * deltaTime;
            posY[i] += velY[i] * deltaTime;
            posZ[i] += velZ[i] * deltaTime;
        }
    }

    private void solveConstraints(BodyConfig config) {
        // Higher softness = looser springs = fewer effective iterations of correction
        // applied per constraint (compliance), simulated here by scaling the correction
        // factor rather than varying iteration count.
        float compliance = 0.35f + config.softness() * 0.5f; // (0.35 stiff .. 0.85 loose)

        for (int iter = 0; iter < SOLVER_ITERATIONS; iter++) {
            for (int c = 0; c < constraintPairs.length; c++) {
                int a = constraintPairs[c][0];
                int b = constraintPairs[c][1];
                float restLen = restLengths[c];

                float dx = posX[b] - posX[a];
                float dy = posY[b] - posY[a];
                float dz = posZ[b] - posZ[a];
                float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist < 1.0e-6f) {
                    continue;
                }

                float diff = (dist - restLen) / dist;
                boolean aFixed = layout.fixed()[a];
                boolean bFixed = layout.fixed()[b];

                float correctionScale = compliance / SOLVER_ITERATIONS;

                if (!aFixed && !bFixed) {
                    float half = 0.5f * diff * correctionScale;
                    posX[a] += dx * half;
                    posY[a] += dy * half;
                    posZ[a] += dz * half;
                    posX[b] -= dx * half;
                    posY[b] -= dy * half;
                    posZ[b] -= dz * half;
                } else if (!aFixed) {
                    posX[a] += dx * diff * correctionScale;
                    posY[a] += dy * diff * correctionScale;
                    posZ[a] += dz * diff * correctionScale;
                } else if (!bFixed) {
                    posX[b] -= dx * diff * correctionScale;
                    posY[b] -= dy * diff * correctionScale;
                    posZ[b] -= dz * diff * correctionScale;
                }
            }
        }
    }

    private void pinFixedNodes() {
        for (int i = 0; i < posX.length; i++) {
            if (!layout.fixed()[i]) {
                continue;
            }
            Vec3 rest = layout.restPositions()[i];
            posX[i] = rest.x();
            posY[i] = rest.y();
            posZ[i] = rest.z();
            velX[i] = velY[i] = velZ[i] = 0f;
        }
    }

    private void deriveVelocities(float deltaTime) {
        if (deltaTime <= 0f) {
            return;
        }
        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            velX[i] = (posX[i] - prevPosX[i]) / deltaTime;
            velY[i] = (posY[i] - prevPosY[i]) / deltaTime;
            velZ[i] = (posZ[i] - prevPosZ[i]) / deltaTime;
        }
    }

    private static float clampMagnitude(float value, float max) {
        if (value > max) {
            return max;
        }
        if (value < -max) {
            return -max;
        }
        return value;
    }

    // -------------------------------------------------------------------
    // IPhysicsEngine
    // -------------------------------------------------------------------

    @Override
    public int nodeCount() {
        return posX.length;
    }

    @Override
    public Vec3 nodeRestPosition(int index) {
        return layout.restPositions()[index];
    }

    @Override
    public Vec3 nodePosition(int index) {
        return new Vec3(posX[index], posY[index], posZ[index]);
    }

    @Override
    public Vec3 nodeVelocity(int index) {
        return new Vec3(velX[index], velY[index], velZ[index]);
    }

    @Override
    public boolean isNodeFixed(int index) {
        return layout.fixed()[index];
    }

    @Override
    public void interpolate(float partialTick) {
        for (int i = 0; i < posX.length; i++) {
            interpX[i] = lerp(prevPosX[i], posX[i], partialTick);
            interpY[i] = lerp(prevPosY[i], posY[i], partialTick);
            interpZ[i] = lerp(prevPosZ[i], posZ[i], partialTick);
        }
    }

    @Override
    public Vec3 interpolatedNodePosition(int index) {
        return new Vec3(interpX[index], interpY[index], interpZ[index]);
    }

    @Override
    public void applyImpulse(Vec3 localPoint, Vec3 force) {
        int nearest = nearestNode(localPoint);
        if (layout.fixed()[nearest]) {
            return;
        }
        velX[nearest] += force.x();
        velY[nearest] += force.y();
        velZ[nearest] += force.z();
    }

    private int nearestNode(Vec3 point) {
        int best = 0;
        float bestDist = Float.MAX_VALUE;
        for (int i = 0; i < posX.length; i++) {
            float dx = posX[i] - point.x();
            float dy = posY[i] - point.y();
            float dz = posZ[i] - point.z();
            float dist = dx * dx + dy * dy + dz * dz;
            if (dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        return best;
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }
}
