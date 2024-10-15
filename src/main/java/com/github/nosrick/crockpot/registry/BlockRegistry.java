package com.github.nosrick.crockpot.registry;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.block.CrockPotBlock;
import com.github.nosrick.crockpot.block.ElectricCrockPotBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegistry {

    public static void initialize() {}

    public static Block register(Function<AbstractBlock.Settings, Block> factory, String id, AbstractBlock.Settings settings) {
        Identifier blockID = CrockPotMod.createIdentifier(id);
        RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, blockID);

        Block block = factory.apply(settings.registryKey(registryKey));

        return Registry.register(Registries.BLOCK, blockID, block);
    }

    public static final Block CROCK_POT = register(
            CrockPotBlock::new,
            "crock_pot",
            AbstractBlock.Settings
            .create()
            .strength(2.0f)
            .requiresTool()
            .nonOpaque());
    public static final Block ELECTRIC_CROCK_POT = register(
            ElectricCrockPotBlock::new,
            "electric_crock_pot",
            AbstractBlock.Settings
            .create()
            .strength(2.0f)
            .requiresTool()
            .nonOpaque());
}
