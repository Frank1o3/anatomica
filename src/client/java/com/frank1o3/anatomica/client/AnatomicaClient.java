package com.frank1o3.anatomica.client;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.client.gui.screen.BodyCustomizationScreen;
import com.frank1o3.anatomica.client.networking.AnatomicaClientNetworking;
import com.frank1o3.anatomica.client.config.ClientBodyConfigStorage;
import com.frank1o3.anatomica.client.registry.AnatomicaRegistries;
import com.frank1o3.anatomica.client.render.BodyPhysicsTicker;
import com.frank1o3.anatomica.client.render.BodyArmorRenderLayer;
import com.frank1o3.anatomica.client.render.BodyRenderLayer;
import com.frank1o3.anatomica.client.render.ModelMeshCache;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.glfw.GLFW;

public class AnatomicaClient implements ClientModInitializer {

    private static KeyMapping openCustomizationScreenKey;
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category
            .register(Anatomica.id("main"));
    private static final String BINDING_KEY = Anatomica.id("open_scale_screen").toLanguageKey("key");

    @Override
    public void onInitializeClient() {
        Anatomica.LOGGER.info("Anatomica initializing (client)");

        AnatomicaRegistries.registerClient();
        AnatomicaClientNetworking.register();
        registerConnectionEvents();
        registerResourceReloadListener();
        BodyPhysicsTicker.register();
        registerKeybind();
    }

    private void registerResourceReloadListener() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Anatomica.id("clear_model_caches");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        BodyRenderLayer.clearModelCache();
                        BodyArmorRenderLayer.clearModelCache();
                        ModelMeshCache.clear();
                    }
                });
    }

    private void registerConnectionEvents() {
        ClientPlayConnectionEvents.JOIN.register((listener, sender, client) ->
                AnatomicaClientNetworking.loadAndSyncLocalConfig());
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> {
            ClientBodyConfigStorage.closeAll();
            com.frank1o3.anatomica.client.data.EntityBodyData.INSTANCE.clear();
            com.frank1o3.anatomica.client.render.ClientBodyPhysics.clearAll();
        });
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
