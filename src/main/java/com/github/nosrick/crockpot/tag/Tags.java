package com.github.nosrick.crockpot.tag;

import com.github.nosrick.crockpot.CrockPotMod;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class Tags {

    private Tags(){}

    public static final Tag.Identified<Block> HEAT_SOURCES = create(new Identifier("c", "heat_sources"), TagFactory.BLOCK);

    private static <E> Tag.Identified<E> create(String pathName, TagFactory<E> tagFactory){
        return tagFactory.create(new Identifier(CrockPotMod.MOD_ID, pathName));
    }

    private static <E> Tag.Identified<E> create(Identifier id, TagFactory<E> tagFactory) {
        return tagFactory.create(id);
    }
}
