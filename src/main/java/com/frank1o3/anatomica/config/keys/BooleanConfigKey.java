package com.frank1o3.anatomica.config.keys;

import com.frank1o3.anatomica.config.BodyConfigKey;
import net.minecraft.nbt.CompoundTag;

public final class BooleanConfigKey extends BodyConfigKey<Boolean> {

    public BooleanConfigKey(String id, boolean defaultValue) {
        super(id, defaultValue);
    }

    @Override
    public Boolean clamp(Boolean value) {
        return value == null ? defaultValue() : value;
    }

    @Override
    public void write(CompoundTag tag, Boolean value) {
        tag.putBoolean(id(), clamp(value));
    }

    @Override
    public Boolean read(CompoundTag tag) {
        if (!tag.contains(id())) {
            return defaultValue();
        }
        return tag.getBooleanOr(id(), defaultValue());
    }
}
