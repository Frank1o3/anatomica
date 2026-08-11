package com.frank1o3.anatomica.config;

import net.minecraft.nbt.CompoundTag;

/**
 * * Common contract for Anatomica configuration serialization. * *
 * <p>
 * The concrete configuration/key table is implementation-specific. Common *
 * code should not depend on the individual client-side configuration key *
 * classes.
 * </p>
 */
public interface IAnatomicaConfig {
    /** * Creates a default body configuration. */
    IBodyConfig createDefaultBodyConfig();

    /** * Reads a body configuration from serialized data. */
    IBodyConfig bodyConfigFromNbt(CompoundTag tag);

    /** * Writes a body configuration to serialized data. */
    CompoundTag bodyConfigToNbt(IBodyConfig config);
}