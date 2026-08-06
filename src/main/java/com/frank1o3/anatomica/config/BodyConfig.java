package com.frank1o3.anatomica.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

/**
 * A single entity's body customization state. Pure data — no Minecraft client
 * imports, no persistence/networking logic of its own. Serialization is driven
 * generically by {@link AnatomicaConfig}'s registered key table so disk and
 * network
 * code share exactly one read/write path.
 */
public final class BodyConfig {

    private float size = AnatomicaConfig.SIZE.defaultValue();
    private float offsetX = AnatomicaConfig.OFFSET_X.defaultValue();
    private float offsetY = AnatomicaConfig.OFFSET_Y.defaultValue();
    private float offsetZ = AnatomicaConfig.OFFSET_Z.defaultValue();
    private int textureX1 = AnatomicaConfig.TEXTURE_X1.defaultValue();
    private int textureY1 = AnatomicaConfig.TEXTURE_Y1.defaultValue();
    private int textureX2 = AnatomicaConfig.TEXTURE_X2.defaultValue();
    private int textureY2 = AnatomicaConfig.TEXTURE_Y2.defaultValue();
    private float spread = AnatomicaConfig.SPREAD.defaultValue();
    private boolean independentSides = AnatomicaConfig.INDEPENDENT_SIDES.defaultValue();
    private boolean physicsEnabled = AnatomicaConfig.PHYSICS_ENABLED.defaultValue();
    private float bounceStrength = AnatomicaConfig.BOUNCE_STRENGTH.defaultValue();
    private float softness = AnatomicaConfig.SOFTNESS.defaultValue();
    private Identifier physicsEngineId = AnatomicaConfig.PHYSICS_ENGINE_ID.defaultValue();
    private Identifier modelId = AnatomicaConfig.MODEL_ID.defaultValue();
    private boolean showInArmor = AnatomicaConfig.SHOW_IN_ARMOR.defaultValue();

    public BodyConfig() {
    }

    // -------------------------------------------------------------------
    // Getters / setters (setters clamp through the owning key)
    // -------------------------------------------------------------------

    public float size() {
        return size;
    }

    public void setSize(float value) {
        this.size = AnatomicaConfig.SIZE.clamp(value);
    }

    public float offsetX() {
        return offsetX;
    }

    public void setOffsetX(float value) {
        this.offsetX = AnatomicaConfig.OFFSET_X.clamp(value);
    }

    public float offsetY() {
        return offsetY;
    }

    public void setOffsetY(float value) {
        this.offsetY = AnatomicaConfig.OFFSET_Y.clamp(value);
    }

    public float offsetZ() {
        return offsetZ;
    }

    public void setOffsetZ(float value) {
        this.offsetZ = AnatomicaConfig.OFFSET_Z.clamp(value);
    }

    public int textureX1() {
        return textureX1;
    }

    public void setTextureX1(int v) {
        this.textureX1 = AnatomicaConfig.TEXTURE_X1.clamp(v);
    }

    public int textureY1() {
        return textureY1;
    }

    public void setTextureY1(int v) {
        this.textureY1 = AnatomicaConfig.TEXTURE_Y1.clamp(v);
    }

    public int textureX2() {
        return textureX2;
    }

    public void setTextureX2(int v) {
        this.textureX2 = AnatomicaConfig.TEXTURE_X2.clamp(v);
    }

    public int textureY2() {
        return textureY2;
    }

    public void setTextureY2(int v) {
        this.textureY2 = AnatomicaConfig.TEXTURE_Y2.clamp(v);
    }

    public void setTextureRegion(int x1, int y1, int x2, int y2) {
        setTextureX1(x1);
        setTextureY1(y1);
        setTextureX2(x2);
        setTextureY2(y2);
    }

    public float spread() {
        return spread;
    }

    public void setSpread(float value) {
        this.spread = AnatomicaConfig.SPREAD.clamp(value);
    }

    public boolean independentSides() {
        return independentSides;
    }

    public void setIndependentSides(boolean value) {
        this.independentSides = value;
    }

    public boolean physicsEnabled() {
        return physicsEnabled;
    }

    public void setPhysicsEnabled(boolean value) {
        this.physicsEnabled = value;
    }

    public float bounceStrength() {
        return bounceStrength;
    }

    public void setBounceStrength(float value) {
        this.bounceStrength = AnatomicaConfig.BOUNCE_STRENGTH.clamp(value);
    }

    public float softness() {
        return softness;
    }

    public void setSoftness(float value) {
        this.softness = AnatomicaConfig.SOFTNESS.clamp(value);
    }

    public Identifier physicsEngineId() {
        return physicsEngineId;
    }

    public void setPhysicsEngineId(Identifier value) {
        this.physicsEngineId = AnatomicaConfig.PHYSICS_ENGINE_ID.clamp(value);
    }

    public Identifier modelId() {
        return modelId;
    }

    public void setModelId(Identifier value) {
        this.modelId = AnatomicaConfig.MODEL_ID.clamp(value);
    }

    public boolean showInArmor() {
        return showInArmor;
    }

    public void setShowInArmor(boolean value) {
        this.showInArmor = value;
    }

    // -------------------------------------------------------------------
    // Serialization
    // -------------------------------------------------------------------

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        for (AnatomicaConfig.RegisteredKey<?> entry : AnatomicaConfig.ENTRIES) {
            entry.writeInto(tag, this);
        }
        return tag;
    }

    public static BodyConfig fromNbt(CompoundTag tag) {
        BodyConfig config = new BodyConfig();
        for (AnatomicaConfig.RegisteredKey<?> entry : AnatomicaConfig.ENTRIES) {
            entry.readInto(tag, config);
        }
        return config;
    }

    public BodyConfig copy() {
        return fromNbt(toNbt());
    }
}
