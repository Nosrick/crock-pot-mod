package com.github.nosrick.crockpot.tag;

import com.github.nosrick.crockpot.CrockPotMod;
import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Tags {

    private Tags(){}

    public static TagKey<Block> HEAT_SOURCES = create(new Identifier("c", "heat_sources"));

    public static TagKey<Block> CROCK_POT_REQUIRES_SUPPORT = create(
            new Identifier(CrockPotMod.MOD_ID, "crock_pot_requires_support"));

    private static  TagKey<Block> create(Identifier id) {
        return TagKey.of(Registry.BLOCK_KEY, id);
    }
}
