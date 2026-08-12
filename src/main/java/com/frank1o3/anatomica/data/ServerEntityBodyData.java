package com.frank1o3.anatomica.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;

/**
 * Dedicated-server-safe storage for body data.
 *
 * <p>The server deliberately stores the wire representation rather than a
 * client {@code BodyConfig}. It can therefore validate ownership and relay the
 * data without loading any rendering, model, or client configuration classes.</p>
 */
public final class ServerEntityBodyData implements IEntityBodyData {

    private final Map<UUID, CompoundTag> data = new HashMap<>();

    @Override
    public CompoundTag get(UUID uuid) {
        CompoundTag config = data.get(uuid);
        return config == null ? null : config.copy();
    }

    @Override
    public boolean has(UUID uuid) {
        return data.containsKey(uuid);
    }

    @Override
    public void put(UUID uuid, CompoundTag config) {
        data.put(uuid, config.copy());
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
