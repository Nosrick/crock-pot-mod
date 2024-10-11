package com.github.nosrick.crockpot.registry;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.block.CrockPotBlock;
import com.github.nosrick.crockpot.block.ElectricCrockPotBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class BlockRegistry {

    public static Block CROCK_POT = register(new CrockPotBlock(), "crock_pot");
    public static Block ELECTRIC_CROCK_POT = register(new ElectricCrockPotBlock(), "electric_crock_pot");

    public static void initialize() {

    }

    public static Block register(Block block, String id) {
        Identifier blockID = CrockPotMod.createIdentifier(id);
        return Registry.register(Registries.BLOCK, blockID, block);
    }

    /*
    @Environment(EnvType.CLIENT)
    public static void registerRenderLayer() {
        BlockRenderLayerMap.INSTANCE.putBlock(CROCK_POT, RenderLayer.getCutout());
    }
     */
}
