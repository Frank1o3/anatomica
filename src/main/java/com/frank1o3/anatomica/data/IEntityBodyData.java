package com.frank1o3.anatomica.data;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;

/**
 * Common contract for server storage of per-entity Anatomica body data.
 * *
 * <p>
 * The concrete implementation may use a cache, map, or another storage *
 * mechanism. Common code only depends on this interface.
 * </p>
 */
public interface IEntityBodyData {
    /**
     * Returns a copy of the serialized configuration for an entity, or {@code null}
     * when that entity has not sent one yet.
     */
    CompoundTag get(UUID uuid);

    /**
     * * Returns whether an explicitly stored configuration exists for the entity.
     */
    boolean has(UUID uuid);

    /** Stores a serialized configuration for an entity. */
    void put(UUID uuid, CompoundTag config);

    /** * Removes the configuration for an entity. */
    void remove(UUID uuid);

    /** * Removes all stored configurations. */
    void clear();
}
