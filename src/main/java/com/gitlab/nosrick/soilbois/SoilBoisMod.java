package com.gitlab.nosrick.soilbois;

import com.gitlab.nosrick.soilbois.registry.BlockRegistry;
import com.gitlab.nosrick.soilbois.registry.ItemRegistry;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoilBoisMod implements ModInitializer {
    public static final String MOD_ID = "soilbois";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("FUCK FACTORY FARMING don't @me");

        BlockRegistry.registerAll();
        ItemRegistry.registerAll();
    }
}
