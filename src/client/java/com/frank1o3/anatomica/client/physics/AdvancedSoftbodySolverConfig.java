package com.frank1o3.anatomica.client.physics;

/**
 * Shared tuning values for the advanced scalar and SIMD soft-body solvers.
 *
 * <p>
 * Keep topology dimensions in {@link SoftbodyGridLayout} and user-facing
 * material controls in {@code IBodyConfig}; this class contains only internal
 * numerical solver policy.
 * </p>
 */
public final class AdvancedSoftbodySolverConfig {

    private AdvancedSoftbodySolverConfig() {
    }

    public static final int PHYSICS_ITERATIONS = 10;
    public static final float REFERENCE_TICK_DELTA = 1.0f / 20.0f;
    public static final float SPRING_ITERATION_RATIO = 0.7f;

    public static final float SPRING_STIFFNESS_BASE = 1.5f;
    public static final float SPRING_DAMPING_BASE = 0.7f;
    public static final float SPRING_STIFFNESS_SOFTNESS_SCALE = 0.8f;
    public static final float SPRING_DAMPING_SOFTNESS_SCALE = 0.6f;
    public static final float SPRING_STIFFNESS_SOFTNESS_BASE = 1.4f;
    public static final float SPRING_DAMPING_SOFTNESS_BASE = 1.2f;
    public static final float SPRING_STIFFNESS_MIN = 0.15f;
    public static final float SPRING_STIFFNESS_MAX = 2.0f;
    public static final float SPRING_DAMPING_MIN = 0.25f;
    public static final float SPRING_DAMPING_MAX = 1.5f;
    public static final float INITIAL_DYNAMIC_DAMPING = 0.98f;
    public static final float MIN_DYNAMIC_DAMPING = 0.70f;
    public static final float MAX_DYNAMIC_DAMPING = 1.0f;

    public static final float REACTIVITY_SCALE = 1.0f;
    public static final float MAX_VERTICAL_MOTION = 0.6f;
    public static final float VERTICAL_MOTION_RESPONSE = 1.0f;
    /**
     * Sign applied to vertical inertial response; positive is the corrected
     * direction.
     */
    public static final float VERTICAL_RESPONSE_SIGN = 1.0f;
    public static final float GRAVITY_ACCEL = 0.028f;
    public static final float BUOYANCY_FACTOR = 0.35f;
    public static final float MAX_VELOCITY = 0.05f;
    public static final float VELOCITY_EPSILON = 1.0e-6f;
    public static final float GEOMETRY_EPSILON = 1.0e-7f;
    public static final float NODE_MASS_BASE = 0.8f;
    public static final float NODE_MASS_DEPTH_SCALE = 0.4f;

    public static final float ACCELERATION_FILTER = 0.35f;
    public static final float BOUNCE_BASE = 0.4f;
    public static final float BOUNCE_SCALE = 1.6f;
    public static final float FORWARD_REACTION_SCALE = 2.0f;
    public static final float YAW_THRESHOLD = 1.0e-5f;
    public static final float YAW_RESPONSE_SCALE = 0.01f;
    public static final float POSE_IMPULSE_SCALE = 0.15f;
    public static final float SWING_IMPULSE_SCALE = 0.5f;
    public static final float SWING_IMPULSE_DIVISOR = 1.0f;
    public static final float PASSENGER_FORCE_SCALE = 0.5f;
    public static final float SPECIAL_MOVEMENT_FORCE_SCALE = 0.4f;

    public static final float PBD_COMPLIANCE_BASE = 0.01f;
    public static final float PBD_COMPLIANCE_SOFTNESS = 0.08f;
    public static final float PBD_DISTANCE_EPSILON = 1.0e-6f;
    public static final float PBD_DENOMINATOR_EPSILON = 1.0e-8f;

    public static final float VOLUME_MIN_SCALE = 0.78f;
    public static final float VOLUME_MAX_SCALE = 1.22f;
    public static final float VOLUME_NEUTRAL_SETTING = 1.0f;
    /** Maximum volume setting, where 10.0 represents 1000% in the UI. */
    public static final float VOLUME_SETTING_MAX = 10.0f;
    public static final float VOLUME_ERROR_LIMIT = 0.25f;
    public static final float PRESSURE_BASE = 0.18f;
    public static final float PRESSURE_SCALE = 0.22f;
    public static final float VOLUME_CORRECTION_LIMIT = 0.08f;

    public static final float RECONSTRUCTION_BLEND = 0.35f;
    public static final float ACTIVITY_BLEND = 0.15f;
    public static final float DEFORMATION_NORMALIZER = 0.05f;
    public static final float ADAPTIVE_ACTIVITY_SCALE = 0.75f;
    public static final float ADAPTIVE_BASELINE_MIN = 0.90f;
    public static final float ADAPTIVE_BASELINE_MAX = 0.98f;
    public static final float ADAPTATION_RATE = 0.08f;

    public static final float LATERAL_BOUND_SCALE = 0.55f;
    public static final float VERTICAL_BOUND_SCALE = 0.55f;
    public static final float MIN_DEPTH_SCALE = 1.75f;
    public static final float MAX_DEPTH_SCALE = 0.81f;
    public static final float POSITIVE_DEPTH_MIN = 0.25f;
    public static final float DELTA_TIME_MIN = 0.01f;
    public static final float DELTA_TIME_MAX = 2.0f;
    public static final float IMPULSE_FALLOFF = 40.0f;
    public static final float IMPULSE_CUTOFF = 0.01f;

    /**
     * Converts the UI setting (1.0 = 100%) into a bounded physical volume
     * target. The UI may reach 10.0 (1000%), but the mesh remains within the
     * solver's stable target range.
     */
    public static float volumeTargetScale(float setting) {
        float value = Math.max(0.0f, Math.min(VOLUME_SETTING_MAX, setting));
        if (value <= VOLUME_NEUTRAL_SETTING) {
            return VOLUME_MIN_SCALE
                    + (1.0f - VOLUME_MIN_SCALE)
                            * (value / VOLUME_NEUTRAL_SETTING);
        }
        return 1.0f
                + (VOLUME_MAX_SCALE - 1.0f)
                        * ((value - VOLUME_NEUTRAL_SETTING)
                                / (VOLUME_SETTING_MAX - VOLUME_NEUTRAL_SETTING));
    }
}
