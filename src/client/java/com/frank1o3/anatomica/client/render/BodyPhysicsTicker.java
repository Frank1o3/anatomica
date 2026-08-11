package com.frank1o3.anatomica.client.render;

import com.frank1o3.anatomica.client.config.BodyConfig;
import com.frank1o3.anatomica.client.data.EntityBodyData;
import com.frank1o3.anatomica.physics.LivingEntityLike;
import com.frank1o3.anatomica.client.physics.ClientLivingEntityAdapter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Advances every locally-visible player's {@link ClientBodyPhysics} by one tick, once
 * per client game tick. {@link BodyRenderLayer} only reads/interpolates the resulting
 * node state — the actual simulation step lives here, decoupled from rendering, so
 * physics stays correct at a fixed tick rate regardless of render framerate.
 *
 * <p>
 * Only players with an actual synced {@link com.frank1o3.anatomica.config.BodyConfig}
 * are ticked (via {@link EntityBodyData#has}) — a player nobody has ever synced a
 * config for costs nothing here.
 */
public final class BodyPhysicsTicker {

    private static final float FIXED_DELTA_TIME = 1.0f / 20.0f;

    private BodyPhysicsTicker() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(BodyPhysicsTicker::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        if (client.level == null) {
            return;
        }
        for (Player player : client.level.players()) {
            var uuid = player.getUUID();
            if (!EntityBodyData.has(uuid)) {
                continue;
            }
            BodyConfig config = EntityBodyData.get(uuid);
            LivingEntityLike adapter = new ClientLivingEntityAdapter(player);
            ClientBodyPhysics.get(uuid).tick(FIXED_DELTA_TIME, adapter, config);
        }
    }
}
