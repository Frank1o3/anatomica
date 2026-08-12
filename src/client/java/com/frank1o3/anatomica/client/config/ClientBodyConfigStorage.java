package com.frank1o3.anatomica.client.config;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.franklylib.config.FranklyConfigHolder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Persists each local Minecraft profile's body configuration as JSON. */
public final class ClientBodyConfigStorage {

    private static final Path CONFIG_DIRECTORY = FabricLoader.getInstance().getConfigDir()
            .resolve(Anatomica.MOD_ID)
            .resolve("players");
    private static final Map<UUID, FranklyConfigHolder<BodyConfig>> HOLDERS = new HashMap<>();

    private ClientBodyConfigStorage() {
    }

    public static Optional<BodyConfig> load(UUID uuid) {
        Path jsonFile = jsonFileFor(uuid);
        Path legacyFile = legacyFileFor(uuid);
        if (Files.notExists(jsonFile) && Files.notExists(legacyFile)) {
            return Optional.empty();
        }
        FranklyConfigHolder<BodyConfig> holder = holderFor(uuid);
        if (Files.exists(jsonFile)) {
            return Optional.of(holder.get());
        }

        return loadLegacy(legacyFile).map(config -> {
            holder.update(ignored -> config);
            holder.save();
            return config;
        });
    }

    public static void save(UUID uuid, BodyConfig config) {
        FranklyConfigHolder<BodyConfig> holder = holderFor(uuid);
        holder.update(ignored -> config);
        holder.save();
    }

    /** Flushes profile files and releases their config-engine I/O workers. */
    public static void closeAll() {
        HOLDERS.values().forEach(FranklyConfigHolder::flushAndClose);
        HOLDERS.clear();
    }

    private static FranklyConfigHolder<BodyConfig> holderFor(UUID uuid) {
        return HOLDERS.computeIfAbsent(uuid, ignored -> FranklyConfigHolder
                .builder(BodyConfig.class, BodyConfig::new)
                .path(jsonFileFor(uuid))
                .autosaveTicks(0)
                .staleCheckTicks(0)
                .build());
    }

    private static Optional<BodyConfig> loadLegacy(Path file) {
        try {
            CompoundTag data = NbtIo.readCompressed(file, NbtAccounter.defaultQuota());
            return Optional.of(BodyConfig.fromNbt(data));
        } catch (IOException | RuntimeException exception) {
            Anatomica.LOGGER.error("Failed to migrate legacy body configuration from {}", file, exception);
            return Optional.empty();
        }
    }

    private static Path jsonFileFor(UUID uuid) {
        return CONFIG_DIRECTORY.resolve(uuid + ".json");
    }

    private static Path legacyFileFor(UUID uuid) {
        return CONFIG_DIRECTORY.resolve(uuid + ".nbt");
    }
}
