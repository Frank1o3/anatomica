package com.frank1o3.anatomica.client.config.keys;

import com.frank1o3.anatomica.client.config.BodyConfigKey;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class IntConfigKey extends BodyConfigKey<Integer> {
    private final int min;
    private final int max;

    public IntConfigKey(String id, int defaultValue, int min, int max) {
        super(id, defaultValue);
        this.min = min;
        this.max = max;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    @Override
    public Integer clamp(Integer value) {
        return value == null ? defaultValue() : Mth.clamp(value, min, max);
    }

    @Override
    public void write(CompoundTag tag, Integer value) {
        tag.putInt(id(), clamp(value));
    }

    @Override
    public Integer read(CompoundTag tag) {
        if (!tag.contains(id()))
            return defaultValue();
        return clamp(tag.getIntOr(id(), defaultValue()));
    }
}
