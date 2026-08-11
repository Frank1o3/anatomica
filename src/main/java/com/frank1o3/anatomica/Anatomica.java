package com.frank1o3.anatomica;

import com.frank1o3.anatomica.networking.AnatomicaNetworking;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Anatomica implements ModInitializer {
    public static final String MOD_ID = "anatomica";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Anatomica initializing (common)");

        AnatomicaNetworking.registerCommon();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
