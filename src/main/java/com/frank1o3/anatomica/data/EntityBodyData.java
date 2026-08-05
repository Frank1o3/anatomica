package com.frank1o3.anatomica.data;

import com.frank1o3.anatomica.config.BodyConfig;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.Duration;
import java.util.UUID;

/**
 * UUID -> {@link BodyConfig} cache, common-side.
 *
 * <p>
 * On the server this is the authoritative store: populated as sync packets
 * arrive from
 * clients, read when relaying to newly-tracking players. On the client this
 * holds both
 * the local player's own working config and cached copies received for other
 * tracked
 * players.
 *
 * <p>
 * Entries expire after a period of disuse rather than being manually removed on
 * disconnect, so a player briefly leaving tracking range and returning doesn't
 * need a
 * re-sync, but a long-gone entity doesn't leak memory indefinitely.
 */
public final class EntityBodyData {

    private static final Duration EXPIRE_AFTER_ACCESS = Duration.ofMinutes(30);

    private static final LoadingCache<UUID, BodyConfig> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(EXPIRE_AFTER_ACCESS)
            .build(CacheLoader.from(BodyConfig::new));

    private EntityBodyData() {
    }

    /**
     * Returns the config for {@code uuid}, creating a default one if none is cached
     * yet.
     */
    public static BodyConfig get(UUID uuid) {
        return CACHE.getUnchecked(uuid);
    }

    /**
     * Whether a config has actually been set for {@code uuid} (vs. just defaulted
     * by {@link #get}).
     */
    public static boolean has(UUID uuid) {
        return CACHE.asMap().containsKey(uuid);
    }

    public static void put(UUID uuid, BodyConfig config) {
        CACHE.put(uuid, config);
    }

    public static void remove(UUID uuid) {
        CACHE.invalidate(uuid);
    }

    public static void clear() {
        CACHE.invalidateAll();
    }
}
