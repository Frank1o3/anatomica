package com.frank1o3.anatomica.config;

import net.minecraft.nbt.CompoundTag;

/**
 * A single named, typed, validated config value.
 *
 * <p>
 * Each key knows how to clamp/validate a raw value and how to read/write itself
 * to/from a {@link CompoundTag}. {@link AnatomicaConfig} keeps a table of these
 * paired with getter/setter accessors into {@link BodyConfig}, so both disk
 * persistence and network serialization can walk the same list instead of every
 * call site hand-writing per-field NBT code.
 *
 * @param <T> the value type this key holds
 */
public abstract class BodyConfigKey<T> {

    private final String id;
    private final T defaultValue;

    protected BodyConfigKey(String id, T defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
    }

    /**
     * Short, stable key name used as the NBT tag name. Do not rename after release.
     */
    public final String id() {
        return id;
    }

    public final T defaultValue() {
        return defaultValue;
    }

    /** Clamps/validates a value into this key's acceptable range/domain. */
    public abstract T clamp(T value);

    /** Writes {@code value} under {@link #id()} into {@code tag}. */
    public abstract void write(CompoundTag tag, T value);

    /**
     * Reads this key's value from {@code tag}, returning {@link #defaultValue()}
     * if the tag is absent, malformed, or otherwise fails to resolve.
     */
    public abstract T read(CompoundTag tag);
}
