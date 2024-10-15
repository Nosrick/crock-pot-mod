package com.github.nosrick.crockpot.registry;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.item.StewItem;
import net.minecraft.block.Block;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;


public class ItemRegistry {

    public static final Item STEW_ITEM = register(
            StewItem::new,
            "stew",
            new Item.Settings()
                    .food(
                            new FoodComponent.Builder()
                                    .build())
                    .recipeRemainder(Items.BOWL));

    public static final Item CROCK_POT = registerBlock(
            BlockRegistry.CROCK_POT,
            BlockItem::new);

    public static final Item ELECTRIC_CROCK_POT = registerBlock(
            BlockRegistry.ELECTRIC_CROCK_POT,
            BlockItem::new);

    public static Item register(Function<Item.Settings, Item> factory, String id, Item.Settings settings) {
        Identifier itemID = CrockPotMod.createIdentifier(id);
        RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, itemID);

        Item item = factory.apply(settings.registryKey(registryKey));

        return Registry.register(Registries.ITEM, itemID, item);
    }

    public static Item registerBlock(Block block, BiFunction<Block, Item.Settings, Item> factory) {
        Item.Settings itemSettings = new Item.Settings();

        var blockKey = RegistryKey.of(RegistryKeys.ITEM, block.getRegistryEntry().registryKey().getValue());
        Item item = factory.apply(block, itemSettings);
        itemSettings.useBlockPrefixedTranslationKey();

        return Registry.register(Registries.ITEM, blockKey, item);
    }

    public static void initialize() {
    }
}