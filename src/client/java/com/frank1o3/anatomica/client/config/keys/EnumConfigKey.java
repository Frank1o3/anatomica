package com.frank1o3.anatomica.client.config.keys;

import com.frank1o3.anatomica.client.config.BodyConfigKey;
import net.minecraft.nbt.CompoundTag;

public final class EnumConfigKey<E extends Enum<E>> extends BodyConfigKey<E> {

    private final Class<E> enumType;

    public EnumConfigKey(String id, Class<E> enumType, E defaultValue) {
        super(id, defaultValue);
        this.enumType = enumType;
    }

    @Override
    public E clamp(E value) {
        return value == null ? defaultValue() : value;
    }

    @Override
    public void write(CompoundTag tag, E value) {
        tag.putString(id(), clamp(value).name());
    }

    @Override
    public E read(CompoundTag tag) {
        String name = tag.getStringOr(id(), null);
        if (name == null) {
            return defaultValue();
        }
        try {
            return Enum.valueOf(enumType, name);
        } catch (IllegalArgumentException e) {
            // Unknown/stale enum constant (e.g. saved by a newer/older mod version) ->
            // fall back rather than throw, same policy as IdentifierConfigKey.
            return defaultValue();
        }
    }
}
