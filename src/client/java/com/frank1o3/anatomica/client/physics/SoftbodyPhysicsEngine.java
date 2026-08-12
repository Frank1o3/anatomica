package com.frank1o3.anatomica.client.physics;

import com.frank1o3.anatomica.config.IBodyConfig;
import com.frank1o3.anatomica.physics.IPhysicsEngine;
import com.frank1o3.anatomica.physics.LivingEntityLike;
import com.frank1o3.franklylib.Vec3;
import net.minecraft.util.Mth;

/**
 * Position-Based-Dynamics soft-body simulation over the fixed grid defined by
 * {@link SoftbodyGridLayout}, ported from a reference PBD breast-physics engine
 * and
 * adapted to Anatomica's node-only {@link IPhysicsEngine} contract.
 *
 * <p>
 * Differences from the reference this was ported from:
 * <ul>
 * <li>No legacy scalar outputs (positionX/Y/Z, bounceRotation) — every consumer
 * reads node data directly via
 * {@link #nodePosition}/{@link #interpolatedNodePosition}.
 * <li>No EntityConfig/IGenderArmor/ArmorStand-specific branches — armor
 * resistance
 * and forced-simplified-physics paths don't exist in this mod yet.
 * <li>No vehicle-type-specific impulse tuning (Boat/Minecart/Horse/Pig/Strider)
 * —
 * {@link LivingEntityLike} only exposes {@link LivingEntityLike#isPassenger()},
 * not
 * vehicle type, so passenger state just generally damps lateral impulse
 * instead.
 * <li>Vehicle handling is intentionally generic. {@link LivingEntityLike}
 * exposes passenger state but not individual vehicle types, so passenger state
 * simply damps lateral impulse.
 * <li>Arm-swing impulse is driven by a continuous
 * {@code attackSwingProgress(1.0f)}
 * read each tick rather than the reference's discrete every-Nth-tick sampling
 * (which needed {@code swingTime}/{@code tickCount}, not exposed here) — this
 * is a
 * simplification, not a verbatim port; retune {@code ARM_SWING_SCALE} if the
 * feel
 * is off in-game.
 * </ul>
 */
public final class SoftbodyPhysicsEngine implements IPhysicsEngine {

    private static final int PBD_ITERATIONS = 5;

    /** Constant downward pull applied every tick, scaled by the mesh's own size. */
    private static final float GRAVITY_PULL = 0.030f;
    /** Small constant resting sag so the mesh isn't perfectly rigid when idle. */
    private static final float RESTING_SAG = 0.0005f;

    // Bounds below are expressed as *fractions* of the grid's own half-extent/depth
    // (SoftbodyGridLayout.HALF_WIDTH/HALF_HEIGHT/DEPTH), since — unlike the
    // reference
    // engine's normalized [-1,1]/[0,1] rest space — this grid's rest positions are
    // already real, small world-space units.
    private static final float MAX_LATERAL_MOVEMENT_FRACTION = 0.8f;
    private static final float MAX_UPWARD_MOVEMENT_FRACTION = 0.55f;
    private static final float MAX_DOWNWARD_MOVEMENT_FRACTION = 0.55f;
    /**
     * Keep every free layer at least this far from the chest anchor (fraction of
     * its rest depth).
     */
    private static final float MIN_DEPTH_RATIO = 0.81f;
    private static final float MAX_FRONT_EXTENSION_RATIO = 1.75f;

    private static final float ARM_SWING_SCALE = 0.35f;
    private static final float IMPULSE_SCALE = 0.18f;

    private final SoftbodyGridLayout.Layout layout;
    private final int[][] constraintPairs;
    private final float[] restLengths;
    private final int[][] volumeCells;
    private final float restVolume;
    /**
     * Simulated per-node mass (front nodes slightly heavier -> more visible
     * inertia). Fixed nodes: MAX_VALUE.
     */
    private final float[] nodeMass;

    private final float[] posX, posY, posZ;
    private final float[] prevPosX, prevPosY, prevPosZ;
    private final float[] velX, velY, velZ;
    private final float[] interpX, interpY, interpZ;

    /**
     * The last entity position seen by this engine, for fixed-tick displacement.
     */
    private Vec3 previousEntityPosition;
    /** Vertical gait offset applied to the chest-anchored node layer. */
    private float walkBaseOffsetY;
    private boolean wasCrouching;
    private boolean wasSleeping;

    public SoftbodyPhysicsEngine() {
        this.layout = SoftbodyGridLayout.build();
        int n = layout.restPositions().length;

        this.constraintPairs = layout.constraintPairs().toArray(new int[0][]);
        this.restLengths = new float[constraintPairs.length];
        this.nodeMass = new float[n];
        this.volumeCells = buildVolumeCells();

        float maxZ = SoftbodyGridLayout.PHYSICS_DEPTH;
        for (int i = 0; i < n; i++) {
            Vec3 rest = layout.restPositions()[i];
            float zFactor = maxZ > 0f ? rest.z() / maxZ : 0f; // 0 (back) .. 1 (front)
            nodeMass[i] = layout.fixed()[i] ? Float.MAX_VALUE : 0.8f + zFactor * 0.4f;
        }

        for (int i = 0; i < constraintPairs.length; i++) {
            Vec3 a = layout.restPositions()[constraintPairs[i][0]];
            Vec3 b = layout.restPositions()[constraintPairs[i][1]];
            Vec3 delta = a.subtract(b);
            restLengths[i] = (float) Math.sqrt(delta.dot(delta));
        }
        this.restVolume = totalVolume(layout.restPositions());

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
        previousEntityPosition = null;
        walkBaseOffsetY = 0f;
        wasCrouching = false;
        wasSleeping = false;
    }

    @Override
    public void tick(float deltaTime, LivingEntityLike entity, IBodyConfig config) {
        if (!config.physicsEnabled()) {
            // Physics disabled: snap straight back to rest so the mesh reads as
            // static, rather than continuing to simulate an engine nothing reads.
            reset();
            return;
        }

        int n = posX.length;
        System.arraycopy(posX, 0, prevPosX, 0, n);
        System.arraycopy(posY, 0, prevPosY, 0, n);
        System.arraycopy(posZ, 0, prevPosZ, 0, n);

        applyDrivingForces(entity, config, deltaTime);
        integratePositions(deltaTime, config);
        solveConstraints(config);
        enforceBounds();
        deriveVelocities(deltaTime, config);

    }

    // -------------------------------------------------------------------
    // Force computation (impulse-per-tick, mirrors the reference engine's
    // computeImpulse/computeArmSwingImpulse, minus vehicle/rotation specifics)
    // -------------------------------------------------------------------

    private void applyDrivingForces(LivingEntityLike entity, IBodyConfig config, float deltaTime) {
        float bounceIntensity = config.bounceStrength() * 3.0f;

        // The gait belongs to the attachment/base, not to every simulated node.
        // Moving only the fixed chest layer lets the free layers lag behind and
        // settle through the PBD constraints instead of bobbing in lockstep.
        float walkPhase = entity.walkAnimationPhase();
        float walkSpeed = entity.walkAnimationSpeed();
        walkBaseOffsetY = walkSpeed > 0.01f
                ? Mth.cos(walkPhase * 0.6662f + Mth.PI) * 0.5f * walkSpeed * 0.5f * IMPULSE_SCALE
                : 0f;
        updateAnchorPositions();

        Vec3 currentPosition = entity.position();
        if (previousEntityPosition == null) {
            // There is no displacement to simulate until this engine has seen a
            // complete client tick. This avoids a false impulse when it is first
            // created or when the entity enters render distance.
            previousEntityPosition = currentPosition;
            return;
        }

        float impX = 0f;
        float impY = 0f;
        float impZ = 0f;

        Vec3 motion = currentPosition.subtract(previousEntityPosition);
        previousEntityPosition = currentPosition;

        impY += -motion.y() * bounceIntensity;
        impZ += -motion.z() * bounceIntensity * 2.0f;

        // Constant resting sag, scaled by the configured size, so the mesh doesn't
        // read as perfectly rigid even when the player stands still.
        impY += config.size() * RESTING_SAG;

        // Body yaw turning rotation impulse -> lateral sway
        float yawDelta = entity.bodyYawDelta();
        if (Math.abs(yawDelta) > 0.01f) {
            impX += -(yawDelta / 15.0f) * bounceIntensity;
        }

        // Arm swing (simplified continuous version — see class javadoc).
        float swing = entity.attackSwingProgress(1.0f);
        if (swing > 0f) {
            impY += (float) (Math.sin(swing * Math.PI)) * bounceIntensity * ARM_SWING_SCALE;
        }

        // Pose transitions (crouch/sleep), mirroring the reference's pose-change kick.
        boolean crouching = entity.isCrouching();
        boolean sleeping = entity.isSleeping();
        if (crouching != wasCrouching) {
            impY += bounceIntensity;
            wasCrouching = crouching;
        }
        if (sleeping != wasSleeping) {
            impY = bounceIntensity;
            wasSleeping = sleeping;
        }

        // Less horizontal responsiveness while swimming/flying (more horizontal-ish)
        // or riding something (no vehicle-type detail available here).
        if (entity.isSwimming() || entity.isFallFlying()) {
            impY *= 0.4f;
        }
        if (entity.isPassenger()) {
            impX *= 0.5f;
            impZ *= 0.5f;
        }

        impX = clampMagnitude(impX * IMPULSE_SCALE, 0.55f);
        impY = clampMagnitude(impY * IMPULSE_SCALE, 0.75f);
        impZ = clampMagnitude(impZ * IMPULSE_SCALE, 0.40f);

        // Gravity/breast-weight pull, applied every tick regardless of motion.
        float gravity = GRAVITY_PULL * config.size();

        for (int i = 0; i < velX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            // Front nodes (higher z) respond more to bounce; back-adjacent nodes less.
            float zFactor = layout.restPositions()[i].z() / SoftbodyGridLayout.PHYSICS_DEPTH;

            velX[i] += impX * zFactor;
            velY[i] += impY * zFactor + gravity;
            velZ[i] += impZ * zFactor;
        }
    }

    private void integratePositions(float deltaTime, IBodyConfig config) {
        float softness = config.softness();
        float damping = Mth.clamp(0.96f - softness * 0.20f, 0.72f, 0.96f);

        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            velX[i] *= damping;
            velY[i] *= damping;
            velZ[i] *= damping;

            posX[i] += velX[i];
            posY[i] += velY[i];
            posZ[i] += velZ[i];
        }
    }

    /**
     * Moves only the fixed back layer; dynamic nodes are pulled along by
     * constraints.
     */
    private void updateAnchorPositions() {
        for (int i = 0; i < posY.length; i++) {
            if (!layout.fixed()[i]) {
                continue;
            }
            Vec3 rest = layout.restPositions()[i];
            posX[i] = rest.x();
            posY[i] = rest.y() + walkBaseOffsetY;
            posZ[i] = rest.z();
            velX[i] = velY[i] = velZ[i] = 0f;
        }
    }

    private void solveConstraints(IBodyConfig config) {
        // Softer springs (higher softness) -> more visible deformation.
        float compliance = 0.05f + config.softness() * 0.30f;

        for (int iter = 0; iter < PBD_ITERATIONS; iter++) {
            for (int c = 0; c < constraintPairs.length; c++) {
                int a = constraintPairs[c][0];
                int b = constraintPairs[c][1];
                boolean fa = layout.fixed()[a];
                boolean fb = layout.fixed()[b];
                if (fa && fb) {
                    continue;
                }

                float dx = posX[b] - posX[a];
                float dy = posY[b] - posY[a];
                float dz = posZ[b] - posZ[a];
                float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist < 1.0e-6f) {
                    continue;
                }

                float restLen = restLengths[c];
                float diff = (dist - restLen) / (dist * 2.0f + compliance);

                float cx = dx * diff;
                float cy = dy * diff;
                float cz = dz * diff;

                // Mass-weighted split: heavier (more front-ward) nodes move less.
                float ma = nodeMass[a];
                float mb = nodeMass[b];
                float totalM = ma + mb;
                float wa = totalM > 0f ? mb / totalM : 0.5f;
                float wb = totalM > 0f ? ma / totalM : 0.5f;

                if (!fa) {
                    posX[a] += cx * wa;
                    posY[a] += cy * wa;
                    posZ[a] += cz * wa;
                }
                if (!fb) {
                    posX[b] -= cx * wb;
                    posY[b] -= cy * wb;
                    posZ[b] -= cz * wb;
                }
            }
            applyVolumeConstraint(config.petite());
        }
    }

    /**
     * Restores the grid's enclosed volume after the distance constraints run.
     * Scaling only the free layers along their chest-to-front axis retains sag
     * and lateral motion while pressure pushes an inward-collapsing mesh back
     * out from its anchored back layer.
     */
    private void applyVolumeConstraint(float strength) {
        if (strength <= 0f || restVolume <= 0f) {
            return;
        }
        float currentVolume = totalVolume(posX, posY, posZ);
        if (currentVolume <= 1.0e-7f) {
            return;
        }

        float targetDepthScale = Mth.clamp(restVolume / currentVolume, 0.85f, 1.20f);
        float correction = Mth.lerp(strength * 0.35f, 1.0f, targetDepthScale);
        for (int i = 0; i < posZ.length; i++) {
            if (!layout.fixed()[i]) {
                posZ[i] *= correction;
            }
        }
    }

    private int[][] buildVolumeCells() {
        int count = (SoftbodyGridLayout.COLS - 1) * (SoftbodyGridLayout.ROWS - 1)
                * (SoftbodyGridLayout.LAYERS - 1);
        int[][] cells = new int[count][8];
        int cell = 0;
        for (int z = 0; z < SoftbodyGridLayout.LAYERS - 1; z++) {
            for (int y = 0; y < SoftbodyGridLayout.ROWS - 1; y++) {
                for (int x = 0; x < SoftbodyGridLayout.COLS - 1; x++) {
                    cells[cell++] = new int[] {
                            SoftbodyGridLayout.index(x, y, z),
                            SoftbodyGridLayout.index(x + 1, y, z),
                            SoftbodyGridLayout.index(x + 1, y + 1, z),
                            SoftbodyGridLayout.index(x, y + 1, z),
                            SoftbodyGridLayout.index(x, y, z + 1),
                            SoftbodyGridLayout.index(x + 1, y, z + 1),
                            SoftbodyGridLayout.index(x + 1, y + 1, z + 1),
                            SoftbodyGridLayout.index(x, y + 1, z + 1)
                    };
                }
            }
        }
        return cells;
    }

    private float totalVolume(Vec3[] positions) {
        float[] x = new float[positions.length];
        float[] y = new float[positions.length];
        float[] z = new float[positions.length];
        for (int i = 0; i < positions.length; i++) {
            x[i] = positions[i].x();
            y[i] = positions[i].y();
            z[i] = positions[i].z();
        }
        return totalVolume(x, y, z);
    }

    private float totalVolume(float[] x, float[] y, float[] z) {
        float volume = 0f;
        for (int[] cell : volumeCells) {
            volume += tetrahedronVolume(cell[0], cell[1], cell[3], cell[4], x, y, z);
            volume += tetrahedronVolume(cell[1], cell[2], cell[3], cell[6], x, y, z);
            volume += tetrahedronVolume(cell[1], cell[3], cell[4], cell[6], x, y, z);
            volume += tetrahedronVolume(cell[1], cell[4], cell[5], cell[6], x, y, z);
            volume += tetrahedronVolume(cell[3], cell[4], cell[6], cell[7], x, y, z);
        }
        return volume;
    }

    private static float tetrahedronVolume(int a, int b, int c, int d, float[] x, float[] y, float[] z) {
        float abx = x[b] - x[a];
        float aby = y[b] - y[a];
        float abz = z[b] - z[a];
        float acx = x[c] - x[a];
        float acy = y[c] - y[a];
        float acz = z[c] - z[a];
        float adx = x[d] - x[a];
        float ady = y[d] - y[a];
        float adz = z[d] - z[a];
        float determinant = abx * (acy * adz - acz * ady)
                - aby * (acx * adz - acz * adx)
                + abz * (acx * ady - acy * adx);
        return Math.abs(determinant) / 6f;
    }

    /**
     * Clamps node positions to bounds relative to their rest positions, preventing
     * the mesh from collapsing back toward the chest anchor or inverting/exploding.
     */
    private void enforceBounds() {
        float halfWidth = SoftbodyGridLayout.HALF_WIDTH;
        float halfHeight = SoftbodyGridLayout.HALF_HEIGHT;

        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            Vec3 rest = layout.restPositions()[i];

            float lateralBound = halfWidth * MAX_LATERAL_MOVEMENT_FRACTION;
            posX[i] = Mth.clamp(posX[i], rest.x() - lateralBound, rest.x() + lateralBound);

            float upBound = halfHeight * MAX_UPWARD_MOVEMENT_FRACTION;
            float downBound = halfHeight * MAX_DOWNWARD_MOVEMENT_FRACTION;
            posY[i] = Mth.clamp(posY[i], rest.y() - upBound, rest.y() + downBound);

            if (rest.z() < 0f) {
                float minZ = rest.z() * MAX_FRONT_EXTENSION_RATIO; // 1.75x rest depth
                float maxZ = rest.z() * MIN_DEPTH_RATIO; // 0.81x rest depth
                posZ[i] = Mth.clamp(posZ[i], minZ, maxZ);
            } else {
                posZ[i] = Mth.clamp(posZ[i], -SoftbodyGridLayout.PHYSICS_DEPTH * 0.25f, 0f);
            }
        }
    }

    private void deriveVelocities(float deltaTime, IBodyConfig config) {
        float softness = config.softness();
        float damping = Mth.clamp(0.96f - softness * 0.20f, 0.72f, 0.96f);

        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            velX[i] = (posX[i] - prevPosX[i]) * damping;
            velY[i] = (posY[i] - prevPosY[i]) * damping;
            velZ[i] = (posZ[i] - prevPosZ[i]) * damping;
        }
    }

    private static float clampMagnitude(float value, float max) {
        return Mth.clamp(value, -max, max);
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
        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            Vec3 rest = layout.restPositions()[i];
            float dx = rest.x() - localPoint.x();
            float dy = rest.y() - localPoint.y();
            float dz = rest.z() - localPoint.z();
            float d2 = dx * dx + dy * dy + dz * dz;
            // Gaussian-like falloff so the impulse affects nearby nodes too, not just
            // the single nearest one.
            float w = (float) Math.exp(-d2 * 40.0);
            if (w < 0.01f) {
                continue;
            }
            float invMass = nodeMass[i] > 0f ? 1.0f / nodeMass[i] : 1.0f;
            velX[i] += force.x() * w * invMass;
            velY[i] += force.y() * w * invMass;
            velZ[i] += force.z() * w * invMass;
        }
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }
}
