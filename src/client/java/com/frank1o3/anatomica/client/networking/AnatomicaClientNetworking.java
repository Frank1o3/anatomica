package com.frank1o3.anatomica.client.networking;

import com.frank1o3.anatomica.client.data.EntityBodyData;
import com.frank1o3.anatomica.config.IBodyConfig;
import com.frank1o3.anatomica.config.Services;
import com.frank1o3.anatomica.networking.BodySyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import java.util.UUID;

/**
 * Client-side half of {@link BodySyncPacket}: receives the server's relayed
 * configs
 * for tracked players into {@link EntityBodyData}, and sends the local player's
 * own
 * config up to the server whenever the GUI commits a change.
 */
public final class AnatomicaClientNetworking {

    private AnatomicaClientNetworking() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(BodySyncPacket.TYPE, (payload, context) -> {
            Services.entityBodyData().put(payload.uuid(), payload.toConfig());
        });
    }

    /**
     * Sends the local player's current config to the server. Call this from the
     * customization screen on slider release / screen close — not on every single
     * per-frame value change, to avoid flooding the connection while a slider is
     * being dragged.
     */
    public static void sendLocalConfig(IBodyConfig config) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        UUID uuid = client.player.getUUID();
        Services.entityBodyData().put(uuid, config); // update locally immediately, don't wait for the round trip
        ClientPlayNetworking.send(BodySyncPacket.of(uuid, config));
    }
}
