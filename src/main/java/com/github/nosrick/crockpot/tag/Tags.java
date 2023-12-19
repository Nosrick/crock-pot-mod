package com.github.nosrick.crockpot.tag;

import com.github.nosrick.crockpot.CrockPotMod;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Tags {

    private Tags(){}

    public static TagKey<Block> HEAT_SOURCES = createBlockTag(new Identifier("c", "heat_sources"));
    public static TagKey<Block> CROCK_POT_REQUIRES_SUPPORT = createBlockTag(
            new Identifier(CrockPotMod.MOD_ID, "crock_pot_requires_support"));

    public static TagKey<Item> CONSUMABLE_WATER_SOURCES_ITEMS = createItemTag(new Identifier(CrockPotMod.MOD_ID, "consumable_water_sources"));
    public static TagKey<Block> CONSUMABLE_WATER_SOURCES_BLOCKS = createBlockTag(new Identifier(CrockPotMod.MOD_ID, "consumable_water_sources"));
    public static TagKey<Item> INFINITE_WATER_SOURCES_ITEMS = createItemTag(new Identifier(CrockPotMod.MOD_ID, "infinite_water_sources"));
    public static TagKey<Block> INFINITE_WATER_SOURCES_BLOCKS = createBlockTag(new Identifier(CrockPotMod.MOD_ID, "infinite_water_sources"));

    private static TagKey<Block> createBlockTag(Identifier id) {
        return TagKey.of(Registry.BLOCK_KEY, id);
    }

    private static TagKey<Item> createItemTag(Identifier id) { return TagKey.of(Registry.ITEM_KEY, id); }
}
