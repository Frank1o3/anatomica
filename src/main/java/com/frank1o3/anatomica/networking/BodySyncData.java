package com.frank1o3.anatomica.networking;

import com.frank1o3.anatomica.uv.UVDirection;
import com.frank1o3.anatomica.uv.UVLayout;
import com.frank1o3.anatomica.uv.UVQuad;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Dedicated-server-safe, compact representation of the values a remote client
 * needs to render a body. This deliberately has no NBT or client-config
 * dependency: the on-wire form is fixed-width where possible and UV
 * coordinates occupy one byte (their valid range is 0..64).
 */
public record BodySyncData(
        boolean breastsEnabled, float size, float petite, float offsetX, float offsetY, float offsetZ,
        UVLayout leftUvLayout, UVLayout rightUvLayout, float spread, float cleavage,
        boolean independentSides, boolean physicsEnabled, float bounceStrength, float softness,
        String physicsEngineId, String modelId, boolean showInArmor) {

    private static final int MAX_IDENTIFIER_LENGTH = 128;

    public static void encode(RegistryFriendlyByteBuf buffer, BodySyncData data) {
        int flags = (data.breastsEnabled ? 1 : 0)
                | (data.independentSides ? 1 << 1 : 0)
                | (data.physicsEnabled ? 1 << 2 : 0)
                | (data.showInArmor ? 1 << 3 : 0);
        buffer.writeByte(flags);
        writeUnit(buffer, data.size);
        writeUnit(buffer, data.petite);
        writeOffset(buffer, data.offsetX);
        writeOffset(buffer, data.offsetY);
        writeOffset(buffer, data.offsetZ);
        writeLayout(buffer, data.leftUvLayout);
        writeLayout(buffer, data.rightUvLayout);
        writeSmallUnit(buffer, data.spread);
        writeSmallUnit(buffer, data.cleavage);
        writeUnit(buffer, data.bounceStrength);
        writeUnit(buffer, data.softness);
        buffer.writeUtf(data.physicsEngineId, MAX_IDENTIFIER_LENGTH);
        buffer.writeUtf(data.modelId, MAX_IDENTIFIER_LENGTH);
    }

    public static BodySyncData decode(RegistryFriendlyByteBuf buffer) {
        int flags = buffer.readUnsignedByte();
        float size = readUnit(buffer);
        float petite = readUnit(buffer);
        float offsetX = readOffset(buffer);
        float offsetY = readOffset(buffer);
        float offsetZ = readOffset(buffer);
        UVLayout left = readLayout(buffer);
        UVLayout right = readLayout(buffer);
        float spread = readSmallUnit(buffer);
        float cleavage = readSmallUnit(buffer);
        float bounce = readUnit(buffer);
        float softness = readUnit(buffer);
        String physicsEngine = buffer.readUtf(MAX_IDENTIFIER_LENGTH);
        String model = buffer.readUtf(MAX_IDENTIFIER_LENGTH);
        return new BodySyncData((flags & 1) != 0, size, petite, offsetX, offsetY, offsetZ, left, right,
                spread, cleavage, (flags & (1 << 1)) != 0, (flags & (1 << 2)) != 0, bounce, softness,
                physicsEngine, model, (flags & (1 << 3)) != 0);
    }

    private static void writeUnit(RegistryFriendlyByteBuf buffer, float value) {
        buffer.writeShort(Math.round(Math.clamp(value, 0.0F, 1.0F) * 65535.0F));
    }

    private static float readUnit(RegistryFriendlyByteBuf buffer) {
        return buffer.readUnsignedShort() / 65535.0F;
    }

    private static void writeOffset(RegistryFriendlyByteBuf buffer, float value) {
        buffer.writeShort(Math.round((Math.clamp(value, -0.5F, 0.5F) + 0.5F) * 65535.0F));
    }

    private static float readOffset(RegistryFriendlyByteBuf buffer) {
        return buffer.readUnsignedShort() / 65535.0F - 0.5F;
    }

    private static void writeSmallUnit(RegistryFriendlyByteBuf buffer, float value) {
        buffer.writeShort(Math.round(Math.clamp(value, 0.0F, 0.1F) * 655350.0F));
    }

    private static float readSmallUnit(RegistryFriendlyByteBuf buffer) {
        return buffer.readUnsignedShort() / 655350.0F;
    }

    private static void writeLayout(RegistryFriendlyByteBuf buffer, UVLayout layout) {
        for (UVDirection direction : UVDirection.values()) {
            UVQuad quad = layout.get(direction);
            buffer.writeBoolean(quad != null);
            if (quad != null) {
                buffer.writeByte(quad.x1()); buffer.writeByte(quad.y1());
                buffer.writeByte(quad.x2()); buffer.writeByte(quad.y2());
            }
        }
    }

    private static UVLayout readLayout(RegistryFriendlyByteBuf buffer) {
        UVLayout layout = new UVLayout();
        for (UVDirection direction : UVDirection.values()) {
            if (buffer.readBoolean()) {
                layout.put(direction, new UVQuad(buffer.readUnsignedByte(), buffer.readUnsignedByte(),
                        buffer.readUnsignedByte(), buffer.readUnsignedByte()));
            }
        }
        return layout;
    }
}
