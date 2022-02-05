package com.gitlab.nosrick.crockpot.api.appleskin;

import com.gitlab.nosrick.crockpot.CrockPotMod;
import com.gitlab.nosrick.crockpot.item.StewItem;
import com.gitlab.nosrick.crockpot.registry.ItemRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

        TooltipOverlayEvent.Render.EVENT.register(
                tooltipOverlayEvent -> {
                    if(tooltipOverlayEvent.itemStack.getItem() instanceof StewItem) {
                        List<Item> contents = StewItem.getContents(tooltipOverlayEvent.itemStack);
                        for(int i = 0; i < contents.size(); i++) {
                            String content = "textures/item/" + Registry.ITEM.getId(contents.get(i)).getPath();
                            content = content.replace('.', '/').concat(".png");
                            RenderSystem.setShaderTexture(0, new Identifier(content));
                            int hunger = tooltipOverlayEvent.modifiedFood.hunger;
                            int saturation = (int) (hunger * tooltipOverlayEvent.modifiedFood.saturationModifier);
                            int larger = Math.max(hunger, saturation) / 2;
                            DrawableHelper.drawTexture(tooltipOverlayEvent.matrixStack, tooltipOverlayEvent.x + (larger * 8) + i * 8, tooltipOverlayEvent.y, 0, 0, 16, 16, 16, 16);
                        }
                    }
                }
        );

        CrockPotMod.MODS_LOADED.add("appleskin");
    }
}
