package com.frank1o3.anatomica.client.data;

import com.frank1o3.anatomica.client.config.BodyConfig;
import com.frank1o3.anatomica.config.IBodyConfig;
import com.frank1o3.anatomica.data.IEntityBodyData;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.Duration;
import java.util.UUID;

public final class EntityBodyData implements IEntityBodyData {

    // 1. Create a Singleton instance so the rest of your mod can access these
    // instance methods
    public static final EntityBodyData INSTANCE = new EntityBodyData();

    private static final Duration EXPIRE_AFTER_ACCESS = Duration.ofMinutes(30);

    private static final LoadingCache<UUID, BodyConfig> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(EXPIRE_AFTER_ACCESS)
            .build(CacheLoader.from(BodyConfig::new));

    private EntityBodyData() {
    }

    @Override
    public BodyConfig get(UUID uuid) {
        return CACHE.getUnchecked(uuid);
    }

    @Override
    public boolean has(UUID uuid) {
        return CACHE.asMap().containsKey(uuid);
    }

    // 2. Change parameter to IBodyConfig to match the interface exactly
    @Override
    public void put(UUID uuid, IBodyConfig config) {
        // 3. Safely cast to your specific client implementation before putting it in
        // the cache
        if (config instanceof BodyConfig) {
            CACHE.put(uuid, (BodyConfig) config);
        } else {
            throw new IllegalArgumentException("Client EntityBodyData only accepts BodyConfig instances");
        }
    }

    @Override
    public void remove(UUID uuid) {
        CACHE.invalidate(uuid);
    }

    @Override
    public void clear() {
        CACHE.invalidateAll();
    }
}