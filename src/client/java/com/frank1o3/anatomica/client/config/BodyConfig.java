package com.frank1o3.anatomica.client.config;

import com.frank1o3.anatomica.config.IBodyConfig;
import com.frank1o3.anatomica.networking.BodySyncData;
import com.frank1o3.anatomica.uv.UVLayout;
import com.frank1o3.franklylib.config.ConfigEntry;
import com.frank1o3.franklylib.config.Range;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

/**
 * A single entity's body customization state. Pure data — no Minecraft client
 * imports, no persistence/networking logic of its own. Serialization is driven
 * generically by {@link AnatomicaConfig}'s registered key table so disk and
 * network code share exactly one read/write path.
 */
public final class BodyConfig implements IBodyConfig {
    @ConfigEntry(id = "breasts_enabled")
    private boolean breastsEnabled = AnatomicaConfig.BREASTS_ENABLED.defaultValue();
    @ConfigEntry @Range(min = 0.0, max = 1.0)
    private float size = AnatomicaConfig.SIZE.defaultValue();
    @ConfigEntry @Range(min = 0.0, max = 1.0)
    private float petite = AnatomicaConfig.PETITE.defaultValue();
    @ConfigEntry(id = "offset_x") @Range(min = -0.5, max = 0.5)
    private float offsetX = AnatomicaConfig.OFFSET_X.defaultValue();
    @ConfigEntry(id = "offset_y") @Range(min = -0.5, max = 0.5)
    private float offsetY = AnatomicaConfig.OFFSET_Y.defaultValue();
    @ConfigEntry(id = "offset_z") @Range(min = -0.5, max = 0.5)
    private float offsetZ = AnatomicaConfig.OFFSET_Z.defaultValue();
    @ConfigEntry(id = "left_uv_layout")
    private UVLayout leftUvLayout = AnatomicaConfig.LEFT_UV_LAYOUT.defaultValue();
    @ConfigEntry(id = "right_uv_layout")
    private UVLayout rightUvLayout = AnatomicaConfig.RIGHT_UV_LAYOUT.defaultValue();
    @ConfigEntry @Range(min = 0.0, max = 0.1)
    private float spread = AnatomicaConfig.SPREAD.defaultValue();
    @ConfigEntry @Range(min = 0.0, max = 0.1)
    private float cleavage = AnatomicaConfig.CLEAVAGE.defaultValue();
    @ConfigEntry(id = "independent_sides")
    private boolean independentSides = AnatomicaConfig.INDEPENDENT_SIDES.defaultValue();
    @ConfigEntry(id = "physics_enabled")
    private boolean physicsEnabled = AnatomicaConfig.PHYSICS_ENABLED.defaultValue();
    @ConfigEntry(id = "bounce_strength") @Range(min = 0.0, max = 1.0)
    private float bounceStrength = AnatomicaConfig.BOUNCE_STRENGTH.defaultValue();
    @ConfigEntry @Range(min = 0.0, max = 1.0)
    private float softness = AnatomicaConfig.SOFTNESS.defaultValue();
    @ConfigEntry(id = "physics_engine")
    private Identifier physicsEngineId = AnatomicaConfig.PHYSICS_ENGINE_ID.defaultValue();
    @ConfigEntry(id = "model")
    private Identifier modelId = AnatomicaConfig.MODEL_ID.defaultValue();
    @ConfigEntry(id = "show_in_armor")
    private boolean showInArmor = AnatomicaConfig.SHOW_IN_ARMOR.defaultValue();

    public BodyConfig() {
    }

    // -------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------

    public boolean breastsEnabled() {
        return breastsEnabled;
    }

    public void setBreastsEnabled(boolean value) {
        this.breastsEnabled = value;
    }

    public float size() {
        return size;
    }

    public void setSize(float value) {
        this.size = AnatomicaConfig.SIZE.clamp(value);
    }

    public float petite() {
        return petite;
    }

    public void setPetite(float value) {
        this.petite = AnatomicaConfig.PETITE.clamp(value);
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

    public UVLayout leftUvLayout() {
        return leftUvLayout;
    }

    public void setLeftUvLayout(UVLayout value) {
        this.leftUvLayout = AnatomicaConfig.LEFT_UV_LAYOUT.clamp(value);
    }

    public UVLayout rightUvLayout() {
        return rightUvLayout;
    }

    public void setRightUvLayout(UVLayout value) {
        this.rightUvLayout = AnatomicaConfig.RIGHT_UV_LAYOUT.clamp(value);
    }

    public float spread() {
        return spread;
    }

    public void setSpread(float value) {
        this.spread = AnatomicaConfig.SPREAD.clamp(value);
    }

    public float cleavage() {
        return cleavage;
    }

    public void setCleavage(float value) {
        this.cleavage = AnatomicaConfig.CLEAVAGE.clamp(value);
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

    /** Converts to the compact, NBT-free representation used on the network. */
    public BodySyncData toSyncData() {
        return new BodySyncData(breastsEnabled, size, petite, offsetX, offsetY, offsetZ,
                leftUvLayout.copy(), rightUvLayout.copy(), spread, cleavage, independentSides, physicsEnabled,
                bounceStrength, softness, physicsEngineId.toString(), modelId.toString(), showInArmor);
    }

    /** Builds a validated client config from data received from the server. */
    public static BodyConfig fromSyncData(BodySyncData data) {
        BodyConfig config = new BodyConfig();
        config.setBreastsEnabled(data.breastsEnabled());
        config.setSize(data.size()); config.setPetite(data.petite());
        config.setOffsetX(data.offsetX()); config.setOffsetY(data.offsetY()); config.setOffsetZ(data.offsetZ());
        config.setLeftUvLayout(data.leftUvLayout()); config.setRightUvLayout(data.rightUvLayout());
        config.setSpread(data.spread()); config.setCleavage(data.cleavage());
        config.setIndependentSides(data.independentSides()); config.setPhysicsEnabled(data.physicsEnabled());
        config.setBounceStrength(data.bounceStrength()); config.setSoftness(data.softness());
        Identifier physicsEngineId = Identifier.tryParse(data.physicsEngineId());
        if (physicsEngineId != null) config.setPhysicsEngineId(physicsEngineId);
        Identifier modelId = Identifier.tryParse(data.modelId());
        if (modelId != null) config.setModelId(modelId);
        config.setShowInArmor(data.showInArmor());
        return config;
    }
}
