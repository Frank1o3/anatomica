package com.frank1o3.anatomica.networking;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.client.config.BodyConfig;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * Carries one entity's {@link BodyConfig}, serialized through the same NBT path
 * used
 * for disk persistence ({@link BodyConfig#toNbt()} /
 * {@link BodyConfig#fromNbt}) —
 * there is exactly one serialization format for this data, used for both disk
 * and
 * network, rather than a hand-maintained parallel wire format that could drift
 * out of
 * sync with the disk format over time.
 *
 * <p>
 * Sent client -> server when the local player changes their own config, and
 * relayed
 * server -> client to players tracking the changed entity (see
 * {@link AnatomicaNetworking}).
 */
public record BodySyncPacket(UUID uuid, CompoundTag data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BodySyncPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(Anatomica.MOD_ID, "body_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BodySyncPacket> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, BodySyncPacket::uuid,
            ByteBufCodecs.COMPOUND_TAG, BodySyncPacket::data,
            BodySyncPacket::new);

    public static BodySyncPacket of(UUID uuid, BodyConfig config) {
        return new BodySyncPacket(uuid, config.toNbt());
    }

    public BodyConfig toConfig() {
        return BodyConfig.fromNbt(data);
    }

    @Override
    public Type<BodySyncPacket> type() {
        return TYPE;
    }
}
