package com.frank1o3.anatomica.client;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.client.gui.screen.BodyCustomizationScreen;
import com.frank1o3.anatomica.client.networking.AnatomicaClientNetworking;
import com.frank1o3.anatomica.client.render.BodyPhysicsTicker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class AnatomicaClient implements ClientModInitializer {

    private static KeyMapping openCustomizationScreenKey;
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category
            .register(Anatomica.id("main"));
    private static final String BINDING_KEY = Anatomica.id("open_scale_screen").toLanguageKey("key");

    @Override
    public void onInitializeClient() {
        Anatomica.LOGGER.info("Anatomica initializing (client)");

        AnatomicaClientNetworking.register();
        BodyPhysicsTicker.register();
        registerKeybind();
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
