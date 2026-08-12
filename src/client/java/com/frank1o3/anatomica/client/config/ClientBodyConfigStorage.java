package com.frank1o3.anatomica.client.config;

import com.frank1o3.anatomica.Anatomica;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

/** Persists each local Minecraft profile's body configuration under the mod config directory. */
public final class ClientBodyConfigStorage {

    private static final Path CONFIG_DIRECTORY = FabricLoader.getInstance().getConfigDir()
            .resolve(Anatomica.MOD_ID)
            .resolve("players");

    private ClientBodyConfigStorage() {
    }

    public static Optional<BodyConfig> load(UUID uuid) {
        Path file = fileFor(uuid);
        if (Files.notExists(file)) {
            return Optional.empty();
        }

        try {
            CompoundTag data = NbtIo.readCompressed(file, NbtAccounter.defaultQuota());
            return Optional.of(BodyConfig.fromNbt(data));
        } catch (IOException | RuntimeException exception) {
            Anatomica.LOGGER.error("Failed to load body configuration from {}", file, exception);
            return Optional.empty();
        }
    }

    public static void save(UUID uuid, BodyConfig config) {
        Path file = fileFor(uuid);
        Path temporaryFile = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(CONFIG_DIRECTORY);
            NbtIo.writeCompressed(config.toNbt(), temporaryFile);
            try {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            Anatomica.LOGGER.error("Failed to save body configuration to {}", file, exception);
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException cleanupFailure) {
                Anatomica.LOGGER.warn("Failed to remove incomplete body configuration {}", temporaryFile,
                        cleanupFailure);
            }
        }
    }

    private static Path fileFor(UUID uuid) {
        return CONFIG_DIRECTORY.resolve(uuid + ".nbt");
    }
}
