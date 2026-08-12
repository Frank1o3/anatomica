package com.frank1o3.anatomica.networking;

import com.frank1o3.anatomica.Anatomica;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * Carries one entity's compact body configuration to observing clients. The
 * common networking layer remains independent of client-only configuration
 * classes, so it is safe on dedicated servers.
 *
 * <p>
 * Sent client -> server when the local player changes their own config, and
 * relayed
 * server -> client to players tracking the changed entity (see
 * {@link AnatomicaNetworking}).
 */
public record BodySyncPacket(UUID uuid, BodySyncData data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BodySyncPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(Anatomica.MOD_ID, "body_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BodySyncPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, BodySyncPacket::uuid,
            StreamCodec.of(BodySyncData::encode, BodySyncData::decode), BodySyncPacket::data,
            BodySyncPacket::new);

    @Override
    public Type<BodySyncPacket> type() {
        return TYPE;
    }
}
