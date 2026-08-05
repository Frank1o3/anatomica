package com.frank1o3.anatomica.physics;

import com.frank1o3.franklylib.Vec3;

/**
 * The subset of a living entity's state a physics engine actually needs to
 * derive
 * per-tick forces (motion delta, pose, swing, vehicle state, a stable random
 * source).
 * Kept as a narrow interface rather than depending on {@code LivingEntity}
 * directly so
 * {@link IPhysicsEngine} can stay common-side-referenceable and so physics code
 * is
 * testable without a running client.
 *
 * <p>
 * The real client-side implementation wraps a live
 * {@code LivingEntity}/{@code Player};
 * this file only declares the contract.
 */
public interface LivingEntityLike {

    Vec3 motionDelta();

    boolean isCrouching();

    boolean isSleeping();

    boolean isSwimming();

    boolean isFallFlying();

    boolean isPassenger();

    /**
     * Walk-cycle phase in {@code [0, 1)}, or {@code 0} if not currently walking.
     */
    float walkAnimationPhase();

    /** Arm-swing progress in {@code [0, 1]}, or {@code 0} if not swinging. */
    float attackSwingProgress(float partialTick);

    long randomSeed();
}
