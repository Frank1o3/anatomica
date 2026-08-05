package com.frank1o3.anatomica.client.physics;

import com.frank1o3.anatomica.physics.LivingEntityLike;
import com.frank1o3.franklylib.Vec3;
import net.minecraft.world.entity.LivingEntity;

/**
 * Adapts a real {@link LivingEntity} to {@link LivingEntityLike}. This is the only
 * place vanilla entity state gets translated into what the physics engine actually
 * reads — keeps {@code SoftbodyPhysicsEngine} decoupled from the entity API surface.
 *
 * <p>
 * Note: {@code walkAnimation}/{@code getAttackAnim} field and method names are best-
 * effort against typical Yarn/Mojmap-style naming for this era of the game — verify
 * against your actual mappings (IDE autocomplete on {@code entity.} will show the real
 * names if these don't resolve) and adjust {@link #walkAnimationPhase()} /
 * {@link #attackSwingProgress(float)} accordingly.
 */
public final class ClientLivingEntityAdapter implements LivingEntityLike {

    private final LivingEntity entity;

    public ClientLivingEntityAdapter(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public Vec3 motionDelta() {
        net.minecraft.world.phys.Vec3 delta = entity.getDeltaMovement();
        return new Vec3((float) delta.x, (float) delta.y, (float) delta.z);
    }

    @Override
    public boolean isCrouching() {
        return entity.isCrouching();
    }

    @Override
    public boolean isSleeping() {
        return entity.isSleeping();
    }

    @Override
    public boolean isSwimming() {
        return entity.isSwimming();
    }

    @Override
    public boolean isFallFlying() {
        return entity.isFallFlying();
    }

    @Override
    public boolean isPassenger() {
        return entity.isPassenger();
    }

    @Override
    public float walkAnimationPhase() {
        return entity.walkAnimation.speed() > 0.01f
                ? (entity.walkAnimation.position() / 13.0f) % 1.0f
                : 0.0f;
    }

    @Override
    public float attackSwingProgress(float partialTick) {
        return entity.getAttackAnim(partialTick);
    }

    @Override
    public long randomSeed() {
        return entity.getUUID().getMostSignificantBits();
    }
}
