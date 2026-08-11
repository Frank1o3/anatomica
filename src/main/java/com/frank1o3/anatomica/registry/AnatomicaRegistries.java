package com.frank1o3.anatomica.registry;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.client.model.BreastDeformableModel;
import com.frank1o3.anatomica.client.model.RoundedBreastDeformableModel;
import com.frank1o3.anatomica.client.model.WedgeDeformableModel;
import com.frank1o3.anatomica.client.physics.SoftbodyPhysicsEngine;
import com.frank1o3.anatomica.model.ModelFactory;
import com.frank1o3.anatomica.physics.PhysicsEngineFactory;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Registries for the two kinds of pluggable, mod-extensible content in
 * Anatomica:
 * physics engines and deformable models. Both are defaulted registries so an
 * unknown
 * id (e.g. from a saved config referencing an engine/model that isn't
 * installed)
 * resolves to a safe built-in default rather than crashing.
 *
 * <p>
 * Built-in entries are registered client-side in {@code AnatomicaClient}, since
 * both
 * physics simulation and mesh rendering are client-only concerns — this class
 * only
 * declares the registries themselves, common-side, so config validation
 * ({@code IdentifierConfigKey}) can reference them without pulling in client
 * code.
 */
public final class AnatomicaRegistries {

        private static final ResourceKey<Registry<PhysicsEngineFactory>> PHYSICS_ENGINES_KEY = ResourceKey
                        .createRegistryKey(Anatomica.id("physics_engines"));

        private static final ResourceKey<Registry<ModelFactory>> MODELS_KEY = ResourceKey
                        .createRegistryKey(Anatomica.id("models"));

        public static final DefaultedRegistry<PhysicsEngineFactory> PHYSICS_ENGINES = FabricRegistryBuilder
                        .createDefaulted(PHYSICS_ENGINES_KEY, Anatomica.id("softbody"))
                        .buildAndRegister();

        public static final DefaultedRegistry<ModelFactory> MODELS = FabricRegistryBuilder
                        .createDefaulted(MODELS_KEY, Anatomica.id("box"))
                        .buildAndRegister();

        private AnatomicaRegistries() {
        }

        /** No-op call to force this class's static initializers to run early. */
        public static void bootstrap() {
        }

        public static void registerAll() {
                Registry.register(AnatomicaRegistries.PHYSICS_ENGINES, Anatomica.id("softbody"),
                                SoftbodyPhysicsEngine::new);
                Registry.register(AnatomicaRegistries.MODELS, Anatomica.id("wedge"), WedgeDeformableModel::new);
                Registry.register(AnatomicaRegistries.MODELS, Anatomica.id("rounded"),
                                RoundedBreastDeformableModel::new);
                Registry.register(AnatomicaRegistries.MODELS, Anatomica.id("breast"), BreastDeformableModel::new);
        }
}
