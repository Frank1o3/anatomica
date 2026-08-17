package com.frank1o3.anatomica.networking;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.data.ServerEntityBodyData;

import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AnatomicaNetworking {

    /** Minimum time between accepted uploads from a single player. */
    private static final long MIN_UPLOAD_INTERVAL_MS = 250;
    private static final Map<UUID, Long> LAST_UPLOAD_TIME = new ConcurrentHashMap<>();

    private AnatomicaNetworking() {
    }

    public static void registerCommon() {
        PayloadTypeRegistry.serverboundPlay().register(BodySyncUploadPacket.TYPE, BodySyncUploadPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BodySyncPacket.TYPE, BodySyncPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(BodySyncUploadPacket.TYPE, (payload, context) -> {
            ServerPlayer sender = context.player();
            UUID uuid = sender.getUUID();

            if (isRateLimited(uuid)) {
                Anatomica.LOGGER.debug(
                        "[Anatomica] Dropped body sync upload from {}: rate limit ({}ms).",
                        sender.getName().getString(), MIN_UPLOAD_INTERVAL_MS);
                return;
            }

            if (!isValid(payload.data())) {
                Anatomica.LOGGER.warn(
                        "[Anatomica] Dropped body sync upload from {}: failed validation.",
                        sender.getName().getString());
                return;
            }

            ServerEntityBodyData.get(sender.level().getServer()).put(uuid, payload.data());
            broadcastToTrackingPlayers(sender, new BodySyncPacket(uuid, payload.data()));
        });

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

    /**
     * Removes rate-limit state for a disconnected player. Call from your DISCONNECT
     * hook.
     */
    public static void removePlayerState(UUID uuid) {
        LAST_UPLOAD_TIME.remove(uuid);
    }

    private static boolean isRateLimited(UUID uuid) {
        long now = System.currentTimeMillis();
        Long last = LAST_UPLOAD_TIME.get(uuid);
        if (last != null && (now - last) < MIN_UPLOAD_INTERVAL_MS) {
            return true;
        }
        LAST_UPLOAD_TIME.put(uuid, now);
        return false;
    }

    /**
     * Belt-and-suspenders structural check. The wire format's own bounds already
     * prevent out-of-range numerics (see BodySyncData's fixed-point encoding), so
     * this exists mainly to reject unresolvable identifiers explicitly and log
     * them, rather than silently falling back to a registry default.
     */
    private static boolean isValid(BodySyncData data) {
        if (data.physicsEngineId() == null || data.physicsEngineId().isBlank()
                || data.modelId() == null || data.modelId().isBlank()) {
            return false;
        }
        return Identifier.tryParse(data.physicsEngineId()) != null
                && Identifier.tryParse(data.modelId()) != null;
    }

    private static void broadcastToTrackingPlayers(ServerPlayer sender, BodySyncPacket payload) {
        for (ServerPlayer tracking : PlayerLookup.tracking(sender)) {
            ServerPlayNetworking.send(tracking, payload);
        }
        ServerPlayNetworking.send(sender, payload);
    }

    private interface StartTrackingHandler {
        void onStartTracking(Entity trackedEntity, ServerPlayer observer);
    }

    private static void registerStartTrackingHook(StartTrackingHandler handler) {
        EntityTrackingEvents.START_TRACKING.register(
                (trackedEntity, observer) -> handler.onStartTracking(trackedEntity, observer));
    }
}