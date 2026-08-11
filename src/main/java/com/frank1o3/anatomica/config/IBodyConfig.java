package com.frank1o3.anatomica.config;

import com.frank1o3.anatomica.uv.UVLayout;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

/** * Common contract for an Anatomica entity body configuration. */
public interface IBodyConfig {
    boolean breastsEnabled();

    void setBreastsEnabled(boolean value);

    float size();

    void setSize(float value);

    float petite();

    void setPetite(float value);

    float offsetX();

    void setOffsetX(float value);

    float offsetY();

    void setOffsetY(float value);

    float offsetZ();

    void setOffsetZ(float value);

    UVLayout leftUvLayout();

    void setLeftUvLayout(UVLayout value);

    UVLayout rightUvLayout();

    void setRightUvLayout(UVLayout value);

    float spread();

    void setSpread(float value);

    float cleavage();

    void setCleavage(float value);

    boolean independentSides();

    void setIndependentSides(boolean value);

    boolean physicsEnabled();

    void setPhysicsEnabled(boolean value);

    float bounceStrength();

    void setBounceStrength(float value);

    float softness();

    void setSoftness(float value);

    Identifier physicsEngineId();

    void setPhysicsEngineId(Identifier value);

    Identifier modelId();

    void setModelId(Identifier value);

    boolean showInArmor();

    void setShowInArmor(boolean value);

    CompoundTag toNbt();

    /** * Creates a body configuration from serialized NBT data. */
    static IBodyConfig fromNbt(CompoundTag tag) {
        throw new UnsupportedOperationException(
                "IBodyConfig.fromNbt() must be provided by the concrete configuration implementation");
    }
}