package com.gitlab.nosrick.soilbois.registry;

import com.gitlab.nosrick.soilbois.SoilBoisMod;
import com.nhoryzon.mc.farmersdelight.item.ConsumableItem;
import com.nhoryzon.mc.farmersdelight.item.ModBlockItem;
import com.nhoryzon.mc.farmersdelight.item.ModItemSettings;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public enum ItemRegistry {

    RAW_SEITAN("raw_seitan", () -> new ConsumableItem(new ModItemSettings().food(FoodRegistry.RAW_SEITAN.get()))),
    COOKED_SEITAN("cooked_seitan", () -> new ConsumableItem(new ModItemSettings().food(FoodRegistry.COOKED_SEITAN.get()))),
    RAW_SEITAN_PATTY("raw_seitan_patty", () -> new ConsumableItem(new ModItemSettings().food(FoodRegistry.RAW_SEITAN_PATTY.get()))),
    COOKED_SEITAN_PATTY("cooked_seitan_patty", () -> new ConsumableItem(new ModItemSettings().food(FoodRegistry.COOKED_SEITAN_PATTY.get()))),
    RAW_SEITAN_BACON("raw_seitan_bacon", () -> new ConsumableItem(new ModItemSettings().food(FoodRegistry.RAW_SEITAN_BACON.get()))),
    SEITAN_BACON("seitan_bacon", () -> new ConsumableItem(new ModItemSettings().food(FoodRegistry.SEITAN_BACON.get()))),
    SEITAN_SANDWICH("seitan_sandwich", () -> new ConsumableItem(new ModItemSettings().food(FoodRegistry.SEITAN_SANDWICH.get()))),

    OATS("oats", () -> new ModBlockItem(BlockRegistry.OAT_CROPS.get())),
    WILD_OATS("wild_oats", () -> new ModBlockItem(BlockRegistry.WILD_OATS.get())),
    OAT_MILK("oat_milk", () -> new ConsumableItem(new ModItemSettings().food(FoodRegistry.OAT_MILK.get()))),
    PLAIN_PORRIDGE("plain_porridge", () -> new ConsumableItem(new ModItemSettings().food(FoodRegistry.PLAIN_PORRIDGE.get())));

    private final String pathName;
    private final Supplier<Item> itemSupplier;
    private final Integer burnTime;
    private Item item;

    ItemRegistry(String pathName, Supplier<Item> itemSupplier) {
        this(pathName, itemSupplier, null);
    }

    ItemRegistry(String pathName, Supplier<Item> itemSupplier, Integer burnTime) {
        this.pathName = pathName;
        this.itemSupplier = itemSupplier;
        this.burnTime = burnTime;
    }

    public static void registerAll() {
        for (ItemRegistry value : values()) {
            Registry.register(Registry.ITEM, new Identifier(SoilBoisMod.MOD_ID, value.pathName), value.get());
            if (value.burnTime != null) {
                FuelRegistry.INSTANCE.add(value.get(), value.burnTime);
            }
        }
    }

    public Item get() {
        if (item == null) {
            item = itemSupplier.get();
        }
        return item;
    }
}
