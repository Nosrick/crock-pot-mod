package com.github.nosrick.crockpot;

import com.github.nosrick.crockpot.compat.cloth.ClothConfigManager;
import com.github.nosrick.crockpot.registry.BlockRegistry;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.github.nosrick.crockpot.registry.CrockPotSoundRegistry;
import com.github.nosrick.crockpot.registry.ItemRegistry;
import com.github.nosrick.crockpot.util.FoodManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CrockPotMod implements ModInitializer {
    public static final String MOD_ID = "crockpot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier CROCK_POT_CHANNEL = new Identifier(MOD_ID, "crockpot_blockentity_update");

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "main"),
            () -> new ItemStack(ItemRegistry.CROCK_POT.get()));

    public static final List<String> MODS_LOADED = new ArrayList<>();

    public static final FoodManager FOOD_MANAGER = new FoodManager();

    public static final int POT_COLOUR = ColorHelper.Argb.getArgb(255, 253, 143, 68);
    public static final int ELECTRIC_COLOUR = ColorHelper.Argb.getArgb(255, 222, 100, 100);
    public static final int WATER_COLOUR = ColorHelper.Argb.getArgb(255, 91, 110, 225);
    public static final int FOOD_COLOUR = ColorHelper.Argb.getArgb(255, 102, 57, 49);

    @Override
    public void onInitialize() {

        LOGGER.info("REV UP THOSE CROCK POTS BOIS");

        CrockPotSoundRegistry.registerAll();
        BlockRegistry.registerAll();
        BlockEntityTypesRegistry.registerAll();
        ItemRegistry.registerAll();

        if(FabricLoader.getInstance().isModLoaded("cloth-config")){
            ClothConfigManager.registerAutoConfig();
        }
    }
}
