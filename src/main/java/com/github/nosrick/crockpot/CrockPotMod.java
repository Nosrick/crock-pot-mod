package com.github.nosrick.crockpot;

import com.github.nosrick.crockpot.compat.cloth.ClothConfigManager;
import com.github.nosrick.crockpot.registry.BlockRegistry;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.github.nosrick.crockpot.registry.CrockPotSoundRegistry;
import com.github.nosrick.crockpot.registry.ItemRegistry;
import com.github.nosrick.crockpot.util.FoodManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CrockPotMod implements ModInitializer {
    public static final String MOD_ID = "crockpot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier CROCK_POT_CHANNEL = createIdentifier("crockpot_blockentity_update");

    public static final List<String> MODS_LOADED = new ArrayList<>();

    public static final FoodManager FOOD_MANAGER = new FoodManager();

    @Override
    public void onInitialize() {

        LOGGER.info("REV UP THOSE CROCK POTS BOIS");

        CrockPotSoundRegistry.registerAll();
        BlockRegistry.initialize();
        BlockEntityTypesRegistry.initialize();
        ItemRegistry.initialize();

        if(FabricLoader.getInstance().isModLoaded("cloth-config")){
            ClothConfigManager.registerAutoConfig();
        }
    }

    public static Identifier createIdentifier(String key) {
        return Identifier.of(CrockPotMod.MOD_ID, key);
    }
}
