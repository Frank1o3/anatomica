package com.frank1o3.anatomica.client.render;

import com.frank1o3.anatomica.data.EntityBodyData;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A decoupled snapshot carrying just enough per-frame identity onto a render
 * state
 * so {@link BodyRenderLayer#submit} — which only ever sees the render state,
 * never
 * the original entity — can still resolve which
 * {@link com.frank1o3.anatomica.config.BodyConfig}
 * applies. Same data-key-on-render-state pattern as {@code GenderRenderState}
 * in the
 * mod this was forked from.
 */
public final class BodyRenderState {
    private static final RenderStateDataKey<BodyRenderState> STATE = RenderStateDataKey
            .create(() -> "AnatomicaBodyRenderState");

    public final UUID uuid;
    public final boolean hasConfig;

    private BodyRenderState(UUID uuid, boolean hasConfig) {
        this.uuid = uuid;
        this.hasConfig = hasConfig;
    }

    public static void update(LivingEntity entity, EntityRenderState state) {
        if (!(entity instanceof Player player)) {
            return;
        }
        UUID uuid = player.getUUID();
        state.setData(STATE, new BodyRenderState(uuid, EntityBodyData.has(uuid)));
    }

    public static @Nullable BodyRenderState get(EntityRenderState state) {
        return state.getData(STATE);
    }
}