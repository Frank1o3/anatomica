package com.frank1o3.anatomica.networking;

import com.frank1o3.anatomica.data.ServerEntityBodyData;

import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Registers {@link BodySyncPacket}'s payload type for both directions and wires
 * up the
 * server-side relay: a client's own sync is validated (sender must own the UUID
 * they're
 * syncing), stored into {@link IEntityBodyData}, then re-broadcast to every
 * player
 * currently tracking that entity. Newly-tracking players get one immediate sync
 * of the
 * tracked player's current config rather than waiting for that player's next
 * change.
 *
 * <p>
 * There is no handshake/version packet and no authentication beyond "the UUID
 * in the
 * packet must equal the sender's own UUID" — no accounts, no cloud service,
 * nothing
 * beyond what vanilla server-authoritative multiplayer already provides.
 *
 * <p>
 * Note: exact Fabric API package paths (event classes in particular) can shift
 * between
 * Minecraft/Fabric API versions — verify these imports resolve against the
 * Fabric API
 * version pinned in {@code gradle.properties} and adjust the event hook lookups
 * (tracking-start equivalent) to whatever that version exposes if they've
 * moved.
 */
public final class AnatomicaNetworking {

    private AnatomicaNetworking() {
    }

    public static void registerCommon() {
        PayloadTypeRegistry.serverboundPlay().register(BodySyncUploadPacket.TYPE, BodySyncUploadPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BodySyncPacket.TYPE, BodySyncPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(BodySyncUploadPacket.TYPE, (payload, context) -> {
            ServerPlayer sender = context.player();
            ServerEntityBodyData.get(sender.level().getServer()).put(sender.getUUID(), payload.data());
            broadcastToTrackingPlayers(sender, new BodySyncPacket(sender.getUUID(), payload.data()));
        });

        // Fires when a player enters tracking range of another entity. If we already
        // have a config cached for the newly-visible entity, send it once immediately
        // so the newly-tracking player doesn't have to wait for the tracked player's
        // next change.
        registerStartTrackingHook((trackedEntity, observer) -> {
            if (!(trackedEntity instanceof ServerPlayer trackedPlayer)) {
                return;
            }
            UUID uuid = trackedPlayer.getUUID();
            ServerEntityBodyData data = ServerEntityBodyData.get(trackedPlayer.level().getServer());
            if (data.has(uuid)) {
                ServerPlayNetworking.send(observer, new BodySyncPacket(uuid, data.get(uuid)));
            }
        });
    }

    private static void broadcastToTrackingPlayers(ServerPlayer sender, BodySyncPacket payload) {
        for (ServerPlayer tracking : PlayerLookup.tracking(sender)) {
            ServerPlayNetworking.send(tracking, payload);
        }
        // Also send back to the sender's own other clients / the sender itself, so a
        // player who edits their own config sees it echoed the same way anyone else
        // tracking them would.
        ServerPlayNetworking.send(sender, payload);
    }

    /**
     * Thin seam around Fabric API's tracking-start event so the exact event class
     * can
     * be swapped in one place if it differs from what's assumed here. Wire this up
     * to
     * {@code net.fabricmc.fabric.api.entity.event.v1.EntityTrackingEvents.START_TRACKING}
     * (or whatever the equivalent is in the Fabric API version this project pins).
     */
    private interface StartTrackingHandler {
        void onStartTracking(net.minecraft.world.entity.Entity trackedEntity, ServerPlayer observer);
    }

    private static void registerStartTrackingHook(StartTrackingHandler handler) {
        EntityTrackingEvents.START_TRACKING.register(
                (trackedEntity, observer) -> handler.onStartTracking(trackedEntity, observer));
    }
}
