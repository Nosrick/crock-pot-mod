package com.github.nosrick.crockpot.compat.appleskin;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.item.StewItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.item.ItemStack;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;

public class AppleSkinEventHandler implements AppleSkinApi {

    @Override
    public void registerEvents() {
        FoodValuesEvent.EVENT.register(
                foodValuesEvent -> {
                    if(foodValuesEvent.itemStack.getItem() instanceof StewItem) {
                        ItemStack itemStack = foodValuesEvent.itemStack;
                        foodValuesEvent.defaultFoodComponent = itemStack.getOrDefault(DataComponentTypes.FOOD, FoodComponents.APPLE);
                        foodValuesEvent.modifiedFoodComponent = foodValuesEvent.defaultFoodComponent;
                    }
                }
        );

        CrockPotMod.MODS_LOADED.add("appleskin");
    }
}
