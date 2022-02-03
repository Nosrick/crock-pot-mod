package com.gitlab.nosrick.crockpot.api.appleskin;

import com.gitlab.nosrick.crockpot.CrockPotMod;
import com.gitlab.nosrick.crockpot.item.StewItem;
import net.minecraft.item.ItemStack;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;

public class AppleSkinEventHandler implements AppleSkinApi {

    @Override
    public void registerEvents() {
        FoodValuesEvent.EVENT.register(
                foodValuesEvent -> {
                    if(foodValuesEvent.itemStack.getItem() instanceof StewItem) {
                        ItemStack itemStack = foodValuesEvent.itemStack;
                        foodValuesEvent.modifiedFoodValues = new FoodValues(
                                StewItem.getHunger(itemStack),
                                StewItem.getSaturation(itemStack));
                    }
                }
        );

        CrockPotMod.MODS_LOADED.add("appleskin");
    }
}
