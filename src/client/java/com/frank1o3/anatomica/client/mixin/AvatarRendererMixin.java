package com.frank1o3.anatomica.client.mixin;

import com.frank1o3.anatomica.client.render.BodyRenderLayer;
import com.frank1o3.anatomica.client.render.BodyArmorRenderLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registers {@link BodyRenderLayer} onto every {@link AvatarRenderer} right
 * after
 * vanilla finishes constructing it.
 *
 * <p>
 * The private constructor below only exists to satisfy the compiler for
 * extending
 * {@link LivingEntityRenderer} — Mixin discards it and merges this class's
 * members
 * directly onto {@link AvatarRenderer}, so {@code this} inside
 * {@link #anatomica$addBodyLayer} really is the renderer instance being
 * constructed,
 * and {@code addLayer} (protected on the vanilla superclass) is reachable
 * directly —
 * no accessor mixin or access widener needed.
 */
@Mixin(AvatarRenderer.class)
abstract class AvatarRendererMixin
        extends LivingEntityRenderer<Avatar, AvatarRenderState, HumanoidModel<AvatarRenderState>> {

    private AvatarRendererMixin(EntityRendererProvider.Context ctx, HumanoidModel<AvatarRenderState> model,
            float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void anatomica$addBodyLayer(EntityRendererProvider.Context ctx, boolean slim, CallbackInfo ci) {
        this.addLayer(new BodyRenderLayer<>(this));
        this.addLayer(new BodyArmorRenderLayer<>(this, ctx.getEquipmentAssets()));
    }
}
