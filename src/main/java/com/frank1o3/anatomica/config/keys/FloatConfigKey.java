package com.frank1o3.anatomica.config.keys;

import com.frank1o3.anatomica.config.BodyConfigKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public final class FloatConfigKey extends BodyConfigKey<Float> {

    private final float min;
    private final float max;

    public FloatConfigKey(String id, float defaultValue, float min, float max) {
        super(id, defaultValue);
        if (max <= min) {
            throw new IllegalArgumentException("max (" + max + ") must be > min (" + min + ") for key " + id);
        }
        this.min = min;
        this.max = max;
    }

    public float min() {
        return min;
    }

    public float max() {
        return max;
    }

    @Override
    public Float clamp(Float value) {
        if (value == null || value.isNaN()) {
            return defaultValue();
        }
        return Mth.clamp(value, min, max);
    }

    @Override
    public void write(CompoundTag tag, Float value) {
        tag.putFloat(id(), clamp(value));
    }

    @Override
    public Float read(CompoundTag tag) {
        if (!tag.contains(id())) {
            return defaultValue();
        }
        return clamp(tag.getFloatOr(id(), defaultValue()));
    }
}
