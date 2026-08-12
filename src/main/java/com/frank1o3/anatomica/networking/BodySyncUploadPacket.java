package com.frank1o3.anatomica.networking;

import com.frank1o3.anatomica.Anatomica;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client-to-server upload. The server derives the owner UUID from the connection. */
public record BodySyncUploadPacket(BodySyncData data) implements CustomPacketPayload {
    public static final Type<BodySyncUploadPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(Anatomica.MOD_ID, "body_sync_upload"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BodySyncUploadPacket> CODEC = StreamCodec.of(
            (buffer, packet) -> BodySyncData.encode(buffer, packet.data),
            buffer -> new BodySyncUploadPacket(BodySyncData.decode(buffer)));
    @Override public Type<BodySyncUploadPacket> type() { return TYPE; }
}
