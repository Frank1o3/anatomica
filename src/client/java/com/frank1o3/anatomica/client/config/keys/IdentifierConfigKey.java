package com.frank1o3.anatomica.client.config.keys;

import com.frank1o3.anatomica.client.config.BodyConfigKey;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

/**
 * An {@link Identifier}-valued key whose value is validated against a registry
 * at
 * read-time (e.g. a selected physics engine or model id). An id that doesn't
 * resolve
 * in the registry — because the mod that registered it isn't installed, or the
 * saved
 * data is stale/corrupted — silently falls back to {@link #defaultValue()}
 * rather than
 * throwing, so a missing optional dependency degrades gracefully instead of
 * crashing
 * config load.
 */
public final class IdentifierConfigKey extends BodyConfigKey<Identifier> {

    private final Supplier<Registry<?>> registry;

    public IdentifierConfigKey(String id, Identifier defaultValue, Supplier<Registry<?>> registry) {
        super(id, defaultValue);
        this.registry = registry;
    }

    @Override
    public Identifier clamp(Identifier value) {
        if (value == null) {
            return defaultValue();
        }
        Registry<?> reg = registry.get();
        if (reg != null && !reg.containsKey(value)) {
            return defaultValue();
        }
        return value;
    }

    @Override
    public void write(CompoundTag tag, Identifier value) {
        tag.putString(id(), clamp(value).toString());
    }

    @Override
    public Identifier read(CompoundTag tag) {
        String raw = tag.getStringOr(id(), null);
        if (raw == null) {
            return defaultValue();
        }
        Identifier parsed = Identifier.tryParse(raw);
        return clamp(parsed);
    }
}
