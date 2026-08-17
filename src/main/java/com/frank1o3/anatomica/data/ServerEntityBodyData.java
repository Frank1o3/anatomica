package com.frank1o3.anatomica.data;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.networking.BodySyncData;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Dedicated-server-safe storage for body data.
 *
 * <p>The server stores the compact wire representation rather than a client
 * {@code BodyConfig}, so it can relay data without loading rendering, model, or
 * client configuration classes.</p>
 */
public final class ServerEntityBodyData extends SavedData implements IEntityBodyData {

    private static final Codec<ServerEntityBodyData> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC,
            BodySyncData.NBT_CODEC).xmap(ServerEntityBodyData::new, data -> data.data);
    private static final SavedDataType<ServerEntityBodyData> TYPE = new SavedDataType<>(
            Anatomica.id("body_data"), ServerEntityBodyData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<UUID, BodySyncData> data = new HashMap<>();

    public static ServerEntityBodyData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public ServerEntityBodyData() {
    }

    private ServerEntityBodyData(Map<UUID, BodySyncData> data) {
        this.data.putAll(data);
    }

    @Override
    public BodySyncData get(UUID uuid) {
        return data.get(uuid);
    }

    @Override
    public boolean has(UUID uuid) {
        return data.containsKey(uuid);
    }

    @Override
    public void put(UUID uuid, BodySyncData config) {
        data.put(uuid, config);
        setDirty();
    }

    @Override
    public void remove(UUID uuid) {
        if (data.remove(uuid) != null) {
            setDirty();
        }
    }

    @Override
    public void clear() {
        if (!data.isEmpty()) {
            data.clear();
            setDirty();
        }
    }
}
