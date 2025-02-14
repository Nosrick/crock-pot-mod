package com.github.nosrick.crockpot.tag;

import com.github.nosrick.crockpot.CrockPotMod;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class Tags {

    private Tags(){}

    public static TagKey<Block> HEAT_SOURCES = createBlockTag(Identifier.of("c", "heat_sources"));
    public static TagKey<Block> CROCK_POT_REQUIRES_SUPPORT = createBlockTag(
            CrockPotMod.createIdentifier("crock_pot_requires_support"));

    public static TagKey<Item> CONSUMABLE_WATER_SOURCES_ITEMS = createItemTag(Identifier.of(CrockPotMod.MOD_ID, "consumable_water_sources"));
    public static TagKey<Block> CONSUMABLE_WATER_SOURCES_BLOCKS = createBlockTag(Identifier.of(CrockPotMod.MOD_ID, "consumable_water_sources"));
    public static TagKey<Item> INFINITE_WATER_SOURCES_ITEMS = createItemTag(Identifier.of(CrockPotMod.MOD_ID, "infinite_water_sources"));
    public static TagKey<Block> INFINITE_WATER_SOURCES_BLOCKS = createBlockTag(Identifier.of(CrockPotMod.MOD_ID, "infinite_water_sources"));

    private static TagKey<Block> createBlockTag(Identifier id) {
        return TagKey.of(RegistryKeys.BLOCK, id);
    }

    private static TagKey<Item> createItemTag(Identifier id) {
        return TagKey.of(RegistryKeys.ITEM, id);
    }
}
