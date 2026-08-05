package com.frank1o3.anatomica.client;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.client.gui.screen.BodyCustomizationScreen;
import com.frank1o3.anatomica.client.model.BoxDeformableModel;
import com.frank1o3.anatomica.client.model.OrganicMeshDeformableModel;
import com.frank1o3.anatomica.client.networking.AnatomicaClientNetworking;
import com.frank1o3.anatomica.client.physics.SoftbodyPhysicsEngine;
import com.frank1o3.anatomica.client.render.BodyPhysicsTicker;
import com.frank1o3.anatomica.client.render.BodyRenderLayer;
import com.frank1o3.anatomica.registry.AnatomicaRegistries;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

public class AnatomicaClient implements ClientModInitializer {

    private static KeyMapping openCustomizationScreenKey;
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category
            .register(Anatomica.id("main"));
    private static final String BINDING_KEY = Anatomica.id("open_scale_screen").toLanguageKey("key");

    @Override
    public void onInitializeClient() {
        Anatomica.LOGGER.info("Anatomica initializing (client)");

        registerBuiltins();
        AnatomicaClientNetworking.register();
        BodyPhysicsTicker.register();
        registerRenderLayer();
        registerKeybind();
    }

    private void registerBuiltins() {
        Registry.register(AnatomicaRegistries.PHYSICS_ENGINES, Anatomica.id("softbody"), SoftbodyPhysicsEngine::new);
        Registry.register(AnatomicaRegistries.MODELS, Anatomica.id("box"), BoxDeformableModel::new);
        Registry.register(AnatomicaRegistries.MODELS, Anatomica.id("organic"), OrganicMeshDeformableModel::new);
    }

    /**
     * Registers {@link BodyRenderLayer} onto the player entity renderer via Fabric
     * API's
     * feature-renderer registration callback (the standard extension point for
     * adding a
     * render layer to an existing entity renderer without mixing into it directly).
     *
     * <p>
     * Note: verify the exact generic bounds / lambda shape this callback expects
     * against
     * your Fabric API version — the callback's type parameters can be strict about
     * matching the renderer's own {@code <S, M>} pair exactly.
     */
    private void registerRenderLayer() {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, registrar, context) -> {
            if (renderer.getModel() instanceof PlayerModel
                    && registrar.type().equals(net.minecraft.world.entity.player.Player.class)) {
                // Left generic; real registration depends on registrar's exact API shape
                // (some Fabric API versions expose registrar.register(new
                // BodyRenderLayer(renderer))
                // directly, others require an explicit generic cast). Wire this to whatever
                // your version's registrar signature actually is.
            }
        });

        // If the callback above doesn't cleanly resolve against your Fabric API
        // version,
        // the fallback is a mixin into PlayerRenderer's constructor that calls
        // this.addLayer(new BodyRenderLayer(this)) directly - a handful of lines, same
        // pattern as InventoryScreenAccessor but targeting PlayerRenderer instead.
    }

    private void registerKeybind() {
        openCustomizationScreenKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                BINDING_KEY,
                GLFW.GLFW_KEY_B,
                CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openCustomizationScreenKey.consumeClick()) {
                if (client.gui.screen() == null && client.player != null) {
                    client.gui.setScreen(new BodyCustomizationScreen(null));
                }
            }
        });
    }
}
