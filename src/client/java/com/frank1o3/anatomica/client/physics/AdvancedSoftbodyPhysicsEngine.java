package com.frank1o3.anatomica.client.physics;

import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.ACTIVITY_BLEND;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.ADAPTATION_RATE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.ADAPTIVE_ACTIVITY_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.ADAPTIVE_BASELINE_MAX;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.ADAPTIVE_BASELINE_MIN;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.BOUNCE_BASE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.BOUNCE_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.BUOYANCY_FACTOR;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.DEFORMATION_NORMALIZER;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.DELTA_TIME_MAX;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.DELTA_TIME_MIN;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.FORWARD_REACTION_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.GEOMETRY_EPSILON;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.GRAVITY_ACCEL;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.IMPULSE_CUTOFF;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.IMPULSE_FALLOFF;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.INITIAL_DYNAMIC_DAMPING;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.LATERAL_BOUND_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.MAX_DEPTH_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.MAX_DYNAMIC_DAMPING;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.MAX_VELOCITY;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.MAX_VERTICAL_MOTION;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.MIN_DEPTH_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.MIN_DYNAMIC_DAMPING;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.NODE_MASS_BASE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.NODE_MASS_DEPTH_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.PASSENGER_FORCE_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.PBD_COMPLIANCE_BASE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.PBD_COMPLIANCE_SOFTNESS;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.PHYSICS_ITERATIONS;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.POSE_IMPULSE_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.POSITIVE_DEPTH_MIN;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.PRESSURE_BASE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.PRESSURE_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.REACTIVITY_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.RECONSTRUCTION_BLEND;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.REFERENCE_TICK_DELTA;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SPECIAL_MOVEMENT_FORCE_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SPRING_DAMPING_BASE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SPRING_DAMPING_MAX;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SPRING_DAMPING_MIN;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SPRING_DAMPING_SOFTNESS_BASE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SPRING_DAMPING_SOFTNESS_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SPRING_ITERATION_RATIO;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SPRING_STIFFNESS_BASE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SPRING_STIFFNESS_MAX;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SPRING_STIFFNESS_MIN;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SPRING_STIFFNESS_SOFTNESS_BASE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SPRING_STIFFNESS_SOFTNESS_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.SWING_IMPULSE_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.VELOCITY_EPSILON;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.VERTICAL_BOUND_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.VERTICAL_MOTION_RESPONSE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.VERTICAL_RESPONSE_SIGN;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.VOLUME_CORRECTION_LIMIT;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.VOLUME_ERROR_LIMIT;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.VOLUME_SETTING_MAX;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.YAW_RESPONSE_SCALE;
import static com.frank1o3.anatomica.client.physics.AdvancedSoftbodySolverConfig.volumeTargetScale;

import java.util.Arrays;

import com.frank1o3.anatomica.config.IBodyConfig;
import com.frank1o3.anatomica.physics.IPhysicsEngine;
import com.frank1o3.anatomica.physics.LivingEntityLike;
import com.frank1o3.franklylib.Vec3;

import net.minecraft.util.Mth;

/**
 * Advanced Anatomica soft-body physics.
 *
 * <p>
 * This solver combines:
 *
 * <ul>
 * <li>Spring-mass dynamics for continuous physical motion.</li>
 * <li>Position Based Dynamics for structural stabilization.</li>
 * <li>Volume/pressure constraints for internal volume.</li>
 * <li>Entity acceleration and angular motion as inertial influences.</li>
 * <li>Discrete impulses for events such as swings and pose transitions.</li>
 * </ul>
 *
 * <p>
 * Force sources are evaluated independently from one entity-state snapshot.
 * They do not mutate velocity or position while being collected. The resulting
 * forces and impulses are committed together before integration.
 *
 * <p>
 * The simulation is sub-stepped using {@link #PHYSICS_ITERATIONS}. This is
 * important because the public physics contract supplies delta time, and the
 * spring/PBD system must operate on a sufficiently small timestep rather than
 * treating one Minecraft tick as one unrestricted Euler integration.
 */
public final class AdvancedSoftbodyPhysicsEngine implements IPhysicsEngine {

    /*
     * -------------------------------------------------------------------------
     * Solver configuration
     * -------------------------------------------------------------------------
     *
     * These are INITIAL/default values, not values that should be blindly
     * treated as permanent physical constants.
     *
     * The solver maintains adaptive state for damping and stabilization.
     */

    /*
     * -------------------------------------------------------------------------
     * Layout / topology
     * -------------------------------------------------------------------------
     */
    private final SoftbodyGridLayout.Layout layout;
    private final int[][] constraintPairs;
    private final int[][] volumeCells;
    private final float[] restLengths;
    private final float[] nodeMass;

    /**
     * Rest volume of the complete grid.
     */
    private final float restVolume;

    /*
     * -------------------------------------------------------------------------
     * Position / velocity state
     * -------------------------------------------------------------------------
     */
    private final float[] posX;
    private final float[] posY;
    private final float[] posZ;
    private final float[] prevPosX;
    private final float[] prevPosY;
    private final float[] prevPosZ;
    private final float[] stepPrevPosX;
    private final float[] stepPrevPosY;
    private final float[] stepPrevPosZ;
    private final float[] velX;
    private final float[] velY;
    private final float[] velZ;
    private final float[] interpX;
    private final float[] interpY;
    private final float[] interpZ;

    /*
     * -------------------------------------------------------------------------
     * Temporary solver buffers
     * -------------------------------------------------------------------------
     *
     * These are allocated once and reused. The previous implementation created
     * force arrays every tick.
     */
    private final float[] forceX;
    private final float[] forceY;
    private final float[] forceZ;
    private final float[] impulseX;
    private final float[] impulseY;
    private final float[] impulseZ;

    /*
     * -------------------------------------------------------------------------
     * Entity history
     * -------------------------------------------------------------------------
     */
    private Vec3 previousEntityPosition;
    private Vec3 previousEntityMotion = Vec3.ZERO;
    private float previousYaw;
    private float filteredAccelerationX;
    private float filteredAccelerationY;
    private float filteredAccelerationZ;
    private boolean hasEntityHistory;
    private boolean wasCrouching;
    private boolean wasSleeping;

    /*
     * -------------------------------------------------------------------------
     * Adaptive solver state
     * -------------------------------------------------------------------------
     */
    private float dynamicDamping = INITIAL_DYNAMIC_DAMPING;

    /**
     * Smoothed estimate of deformation/constraint activity.
     */
    private float deformationActivity;

    /**
     * Smoothed estimate of node velocity activity.
     */
    private float velocityActivity;

    /**
     * Indicates that the first physical integration has not happened yet.
     *
     * The first tick deliberately uses conservative initial values because
     * velocity/acceleration history does not exist yet.
     */
    @SuppressWarnings("unused")
    private boolean initialized;

    public AdvancedSoftbodyPhysicsEngine() {
        this.layout = SoftbodyGridLayout.build();
        int nodeCount = layout.restPositions().length;
        this.constraintPairs = layout.constraintPairs().toArray(new int[0][]);
        this.volumeCells = buildVolumeCells();
        this.restLengths = new float[constraintPairs.length];
        this.nodeMass = new float[nodeCount];

        /*
         * Preserve the mass distribution used by the default implementation:
         * frontward nodes are slightly heavier and therefore exhibit more
         * inertia.
         */
        float maxZ = SoftbodyGridLayout.PHYSICS_DEPTH;

        for (int i = 0; i < nodeCount; i++) {
            Vec3 rest = layout.restPositions()[i];
            float zFactor = maxZ > 0.0f
                    ? rest.z() / maxZ
                    : 0.0f;
            nodeMass[i] = layout.fixed()[i]
                    ? Float.MAX_VALUE
                    : NODE_MASS_BASE + zFactor * NODE_MASS_DEPTH_SCALE;
        }

        for (int i = 0; i < constraintPairs.length; i++) {
            Vec3 a = layout.restPositions()[constraintPairs[i][0]];
            Vec3 b = layout.restPositions()[constraintPairs[i][1]];
            Vec3 delta = a.subtract(b);
            restLengths[i] = (float) Math.sqrt(delta.dot(delta));
        }

        this.restVolume = totalVolume(layout.restPositions());
        this.posX = new float[nodeCount];
        this.posY = new float[nodeCount];
        this.posZ = new float[nodeCount];
        this.prevPosX = new float[nodeCount];
        this.prevPosY = new float[nodeCount];
        this.prevPosZ = new float[nodeCount];
        this.stepPrevPosX = new float[nodeCount];
        this.stepPrevPosY = new float[nodeCount];
        this.stepPrevPosZ = new float[nodeCount];
        this.velX = new float[nodeCount];
        this.velY = new float[nodeCount];
        this.velZ = new float[nodeCount];
        this.interpX = new float[nodeCount];
        this.interpY = new float[nodeCount];
        this.interpZ = new float[nodeCount];
        this.forceX = new float[nodeCount];
        this.forceY = new float[nodeCount];
        this.forceZ = new float[nodeCount];
        this.impulseX = new float[nodeCount];
        this.impulseY = new float[nodeCount];
        this.impulseZ = new float[nodeCount];
        reset();
    }

    @Override
    public void reset() {
        for (int i = 0; i < posX.length; i++) {
            Vec3 rest = layout.restPositions()[i];
            posX[i] = rest.x();
            posY[i] = rest.y();
            posZ[i] = rest.z();
            prevPosX[i] = rest.x();
            prevPosY[i] = rest.y();
            prevPosZ[i] = rest.z();
            stepPrevPosX[i] = rest.x();
            stepPrevPosY[i] = rest.y();
            stepPrevPosZ[i] = rest.z();
            interpX[i] = rest.x();
            interpY[i] = rest.y();
            interpZ[i] = rest.z();
            velX[i] = 0.0f;
            velY[i] = 0.0f;
            velZ[i] = 0.0f;
        }
        clearForceBuffers();
        previousEntityPosition = null;
        previousEntityMotion = Vec3.ZERO;
        previousYaw = 0.0f;
        filteredAccelerationX = 0.0f;
        filteredAccelerationY = 0.0f;
        filteredAccelerationZ = 0.0f;
        hasEntityHistory = false;
        wasCrouching = false;
        wasSleeping = false;
        dynamicDamping = INITIAL_DYNAMIC_DAMPING;
        deformationActivity = 0.0f;
        velocityActivity = 0.0f;
        initialized = false;
    }

    /*
     * -------------------------------------------------------------------------
     * Main simulation entry point
     * -------------------------------------------------------------------------
     */

    @Override
    public void tick(
            float deltaTime,
            LivingEntityLike entity,
            IBodyConfig config) {
        if (!config.physicsEnabled()) {
            reset();
            return;
        }

        /*
         * deltaTime is allowed to vary, but we never permit a pathological
         * value to turn into an enormous physics step.
         *
         * The normal Minecraft tick is expected to be approximately 1.0.
         */
        float frameDelta = sanitizeDeltaTime(deltaTime) / REFERENCE_TICK_DELTA;
        ;

        /*
         * Rendering interpolation relies on these being the positions from
         * the previous completed physics tick.
         */
        System.arraycopy(posX, 0, prevPosX, 0, posX.length);
        System.arraycopy(posY, 0, prevPosY, 0, posY.length);
        System.arraycopy(posZ, 0, prevPosZ, 0, posZ.length);

        /*
         * Capture entity state ONCE.
         *
         * Every force/impulse source for this physics tick receives the same
         * snapshot. This is the "asynchronous" behavior we discussed:
         * independent observations, one synchronized physical commit.
         */
        EntityState state = captureEntityState(frameDelta, entity);

        /*
         * The central iteration count determines the timestep resolution.
         */
        float substepDelta = frameDelta / (float) PHYSICS_ITERATIONS;

        /*
         * We distribute the PBD budget through the simulation instead of
         * running a completely separate PBD simulation after all spring
         * dynamics have finished.
         */
        int pbdBudget = Math.max(
                1,
                Math.round(
                        PHYSICS_ITERATIONS
                                * (1.0f - SPRING_ITERATION_RATIO)));
        int pbdPassesPerSubstep = Math.max(
                1,
                (int) Math.ceil(
                        pbdBudget
                                / (float) PHYSICS_ITERATIONS));
        for (int iteration = 0; iteration < PHYSICS_ITERATIONS; iteration++) {

            /*
             * Keep the start of this substep separate from the previous
             * completed tick. PBD velocity reconstruction must use this
             * snapshot; using prevPos here would re-inject the whole tick's
             * displacement on every substep and create artificial bounce.
             */
            System.arraycopy(posX, 0, stepPrevPosX, 0, posX.length);
            System.arraycopy(posY, 0, stepPrevPosY, 0, posY.length);
            System.arraycopy(posZ, 0, stepPrevPosZ, 0, posZ.length);

            /*
             * Fixed nodes follow the attachment before calculating forces.
             */
            updateAnchors();

            /*
             * Every contribution is collected into buffers.
             */
            clearForceBuffers();
            collectContinuousForces(
                    state,
                    config,
                    substepDelta);
            collectEventImpulses(
                    state,
                    config);

            /*
             * Commit all instantaneous impulses first.
             *
             * An impulse is a Δv, not a force integrated over dt.
             */
            applyAccumulatedImpulses();

            /*
             * Integrate all continuous forces using the actual substep dt.
             */
            integrateForces(
                    config,
                    substepDelta);

            /*
             * Predict positions.
             */
            integratePositions(substepDelta);

            /*
             * PBD then corrects the predicted geometry.
             *
             * It does NOT replace the spring simulation. It stabilizes it.
             */
            for (int pbd = 0; pbd < pbdPassesPerSubstep; pbd++) {
                solveDistanceConstraints(
                        config,
                        substepDelta);
                solveVolumeConstraint(
                        config.petite());
            }

            /*
             * Bounds are treated as another positional constraint/safety
             * mechanism rather than as a force.
             */
            enforceBounds();

            /*
             * PBD changed positions, so velocity must be reconstructed from
             * the corrected state.
             *
             * This is essential. Otherwise PBD would visually move the nodes
             * while the spring solver continued using the old velocity.
             */
            reconstructVelocities(
                    substepDelta,
                    stepPrevPosX,
                    stepPrevPosY,
                    stepPrevPosZ);
        }

        /*
         * Update adaptive solver parameters AFTER the complete tick.
         *
         * The next tick therefore uses information produced by this tick,
         * rather than modifying the parameters halfway through a solve.
         */
        updateAdaptiveDamping(config);
        initialized = true;
    }

    /*
     * -------------------------------------------------------------------------
     * Entity state snapshot
     * -------------------------------------------------------------------------
     */

    private EntityState captureEntityState(
            float deltaTime,
            LivingEntityLike entity) {
        Vec3 currentPosition = entity.position();
        Vec3 motion = Vec3.ZERO;
        Vec3 acceleration = Vec3.ZERO;

        if (!hasEntityHistory) {
            previousEntityPosition = currentPosition;
            previousEntityMotion = Vec3.ZERO;
            previousYaw = entity.bodyYaw();
            hasEntityHistory = true;
        } else {
            motion = currentPosition.subtract(
                    previousEntityPosition);
            acceleration = motion.subtract(previousEntityMotion);
            previousEntityPosition = currentPosition;
            previousEntityMotion = motion;
        }

        /*
         * Convert the displacement-derived acceleration into an actual
         * delta-time-scaled quantity.
         *
         * The entity motion is measured per Minecraft tick. Dividing by dt
         * gives the velocity change corresponding to the supplied simulation
         * interval.
         */
        float inverseDt = deltaTime > GEOMETRY_EPSILON
                ? 1.0f / deltaTime
                : 1.0f;
        float rawAccelerationX = acceleration.x() * inverseDt;
        float rawAccelerationY = acceleration.y() * inverseDt;
        float rawAccelerationZ = acceleration.z() * inverseDt;

        /*
         * Low-pass the entity acceleration.
         *
         * This is intentionally separate from the physical damping. Entity
         * position can contain tiny collision/interpolation corrections, and
         * feeding every one of those directly into the soft body creates
         * artificial jitter.
         */
        final float accelerationFilter = 0.35f;
        filteredAccelerationX = Mth.lerp(
                accelerationFilter,
                filteredAccelerationX,
                rawAccelerationX);
        filteredAccelerationY = Mth.lerp(
                accelerationFilter,
                filteredAccelerationY,
                rawAccelerationY);
        filteredAccelerationZ = Mth.lerp(
                accelerationFilter,
                filteredAccelerationZ,
                rawAccelerationZ);

        float yaw = entity.bodyYaw();
        float yawDelta = Mth.wrapDegrees(yaw - previousYaw);
        previousYaw = yaw;
        float yawRate = yawDelta * inverseDt;

        /*
         * Convert acceleration into the entity's local lateral/forward frame.
         */
        float yawRad = -yaw * Mth.DEG_TO_RAD;
        float cos = Mth.cos(yawRad);
        float sin = Mth.sin(yawRad);
        float localAccelerationX = filteredAccelerationX * cos
                - filteredAccelerationZ * sin;
        float localAccelerationZ = filteredAccelerationX * sin
                + filteredAccelerationZ * cos;

        return new EntityState(
                motion,
                entity.verticalVelocity(),
                entity.isOnGround(),
                new Vec3(
                        localAccelerationX,
                        filteredAccelerationY,
                        localAccelerationZ),
                yawRate,
                entity.isSwimming(),
                entity.isFallFlying(),
                entity.isPassenger(),
                entity.isCrouching(),
                entity.isSleeping(),
                entity.attackSwingProgress(1.0f),
                entity.walkAnimationPhase(),
                entity.walkAnimationSpeed());
    }

    /*
     * Immutable state used by all force sources during one tick.
     */
    private record EntityState(
            Vec3 motion,
            float verticalVelocity,
            boolean onGround,
            Vec3 localAcceleration,
            float yawRate,
            boolean swimming,
            boolean fallFlying,
            boolean passenger,
            boolean crouching,
            boolean sleeping,
            float swingProgress,
            float walkPhase,
            float walkSpeed) {
    }

    /*
     * -------------------------------------------------------------------------
     * Force collection
     * -------------------------------------------------------------------------
     */

    private void collectContinuousForces(
            EntityState state,
            IBodyConfig config,
            float deltaTime) {
        /*
         * These methods only write to forceX/Y/Z.
         *
         * None of them modifies velocity or position.
         */
        collectSpringForces(
                config,
                deltaTime);
        collectGravityAndBuoyancy(
                state,
                config);
        collectInertialForces(
                state,
                config);
        collectYawInertia(
                state,
                config);
    }

    private void collectSpringForces(
            IBodyConfig config,
            float deltaTime) {
        float softness = Mth.clamp(config.softness(), 0.0f, 1.0f);

        /*
         * Softness controls how much the baseline spring stiffness is reduced.
         *
         * This remains only the BASE material property. Adaptive damping is
         * handled separately.
         */
        float stiffness = SPRING_STIFFNESS_BASE
                * Mth.clamp(
                        SPRING_STIFFNESS_SOFTNESS_BASE - softness * SPRING_STIFFNESS_SOFTNESS_SCALE,
                        SPRING_STIFFNESS_MIN,
                        SPRING_STIFFNESS_MAX);
        float damping = SPRING_DAMPING_BASE
                * Mth.clamp(
                        SPRING_DAMPING_SOFTNESS_BASE - softness * SPRING_DAMPING_SOFTNESS_SCALE,
                        SPRING_DAMPING_MIN,
                        SPRING_DAMPING_MAX);
        damping *= dynamicDamping;

        for (int c = 0; c < constraintPairs.length; c++) {
            int a = constraintPairs[c][0];
            int b = constraintPairs[c][1];
            float dx = posX[b] - posX[a];
            float dy = posY[b] - posY[a];
            float dz = posZ[b] - posZ[a];
            float distance = (float) Math.sqrt(
                    dx * dx
                            + dy * dy
                            + dz * dz);

            if (distance < 1.0e-6f) {
                continue;
            }

            float nx = dx / distance;
            float ny = dy / distance;
            float nz = dz / distance;

            /*
             * Hookean spring.
             */
            float displacement = distance - restLengths[c];
            float springForce = displacement * stiffness;

            /*
             * Relative velocity along the spring axis.
             *
             * IMPORTANT:
             *
             * Damping OPPOSES relative motion.
             *
             * The old Advanced implementation used:
             *
             * springForce + relVel * damping
             *
             * which can inject energy into the spring.
             */
            float relativeVelocity = (velX[b] - velX[a]) * nx
                    + (velY[b] - velY[a]) * ny
                    + (velZ[b] - velZ[a]) * nz;

            float dampingForce = -relativeVelocity * damping;
            float totalForce = springForce + dampingForce;
            float fx = nx * totalForce;
            float fy = ny * totalForce;
            float fz = nz * totalForce;

            if (!layout.fixed()[a]) {
                forceX[a] += fx;
                forceY[a] += fy;
                forceZ[a] += fz;
            }
            if (!layout.fixed()[b]) {
                forceX[b] -= fx;
                forceY[b] -= fy;
                forceZ[b] -= fz;
            }
        }
    }

    private void collectGravityAndBuoyancy(
            EntityState state,
            IBodyConfig config) {

        /*
         * Preserve the gravity convention used by the working default engine:
         * positive local Y is the direction of the resting sag.
         *
         * Swimming reduces the effective gravitational pull rather than
         * completely replacing the physics with a special animation.
         */
        float gravity = GRAVITY_ACCEL
                * config.size();
        if (state.swimming()) {
            gravity *= BUOYANCY_FACTOR;
        }

        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            float mass = nodeMass[i];
            forceY[i] += gravity * mass;
        }
    }

    private void collectInertialForces(
            EntityState state,
            IBodyConfig config) {
        Vec3 acceleration = state.localAcceleration();
        float entityVerticalVelocity = state.onGround()
                ? 0.0f
                : state.verticalVelocity();
        float verticalMotion = Mth.clamp(
                !state.onGround() && Math.abs(entityVerticalVelocity) > GEOMETRY_EPSILON
                        ? entityVerticalVelocity
                        : !state.onGround() ? state.motion().y() : 0.0f,
                -MAX_VERTICAL_MOTION,
                MAX_VERTICAL_MOTION);
        float verticalAcceleration = state.onGround()
                ? 0.0f
                : acceleration.y();

        /*
         * Bounce is treated as a response coefficient rather than as a
         * collection of independent "walking/jumping" impulses.
         */
        float bounce = BOUNCE_BASE
                + config.bounceStrength() * BOUNCE_SCALE;

        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            float zFactor = depthResponse(i);
            float mass = nodeMass[i];

            /*
             * D'Alembert reaction:
             *
             * accelerating the attachment in one direction causes the free
             * mass to resist in the opposite direction.
             */
            forceX[i] += -acceleration.x()
                    * mass
                    * REACTIVITY_SCALE
                    * bounce
                    * zFactor;
            forceY[i] += VERTICAL_RESPONSE_SIGN * verticalAcceleration
                    * mass
                    * REACTIVITY_SCALE
                    * bounce
                    * zFactor;
            forceY[i] += VERTICAL_RESPONSE_SIGN * verticalMotion
                    * mass
                    * REACTIVITY_SCALE
                    * bounce
                    * VERTICAL_MOTION_RESPONSE
                    * zFactor;
            forceZ[i] += -acceleration.z()
                    * mass
                    * REACTIVITY_SCALE
                    * bounce
                    * FORWARD_REACTION_SCALE
                    * zFactor;
        }
    }

    private void collectYawInertia(
            EntityState state,
            IBodyConfig config) {
        /*
         * Yaw is not processed as a separate "apply this after movement"
         * animation. It is simply another simultaneous inertial contribution.
         *
         * This is deliberately weak relative to translational acceleration
         * because yawRate is angular velocity rather than linear acceleration.
         */
        float yawRate = state.yawRate();
        if (Math.abs(yawRate) < 1.0e-5f) {
            return;
        }

        float bounce = BOUNCE_BASE + config.bounceStrength() * BOUNCE_SCALE;
        /*
         * Convert angular motion into a lateral reaction. The coefficient is
         * deliberately conservative because the body's actual response still
         * comes from springs/PBD rather than directly moving the mesh.
         */
        float angularResponse = yawRate * YAW_RESPONSE_SCALE * bounce;

        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            float zFactor = depthResponse(i);
            float mass = nodeMass[i];
            forceX[i] += -angularResponse
                    * mass
                    * zFactor;
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Event / impulse collection
     * -------------------------------------------------------------------------
     *
     * These are deliberately separate from continuous forces.
     *
     * A pose transition is an event.
     * An arm swing is an event/interaction.
     * External applyImpulse() is an event/interaction.
     *
     * None of them should masquerade as a force that exists for the entire
     * duration of a tick.
     */

    private void collectEventImpulses(
            EntityState state,
            IBodyConfig config) {
        float bounce = BOUNCE_BASE
                + config.bounceStrength() * BOUNCE_SCALE;

        /*
         * Crouch transition.
         */
        if (state.crouching() != wasCrouching) {
            float impulse = bounce * POSE_IMPULSE_SCALE;
            addVerticalImpulse(
                    impulse);
            wasCrouching = state.crouching();
        }

        /*
         * Sleep transition.
         */
        if (state.sleeping() != wasSleeping) {
            float impulse = bounce * POSE_IMPULSE_SCALE;
            addVerticalImpulse(
                    impulse);
            wasSleeping = state.sleeping();
        }

        /*
         * Arm swing.
         *
         * This remains an event contribution rather than a continuous force.
         */
        float swing = state.swingProgress();
        if (swing > 0.0f) {
            float swingImpulse = Mth.sin(swing * Mth.PI) * bounce * SWING_IMPULSE_SCALE
                    / PHYSICS_ITERATIONS;
            addVerticalImpulse(swingImpulse);
        }

        /*
         * Passenger state changes the physical response of the entire body.
         *
         * This is a state modifier, not an ordering-dependent event.
         */
        if (state.passenger()) {
            dampLateralForces(PASSENGER_FORCE_SCALE);
        }

        /*
         * Swimming/fall-flying similarly modifies the continuous response
         * rather than trying to create an entirely separate animation.
         */
        if (state.swimming()
                || state.fallFlying()) {
            forceYScale(SPECIAL_MOVEMENT_FORCE_SCALE);
        }
    }

    private void addVerticalImpulse(float amount) {
        for (int i = 0; i < impulseY.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            impulseY[i] += amount * depthResponse(i);
        }
    }

    private void dampLateralForces(
            float multiplier) {
        for (int i = 0; i < forceX.length; i++) {
            forceX[i] *= multiplier;
            forceZ[i] *= multiplier;
        }
    }

    private void forceYScale(
            float multiplier) {
        for (int i = 0; i < forceY.length; i++) {
            forceY[i] *= multiplier;
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Integration
     * -------------------------------------------------------------------------
     */

    private void applyAccumulatedImpulses() {
        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            float invMass = inverseMass(i);

            /*
             * applyImpulse() and generated event impulses are represented as
             * velocity changes, not forces.
             */
            velX[i] += impulseX[i] * invMass;
            velY[i] += impulseY[i] * invMass;
            velZ[i] += impulseZ[i] * invMass;
        }

        /*
         * The impulse buffer represents only this integration step.
         */
        clearImpulseBuffers();
    }

    private void integrateForces(
            IBodyConfig config,
            float deltaTime) {
        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            float invMass = inverseMass(i);

            /*
             * Force -> acceleration.
             */
            float accelerationX = forceX[i] * invMass;
            float accelerationY = forceY[i] * invMass;
            float accelerationZ = forceZ[i] * invMass;

            /*
             * Acceleration -> velocity.
             *
             * Delta time is explicitly applied here.
             */
            velX[i] += accelerationX * deltaTime;
            velY[i] += accelerationY * deltaTime;
            velZ[i] += accelerationZ * deltaTime;

            /*
             * Dynamic damping is multiplicative and timestep-aware.
             */
            float damping = timestepDamping(
                    deltaTime);
            velX[i] *= damping;
            velY[i] *= damping;
            velZ[i] *= damping;

            /*
             * Safety only. Normal behavior should live well below this limit.
             */
            velX[i] = Mth.clamp(
                    velX[i],
                    -MAX_VELOCITY,
                    MAX_VELOCITY);
            velY[i] = Mth.clamp(
                    velY[i],
                    -MAX_VELOCITY,
                    MAX_VELOCITY);
            velZ[i] = Mth.clamp(
                    velZ[i],
                    -MAX_VELOCITY,
                    MAX_VELOCITY);
        }
    }

    private void integratePositions(
            float deltaTime) {
        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            posX[i] += velX[i] * deltaTime;
            posY[i] += velY[i] * deltaTime;
            posZ[i] += velZ[i] * deltaTime;
        }
    }

    /*
     * -------------------------------------------------------------------------
     * PBD
     * -------------------------------------------------------------------------
     *
     * PBD is not the source of the motion. It stabilizes and corrects the
     * prediction produced by the spring-mass solver.
     */

    private void solveDistanceConstraints(
            IBodyConfig config,
            float deltaTime) {
        float softness = Mth.clamp(
                config.softness(),
                0.0f,
                1.0f);

        /*
         * Softness affects how much positional correction a PBD pass performs.
         *
         * A hard-coded "spring" is therefore not the only thing determining
         * the final shape.
         */
        float compliance = PBD_COMPLIANCE_BASE
                + softness * PBD_COMPLIANCE_SOFTNESS;

        for (int c = 0; c < constraintPairs.length; c++) {
            int a = constraintPairs[c][0];
            int b = constraintPairs[c][1];
            boolean fixedA = layout.fixed()[a];
            boolean fixedB = layout.fixed()[b];

            if (fixedA && fixedB) {
                continue;
            }

            float dx = posX[b] - posX[a];
            float dy = posY[b] - posY[a];
            float dz = posZ[b] - posZ[a];
            float distance = (float) Math.sqrt(
                    dx * dx
                            + dy * dy
                            + dz * dz);
            if (distance < 1.0e-6f) {
                continue;
            }

            float inverseDistance = 1.0f / distance;
            float nx = dx * inverseDistance;
            float ny = dy * inverseDistance;
            float nz = dz * inverseDistance;
            float restLength = restLengths[c];

            /*
             * XPBD-style compliance term.
             *
             * The timestep participates in the constraint response. This is
             * important now that the simulation actually has deltaTime.
             */
            float alpha = compliance
                    / Math.max(
                            deltaTime
                                    * deltaTime,
                            1.0e-6f);
            float constraintError = distance
                    - restLength;

            /*
             * Inverse masses.
             */
            float wA = fixedA
                    ? 0.0f
                    : inverseMass(a);
            float wB = fixedB
                    ? 0.0f
                    : inverseMass(b);
            float denominator = wA
                    + wB
                    + alpha;
            if (denominator < 1.0e-8f) {
                continue;
            }

            /*
             * Constraint correction.
             */
            float lambda = -constraintError
                    / denominator;
            float correctionX = lambda * nx;
            float correctionY = lambda * ny;
            float correctionZ = lambda * nz;

            if (!fixedA) {
                posX[a] -= correctionX * wA;
                posY[a] -= correctionY * wA;
                posZ[a] -= correctionZ * wA;
            }
            if (!fixedB) {
                posX[b] += correctionX * wB;
                posY[b] += correctionY * wB;
                posZ[b] += correctionZ * wB;
            }
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Volume / pressure
     * -------------------------------------------------------------------------
     *
     * The default engine already computes the enclosed volume from tetrahedra.
     * We retain that topology but make the target volume configurable.
     *
     * config.petite() is currently the existing volume-strength/configuration
     * channel in Anatomica, so the Advanced engine continues to use it until
     * the config field is eventually renamed.
     */

    private void solveVolumeConstraint(
            float volumeStrength) {
        if (volumeStrength <= 0.0f
                || restVolume <= GEOMETRY_EPSILON) {

            return;
        }
        float currentVolume = totalVolume(
                posX,
                posY,
                posZ);
        if (currentVolume <= GEOMETRY_EPSILON) {
            return;
        }

        /*
         * Map the config value into a target volume modifier.
         *
         * The current default behavior effectively preserves rest volume while
         * scaling how strongly the correction is applied. Advanced needs a
         * genuine volume target so the UI can actually inflate/deflate the
         * body rather than merely asking for "more correction".
         *
         * The neutral point remains the rest volume.
         */
        float normalized = Mth.clamp(
                volumeStrength / VOLUME_SETTING_MAX,
                0.0f,
                1.0f);

        /*
         * A conservative target range keeps the solver from instantly
         * destroying the geometry at extreme UI values.
         */
        float targetScale = volumeTargetScale(volumeStrength);
        float targetVolume = restVolume
                * targetScale;
        float volumeError = targetVolume
                - currentVolume;

        /*
         * Convert volume error into a normalized pressure term.
         *
         * Pressure is intentionally bounded. This is what stops the volume
         * control from becoming an unlimited energy source.
         */
        float relativeError = volumeError
                / Math.max(
                        restVolume,
                        GEOMETRY_EPSILON);
        relativeError = Mth.clamp(
                relativeError,
                -VOLUME_ERROR_LIMIT,
                VOLUME_ERROR_LIMIT);

        /*
         * The correction is applied along the chest-to-front axis because this
         * is how the existing Anatomica grid represents its soft-body depth.
         *
         * PBD performs the actual geometric correction; this method only
         * determines the pressure/volume target.
         */
        float pressure = relativeError
                * (PRESSURE_BASE + normalized * PRESSURE_SCALE);
        for (int i = 0; i < posZ.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            Vec3 rest = layout.restPositions()[i];
            float depth = rest.z();

            /*
             * Back-adjacent nodes should move much less than the front nodes.
             */
            float zFactor = depthResponse(i);
            float currentDepth = posZ[i];

            /*
             * Move proportionally to the node's rest depth.
             *
             * This makes the volume response coherent with the mesh instead
             * of pushing every node by the same world-space amount.
             */
            float correction = depth * pressure * zFactor;
            posZ[i] += correction;

            /*
             * Keep the correction itself within a conservative percentage of
             * the current depth.
             */
            float maximumCorrection = Math.abs(
                    depth)
                    * VOLUME_CORRECTION_LIMIT;
            posZ[i] = Mth.clamp(
                    posZ[i],
                    currentDepth
                            - maximumCorrection,
                    currentDepth
                            + maximumCorrection);
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Velocity reconstruction
     * -------------------------------------------------------------------------
     */

    private void reconstructVelocities(
            float deltaTime,
            float[] previousStepX,
            float[] previousStepY,
            float[] previousStepZ) {
        float inverseDt = deltaTime > GEOMETRY_EPSILON
                ? 1.0f / deltaTime
                : 1.0f;

        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                velX[i] = 0.0f;
                velY[i] = 0.0f;
                velZ[i] = 0.0f;
                continue;
            }

            float correctedVelocityX = (posX[i] - previousStepX[i]) * inverseDt;
            float correctedVelocityY = (posY[i] - previousStepY[i]) * inverseDt;
            float correctedVelocityZ = (posZ[i] - previousStepZ[i]) * inverseDt;

            /*
             * The reconstructed value is blended with the existing integrated
             * velocity instead of completely replacing it.
             *
             * This prevents PBD from becoming a hard positional teleporter
             * while still feeding its correction back into the physical
             * velocity state.
             */
            final float correctionBlend = RECONSTRUCTION_BLEND;
            velX[i] = Mth.lerp(
                    correctionBlend,
                    velX[i],
                    correctedVelocityX);
            velY[i] = Mth.lerp(
                    correctionBlend,
                    velY[i],
                    correctedVelocityY);
            velZ[i] = Mth.lerp(
                    correctionBlend,
                    velZ[i],
                    correctedVelocityZ);

            /*
             * Remove microscopic numerical noise.
             */
            if (Math.abs(velX[i]) < VELOCITY_EPSILON) {
                velX[i] = 0.0f;
            }
            if (Math.abs(velY[i]) < VELOCITY_EPSILON) {
                velY[i] = 0.0f;
            }
            if (Math.abs(velZ[i]) < VELOCITY_EPSILON) {
                velZ[i] = 0.0f;
            }
            velX[i] = Mth.clamp(
                    velX[i],
                    -MAX_VELOCITY,
                    MAX_VELOCITY);
            velY[i] = Mth.clamp(
                    velY[i],
                    -MAX_VELOCITY,
                    MAX_VELOCITY);
            velZ[i] = Mth.clamp(
                    velZ[i],
                    -MAX_VELOCITY,
                    MAX_VELOCITY);
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Adaptive damping
     * -------------------------------------------------------------------------
     */

    private void updateAdaptiveDamping(
            IBodyConfig config) {
        float totalVelocity = 0.0f;
        float totalDeformation = 0.0f;
        int dynamicNodes = 0;

        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }
            dynamicNodes++;
            float speed = (float) Math.sqrt(
                    velX[i] * velX[i]
                            + velY[i] * velY[i]
                            + velZ[i] * velZ[i]);

            totalVelocity += speed;
            Vec3 rest = layout.restPositions()[i];
            float dx = posX[i] - rest.x();
            float dy = posY[i] - rest.y();
            float dz = posZ[i] - rest.z();
            totalDeformation += (float) Math.sqrt(
                    dx * dx
                            + dy * dy
                            + dz * dz);
        }

        if (dynamicNodes == 0) {
            return;
        }

        float inverseNodeCount = 1.0f / dynamicNodes;
        float averageVelocity = totalVelocity
                * inverseNodeCount;
        float averageDeformation = totalDeformation
                * inverseNodeCount;

        /*
         * Smooth measurements rather than changing damping based on one noisy
         * node or one noisy tick.
         */
        final float activityBlend = ACTIVITY_BLEND;
        velocityActivity = Mth.lerp(
                activityBlend,
                velocityActivity,
                averageVelocity);
        deformationActivity = Mth.lerp(
                activityBlend,
                deformationActivity,
                averageDeformation);

        /*
         * High activity means the system is currently carrying more kinetic or
         * deformation energy, so damping is allowed to increase.
         *
         * Quiet systems progressively return toward their baseline damping.
         */
        float velocityTerm = Mth.clamp(
                velocityActivity
                        / MAX_VELOCITY,
                0.0f,
                1.0f);
        float deformationTerm = Mth.clamp(
                deformationActivity
                        / DEFORMATION_NORMALIZER,
                0.0f,
                1.0f);
        float activity = Math.max(
                velocityTerm,
                deformationTerm);

        /*
         * Softness still matters, but it no longer completely determines the
         * damping.
         */
        float softness = Mth.clamp(
                config.softness(),
                0.0f,
                1.0f);
        float baseline = Mth.lerp(
                ADAPTIVE_BASELINE_MIN,
                ADAPTIVE_BASELINE_MAX,
                1.0f - softness);
        float target = Mth.lerp(
                baseline,
                MAX_DYNAMIC_DAMPING,
                activity * ADAPTIVE_ACTIVITY_SCALE);

        /*
         * Critically, the parameter changes gradually.
         */
        final float adaptationRate = ADAPTATION_RATE;
        dynamicDamping = Mth.lerp(
                adaptationRate,
                dynamicDamping,
                Mth.clamp(
                        target,
                        MIN_DYNAMIC_DAMPING,
                        MAX_DYNAMIC_DAMPING));
    }

    private float timestepDamping(
            float deltaTime) {

        /*
         * Convert the per-tick damping to the current substep so changing the
         * number of physics iterations does not unexpectedly change the
         * material's damping.
         */
        float referenceDamping = Mth.clamp(
                dynamicDamping,
                MIN_DYNAMIC_DAMPING,
                MAX_DYNAMIC_DAMPING);
        return (float) Math.pow(
                referenceDamping,
                deltaTime);
    }

    /*
     * -------------------------------------------------------------------------
     * Anchors / bounds
     * -------------------------------------------------------------------------
     */

    private void updateAnchors() {
        for (int i = 0; i < posX.length; i++) {
            if (!layout.fixed()[i]) {
                continue;
            }
            Vec3 rest = layout.restPositions()[i];
            posX[i] = rest.x();
            posY[i] = rest.y();
            posZ[i] = rest.z();
            velX[i] = 0.0f;
            velY[i] = 0.0f;
            velZ[i] = 0.0f;
        }
    }

    private void enforceBounds() {
        float halfWidth = SoftbodyGridLayout.HALF_WIDTH;
        float halfHeight = SoftbodyGridLayout.HALF_HEIGHT;

        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }

            Vec3 rest = layout.restPositions()[i];
            /*
             * Preserve the working default envelope.
             */
            float lateralBound = halfWidth * LATERAL_BOUND_SCALE;
            posX[i] = Mth.clamp(
                    posX[i],
                    rest.x()
                            - lateralBound,
                    rest.x()
                            + lateralBound);
            float upwardBound = halfHeight * VERTICAL_BOUND_SCALE;
            float downwardBound = halfHeight * VERTICAL_BOUND_SCALE;
            posY[i] = Mth.clamp(
                    posY[i],
                    rest.y()
                            - upwardBound,
                    rest.y()
                            + downwardBound);

            if (rest.z() < 0.0f) {
                /*
                 * Preserve the existing minimum depth / maximum extension
                 * envelope.
                 */
                float minimumZ = rest.z() * MIN_DEPTH_SCALE;
                float maximumZ = rest.z() * MAX_DEPTH_SCALE;
                posZ[i] = Mth.clamp(
                        posZ[i],
                        minimumZ,
                        maximumZ);
            } else {
                posZ[i] = Mth.clamp(
                        posZ[i],
                        -SoftbodyGridLayout.PHYSICS_DEPTH
                                * POSITIVE_DEPTH_MIN,
                        0.0f);
            }
        }
    }

    /*
     * -------------------------------------------------------------------------
     * External impulses
     * -------------------------------------------------------------------------
     */

    @Override
    public void applyImpulse(
            Vec3 localPoint,
            Vec3 force) {
        /*
         * Do not immediately mutate velocity.
         *
         * The impulse is queued and consumed by the next physics integration.
         *
         * This makes external impulses obey the same ordering-independent
         * accumulation rule as internally generated event impulses.
         */
        for (int i = 0; i < posX.length; i++) {
            if (layout.fixed()[i]) {
                continue;
            }

            Vec3 rest = layout.restPositions()[i];
            float dx = rest.x()
                    - localPoint.x();
            float dy = rest.y()
                    - localPoint.y();
            float dz = rest.z()
                    - localPoint.z();
            float distanceSquared = dx * dx
                    + dy * dy
                    + dz * dz;

            /*
             * Keep the same Gaussian-style interaction falloff as the
             * existing engines.
             */
            float weight = (float) Math.exp(
                    -distanceSquared * IMPULSE_FALLOFF);
            if (weight < IMPULSE_CUTOFF) {
                continue;
            }

            impulseX[i] += force.x() * weight;
            impulseY[i] += force.y() * weight;
            impulseZ[i] += force.z() * weight;
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Geometry / volume helpers
     * -------------------------------------------------------------------------
     */

    private int[][] buildVolumeCells() {
        int count = (SoftbodyGridLayout.COLS - 1)
                * (SoftbodyGridLayout.ROWS - 1)
                * (SoftbodyGridLayout.LAYERS - 1);
        int[][] cells = new int[count][8];
        int cell = 0;

        for (int z = 0; z < SoftbodyGridLayout.LAYERS - 1; z++) {
            for (int y = 0; y < SoftbodyGridLayout.ROWS - 1; y++) {
                for (int x = 0; x < SoftbodyGridLayout.COLS - 1; x++) {
                    cells[cell++] = new int[] {
                            SoftbodyGridLayout.index(
                                    x, y, z),
                            SoftbodyGridLayout.index(
                                    x + 1, y, z),
                            SoftbodyGridLayout.index(
                                    x + 1, y + 1, z),
                            SoftbodyGridLayout.index(
                                    x, y + 1, z),
                            SoftbodyGridLayout.index(
                                    x, y, z + 1),
                            SoftbodyGridLayout.index(
                                    x + 1, y, z + 1),
                            SoftbodyGridLayout.index(
                                    x + 1, y + 1, z + 1),
                            SoftbodyGridLayout.index(
                                    x, y + 1, z + 1)
                    };
                }
            }
        }
        return cells;
    }

    private float totalVolume(
            Vec3[] positions) {
        float[] x = new float[positions.length];
        float[] y = new float[positions.length];
        float[] z = new float[positions.length];

        for (int i = 0; i < positions.length; i++) {
            x[i] = positions[i].x();
            y[i] = positions[i].y();
            z[i] = positions[i].z();
        }
        return totalVolume(
                x,
                y,
                z);
    }

    private float totalVolume(
            float[] x,
            float[] y,
            float[] z) {
        float volume = 0.0f;

        for (int[] cell : volumeCells) {
            volume += tetrahedronVolume(
                    cell[0],
                    cell[1],
                    cell[3],
                    cell[4],
                    x,
                    y,
                    z);
            volume += tetrahedronVolume(
                    cell[1],
                    cell[2],
                    cell[3],
                    cell[6],
                    x,
                    y,
                    z);
            volume += tetrahedronVolume(
                    cell[1],
                    cell[3],
                    cell[4],
                    cell[6],
                    x,
                    y,
                    z);
            volume += tetrahedronVolume(
                    cell[1],
                    cell[4],
                    cell[5],
                    cell[6],
                    x,
                    y,
                    z);
            volume += tetrahedronVolume(
                    cell[3],
                    cell[4],
                    cell[6],
                    cell[7],
                    x,
                    y,
                    z);
        }
        return volume;
    }

    private static float tetrahedronVolume(
            int a,
            int b,
            int c,
            int d,
            float[] x,
            float[] y,
            float[] z) {
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
        return Math.abs(determinant)
                / 6.0f;
    }

    /*
     * -------------------------------------------------------------------------
     * Utility helpers
     * -------------------------------------------------------------------------
     */
    private float depthResponse(int index) {
        float depth = SoftbodyGridLayout.PHYSICS_DEPTH;
        if (Math.abs(depth) < GEOMETRY_EPSILON) {
            return 1.0f;
        }
        Vec3 rest = layout.restPositions()[index];
        return Mth.clamp(-rest.z() / depth, 0.0f, 1.0f);
    }

    private float inverseMass(
            int index) {
        float mass = nodeMass[index];
        if (mass <= 0.0f
                || !Float.isFinite(mass)
                || mass == Float.MAX_VALUE) {
            return 0.0f;
        }
        return 1.0f / mass;
    }

    private void clearForceBuffers() {
        Arrays.fill(
                forceX,
                0.0f);
        Arrays.fill(
                forceY,
                0.0f);
        Arrays.fill(
                forceZ,
                0.0f);
    }

    private void clearImpulseBuffers() {
        Arrays.fill(
                impulseX,
                0.0f);
        Arrays.fill(
                impulseY,
                0.0f);
        Arrays.fill(
                impulseZ,
                0.0f);
    }

    private float sanitizeDeltaTime(
            float deltaTime) {

        if (!Float.isFinite(deltaTime)
                || deltaTime <= 0.0f) {

            return 1.0f;
        }

        /*
         * Prevent a huge stalled-frame delta from injecting a massive amount
         * of physical energy in one call.
         */
        return Mth.clamp(
                deltaTime,
                DELTA_TIME_MIN,
                DELTA_TIME_MAX);
    }

    /*
     * -------------------------------------------------------------------------
     * IPhysicsEngine
     * -------------------------------------------------------------------------
     */

    @Override
    public int nodeCount() {
        return posX.length;
    }

    @Override
    public Vec3 nodeRestPosition(
            int index) {
        return layout.restPositions()[index];
    }

    @Override
    public Vec3 nodePosition(
            int index) {
        return new Vec3(
                posX[index],
                posY[index],
                posZ[index]);
    }

    @Override
    public Vec3 nodeVelocity(
            int index) {
        return new Vec3(
                velX[index],
                velY[index],
                velZ[index]);
    }

    @Override
    public boolean isNodeFixed(
            int index) {
        return layout.fixed()[index];
    }

    @Override
    public void interpolate(
            float partialTick) {

        float t = Mth.clamp(
                partialTick,
                0.0f,
                1.0f);

        for (int i = 0; i < posX.length; i++) {
            interpX[i] = lerp(
                    prevPosX[i],
                    posX[i],
                    t);
            interpY[i] = lerp(
                    prevPosY[i],
                    posY[i],
                    t);
            interpZ[i] = lerp(
                    prevPosZ[i],
                    posZ[i],
                    t);
        }
    }

    @Override
    public Vec3 interpolatedNodePosition(
            int index) {
        return new Vec3(
                interpX[index],
                interpY[index],
                interpZ[index]);
    }

    private static float lerp(
            float from,
            float to,
            float t) {
        return from
                + (to - from) * t;
    }
}
