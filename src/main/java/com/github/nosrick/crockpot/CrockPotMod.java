package com.github.nosrick.crockpot;

import com.github.nosrick.crockpot.registry.BlockRegistry;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.github.nosrick.crockpot.registry.CrockPotSoundRegistry;
import com.github.nosrick.crockpot.registry.ItemRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CrockPotMod implements ModInitializer {
    public static final String MOD_ID = "crockpot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "main"),
            () -> new ItemStack(ItemRegistry.CROCK_POT.get()));

    public static final List<String> MODS_LOADED = new ArrayList<>();

    @Override
    public void onInitialize() {
        LOGGER.info("REV UP THOSE CROCK POTS BOIS");

        CrockPotSoundRegistry.registerAll();
        BlockRegistry.registerAll();
        BlockEntityTypesRegistry.registerAll();
        ItemRegistry.registerAll();
    }
}
