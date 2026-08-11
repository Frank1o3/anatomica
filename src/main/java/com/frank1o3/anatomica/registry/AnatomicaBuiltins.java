package com.frank1o3.anatomica.registry;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.client.model.BoxDeformableModel; // now common-side, package left as-is
import com.frank1o3.anatomica.client.model.BreastDeformableModel;
import com.frank1o3.anatomica.client.model.RoundedBreastDeformableModel;
import com.frank1o3.anatomica.client.model.WedgeDeformableModel;
import com.frank1o3.anatomica.client.physics.SoftbodyPhysicsEngine;
import net.minecraft.core.Registry;

public final class AnatomicaBuiltins {
    private AnatomicaBuiltins() {
    }

    public static void registerAll() {
        Registry.register(AnatomicaRegistries.PHYSICS_ENGINES, Anatomica.id("softbody"), SoftbodyPhysicsEngine::new);
        Registry.register(AnatomicaRegistries.MODELS, Anatomica.id("wedge"), WedgeDeformableModel::new);
        Registry.register(AnatomicaRegistries.MODELS, Anatomica.id("box"), BoxDeformableModel::new);
        Registry.register(AnatomicaRegistries.MODELS, Anatomica.id("rounded"), RoundedBreastDeformableModel::new);
        Registry.register(AnatomicaRegistries.MODELS, Anatomica.id("breast"), BreastDeformableModel::new);
    }
}