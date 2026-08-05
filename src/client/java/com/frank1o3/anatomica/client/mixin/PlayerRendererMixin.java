package com.frank1o3.anatomica.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frank1o3.anatomica.client.render.BodyRenderLayer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

@Mixin(PlayerRender.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void anatomica$addLayer(EntityRendererProvider.Context context,
            boolean slim,
            CallbackInfo ci) {
        PlayerRenderer self = (PlayerRenderer) (Object) this;
        self.addLayer(new BodyRenderLayer(self));
    }
}