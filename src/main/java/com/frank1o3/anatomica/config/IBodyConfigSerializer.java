package com.frank1o3.anatomica.config;

import net.minecraft.nbt.CompoundTag;

public interface IBodyConfigSerializer {

    IBodyConfig createDefaultBodyConfig();

    IBodyConfig bodyConfigFromNbt(CompoundTag tag);

    CompoundTag bodyConfigToNbt(IBodyConfig config);
}