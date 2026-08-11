package com.frank1o3.anatomica.data;

import java.util.UUID;

import com.frank1o3.anatomica.config.IBodyConfig;

/**
 * * Common contract for storage of per-entity Anatomica body configurations. *
 * *
 * <p>
 * The concrete implementation may use a cache, map, or another storage *
 * mechanism. Common code only depends on this interface.
 * </p>
 */
public interface IEntityBodyData {
    /**
     * * Returns the configuration for an entity. * *
     * <p>
     * The implementation may create and return a default configuration when * no
     * configuration has previously been stored.
     * </p>
     */
    IBodyConfig get(UUID uuid);

    /**
     * * Returns whether an explicitly stored configuration exists for the entity.
     */
    boolean has(UUID uuid);

    /** * Stores a configuration for an entity. */
    void put(UUID uuid, IBodyConfig config);

    /** * Removes the configuration for an entity. */
    void remove(UUID uuid);

    /** * Removes all stored configurations. */
    void clear();
}