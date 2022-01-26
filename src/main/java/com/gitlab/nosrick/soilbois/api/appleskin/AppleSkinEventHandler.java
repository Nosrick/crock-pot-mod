package com.gitlab.nosrick.soilbois.api.appleskin;

import com.gitlab.nosrick.soilbois.item.TagineItem;
import net.minecraft.item.ItemStack;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;

public class AppleSkinEventHandler implements AppleSkinApi {

    @Override
    public void registerEvents() {
        FoodValuesEvent.EVENT.register(
                foodValuesEvent -> {
                    if(foodValuesEvent.itemStack.getItem() instanceof TagineItem) {
                        ItemStack itemStack = foodValuesEvent.itemStack;
                        foodValuesEvent.modifiedFoodValues = new FoodValues(
                                TagineItem.getHunger(itemStack),
                                TagineItem.getSaturation(itemStack));
                    }
                }
        );
    }
}
