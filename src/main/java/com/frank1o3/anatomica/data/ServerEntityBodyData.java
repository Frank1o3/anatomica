package com.frank1o3.anatomica.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.frank1o3.anatomica.networking.BodySyncData;

/**
 * Dedicated-server-safe storage for body data.
 *
 * <p>The server stores the compact wire representation rather than a client
 * {@code BodyConfig}, so it can relay data without loading rendering, model, or
 * client configuration classes.</p>
 */
public final class ServerEntityBodyData implements IEntityBodyData {

    private final Map<UUID, BodySyncData> data = new HashMap<>();

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
    }

    @Override
    public void remove(UUID uuid) {
        data.remove(uuid);
    }

    @Override
    public void clear() {
        data.clear();
    }
}
