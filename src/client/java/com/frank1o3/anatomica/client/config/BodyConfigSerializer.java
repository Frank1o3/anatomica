package com.frank1o3.anatomica.client.config;

import com.frank1o3.anatomica.config.IBodyConfig;
import com.frank1o3.anatomica.config.IBodyConfigSerializer;
import net.minecraft.nbt.CompoundTag;

public final class BodyConfigSerializer implements IBodyConfigSerializer {

    @Override
    public IBodyConfig createDefaultBodyConfig() {
        return new BodyConfig();
    }

    @Override
    public IBodyConfig bodyConfigFromNbt(CompoundTag tag) {
        BodyConfig config = new BodyConfig();

        for (AnatomicaConfig.RegisteredKey<?> entry : AnatomicaConfig.ENTRIES) {
            entry.readInto(tag, config);
        }

        return config;
    }

    @Override
    public CompoundTag bodyConfigToNbt(IBodyConfig config) {
        BodyConfig bodyConfig = (BodyConfig) config;

        CompoundTag tag = new CompoundTag();

        for (AnatomicaConfig.RegisteredKey<?> entry : AnatomicaConfig.ENTRIES) {
            entry.writeInto(tag, bodyConfig);
        }

        return tag;
    }
}