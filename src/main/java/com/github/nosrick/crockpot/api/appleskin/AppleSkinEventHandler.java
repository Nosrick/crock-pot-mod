package com.github.nosrick.crockpot.api.appleskin;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.item.StewItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.event.TooltipOverlayEvent;
import squeek.appleskin.api.food.FoodValues;

import java.util.List;

public class AppleSkinEventHandler implements AppleSkinApi {

    @Override
    public void registerEvents() {
        FoodValuesEvent.EVENT.register(
                foodValuesEvent -> {
                    if(foodValuesEvent.itemStack.getItem() instanceof StewItem) {
                        ItemStack itemStack = foodValuesEvent.itemStack;
                        foodValuesEvent.defaultFoodValues = new FoodValues(
                                StewItem.getHunger(itemStack),
                                StewItem.getSaturation(itemStack));

                        foodValuesEvent.modifiedFoodValues = foodValuesEvent.defaultFoodValues;
                    }
                }
        );

        CrockPotMod.MODS_LOADED.add("appleskin");
    }
}
