package com.frank1o3.anatomica.client.registry;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.client.model.ClothDeformableModel;
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
 * Registries for the pluggable, mod-extensible content in Anatomica:
 * physics engines, inner deformable models, and cloth deformable models.
 * All are defaulted registries so an unknown id resolves to a safe built-in
 * default rather than crashing.
 *
 * <p>
 * Built-in entries are registered client-side in {@code AnatomicaClient}, since
 * both physics simulation and mesh rendering are client-only concerns — this class
 * only declares the registries themselves, common-side, so config validation
 * ({@code IdentifierConfigKey}) can reference them without pulling in client code.
 */
public final class AnatomicaRegistries {

        private static final ResourceKey<Registry<PhysicsEngineFactory>> PHYSICS_ENGINES_KEY = ResourceKey
                        .createRegistryKey(Anatomica.id("physics_engines"));

        public static final DefaultedRegistry<PhysicsEngineFactory> PHYSICS_ENGINES = FabricRegistryBuilder
                        .createDefaulted(PHYSICS_ENGINES_KEY, Anatomica.id("softbody"))
                        .buildAndRegister();

        private static final ResourceKey<Registry<ModelFactory>> INNER_MODELS_KEY = ResourceKey
                        .createRegistryKey(Anatomica.id("inner_models"));

        public static final DefaultedRegistry<ModelFactory> INNER_MODELS = FabricRegistryBuilder
                        .createDefaulted(INNER_MODELS_KEY, Anatomica.id("breast"))
                        .buildAndRegister();

        private static final ResourceKey<Registry<ModelFactory>> CLOTH_MODELS_KEY = ResourceKey
                        .createRegistryKey(Anatomica.id("cloth_models"));

        public static final DefaultedRegistry<ModelFactory> CLOTH_MODELS = FabricRegistryBuilder
                        .createDefaulted(CLOTH_MODELS_KEY, Anatomica.id("cloth_basic"))
                        .buildAndRegister();

        private AnatomicaRegistries() {
        }

        public static void registerClient() {
                Registry.register(AnatomicaRegistries.PHYSICS_ENGINES, Anatomica.id("softbody"),
                                SoftbodyPhysicsEngine::new);

                Registry.register(AnatomicaRegistries.INNER_MODELS, Anatomica.id("wedge"), WedgeDeformableModel::new);
                Registry.register(AnatomicaRegistries.INNER_MODELS, Anatomica.id("rounded"),
                                RoundedBreastDeformableModel::new);
                Registry.register(AnatomicaRegistries.INNER_MODELS, Anatomica.id("breast"), BreastDeformableModel::new);

                Registry.register(AnatomicaRegistries.CLOTH_MODELS, Anatomica.id("cloth_basic"), ClothDeformableModel::new);
        }
}
