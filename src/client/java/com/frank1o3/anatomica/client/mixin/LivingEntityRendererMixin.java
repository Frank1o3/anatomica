package com.frank1o3.anatomica.client.mixin;

import com.frank1o3.anatomica.client.render.BodyRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the player's UUID onto their render state each frame, since
 * {@link com.frank1o3.anatomica.client.render.BodyRenderLayer#submit} only ever
 * sees
 * the render state, never the original entity.
 */
@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void anatomica$captureBodyRenderState(LivingEntity entity, LivingEntityRenderState state,
            float tickDelta, CallbackInfo ci) {
        BodyRenderState.update(entity, state);
    }
}