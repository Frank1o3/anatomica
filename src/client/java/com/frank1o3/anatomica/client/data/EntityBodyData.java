package com.frank1o3.anatomica.client.data;

import com.frank1o3.anatomica.client.config.BodyConfig;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.Duration;
import java.util.UUID;

public final class EntityBodyData {

    // 1. Create a Singleton instance so the rest of your mod can access these
    // instance methods
    public static final EntityBodyData INSTANCE = new EntityBodyData();

    private static final Duration EXPIRE_AFTER_ACCESS = Duration.ofMinutes(30);

    private static final LoadingCache<UUID, BodyConfig> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(EXPIRE_AFTER_ACCESS)
            .build(CacheLoader.from(BodyConfig::new));

    private EntityBodyData() {
    }

    public BodyConfig get(UUID uuid) {
        return CACHE.getUnchecked(uuid);
    }

    public boolean has(UUID uuid) {
        return CACHE.asMap().containsKey(uuid);
    }

    public void put(UUID uuid, BodyConfig config) {
        CACHE.put(uuid, config);
    }

    public void remove(UUID uuid) {
        CACHE.invalidate(uuid);
    }

    public void clear() {
        CACHE.invalidateAll();
    }
}
