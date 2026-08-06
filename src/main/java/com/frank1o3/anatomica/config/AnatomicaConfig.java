package com.frank1o3.anatomica.config;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.config.keys.BooleanConfigKey;
import com.frank1o3.anatomica.config.keys.FloatConfigKey;
import com.frank1o3.anatomica.config.keys.IdentifierConfigKey;
import com.frank1o3.anatomica.config.keys.UVLayoutConfigKey;
import com.frank1o3.anatomica.registry.AnatomicaRegistries;
import com.frank1o3.anatomica.uv.UVLayout;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The full, ordered table of {@link BodyConfig} fields, each paired with the
 * {@link BodyConfigKey} that validates/(de)serializes it.
 */
public final class AnatomicaConfig {

    public static final FloatConfigKey SIZE = new FloatConfigKey("size", 0.5f, 0.0f, 1.0f);

    public static final FloatConfigKey OFFSET_X = new FloatConfigKey("offset_x", 0.0f, -0.5f, 0.5f);

    public static final FloatConfigKey OFFSET_Y = new FloatConfigKey("offset_y", 0.0f, -0.5f, 0.5f);

    public static final FloatConfigKey OFFSET_Z = new FloatConfigKey("offset_z", 0.0f, -0.5f, 0.5f);

    public static final FloatConfigKey SPREAD = new FloatConfigKey("spread", 0.02f, 0.0f, 0.1f);

    public static final UVLayoutConfigKey LEFT_UV_LAYOUT = new UVLayoutConfigKey("left_uv_layout", UVLayout.DEFAULT_LEFT);
    public static final UVLayoutConfigKey RIGHT_UV_LAYOUT = new UVLayoutConfigKey("right_uv_layout", UVLayout.DEFAULT_RIGHT);

    public static final BooleanConfigKey INDEPENDENT_SIDES = new BooleanConfigKey("independent_sides", true);

    public static final BooleanConfigKey PHYSICS_ENABLED = new BooleanConfigKey("physics_enabled", true);

    public static final FloatConfigKey BOUNCE_STRENGTH = new FloatConfigKey("bounce_strength", 0.5f, 0.0f, 1.0f);

    public static final FloatConfigKey SOFTNESS = new FloatConfigKey("softness", 0.5f, 0.0f, 1.0f);

    public static final IdentifierConfigKey PHYSICS_ENGINE_ID = new IdentifierConfigKey("physics_engine",
            Anatomica.id("softbody"),
            () -> AnatomicaRegistries.PHYSICS_ENGINES);

    public static final IdentifierConfigKey MODEL_ID = new IdentifierConfigKey("model", Anatomica.id("box"),
            () -> AnatomicaRegistries.MODELS);

    public static final BooleanConfigKey SHOW_IN_ARMOR = new BooleanConfigKey("show_in_armor", false);

    public static final List<RegisteredKey<?>> ENTRIES = List.of(
            entry(SIZE, BodyConfig::size, BodyConfig::setSize),
            entry(OFFSET_X, BodyConfig::offsetX, BodyConfig::setOffsetX),
            entry(OFFSET_Y, BodyConfig::offsetY, BodyConfig::setOffsetY),
            entry(OFFSET_Z, BodyConfig::offsetZ, BodyConfig::setOffsetZ),
            entry(SPREAD, BodyConfig::spread, BodyConfig::setSpread),
            entry(INDEPENDENT_SIDES, BodyConfig::independentSides, BodyConfig::setIndependentSides),
            entry(PHYSICS_ENABLED, BodyConfig::physicsEnabled, BodyConfig::setPhysicsEnabled),
            entry(BOUNCE_STRENGTH, BodyConfig::bounceStrength, BodyConfig::setBounceStrength),
            entry(SOFTNESS, BodyConfig::softness, BodyConfig::setSoftness),
            entry(PHYSICS_ENGINE_ID, BodyConfig::physicsEngineId, BodyConfig::setPhysicsEngineId),
            entry(MODEL_ID, BodyConfig::modelId, BodyConfig::setModelId),
            entry(SHOW_IN_ARMOR, BodyConfig::showInArmor, BodyConfig::setShowInArmor),
            entry(LEFT_UV_LAYOUT, BodyConfig::leftUvLayout, BodyConfig::setLeftUvLayout),
            entry(RIGHT_UV_LAYOUT, BodyConfig::rightUvLayout, BodyConfig::setRightUvLayout));

    private AnatomicaConfig() {
    }

    private static <T> RegisteredKey<T> entry(BodyConfigKey<T> key, Function<BodyConfig, T> getter,
            BiConsumer<BodyConfig, T> setter) {
        return new RegisteredKey<>(key, getter, setter);
    }

    /**
     * Pairs a {@link BodyConfigKey} with the getter/setter it drives on
     * {@link BodyConfig}.
     */
    public record RegisteredKey<T>(BodyConfigKey<T> key, Function<BodyConfig, T> getter,
            BiConsumer<BodyConfig, T> setter) {

        void writeInto(CompoundTag tag, BodyConfig config) {
            key.write(tag, getter.apply(config));
        }

        void readInto(CompoundTag tag, BodyConfig config) {
            setter.accept(config, key.read(tag));
        }
    }
}
