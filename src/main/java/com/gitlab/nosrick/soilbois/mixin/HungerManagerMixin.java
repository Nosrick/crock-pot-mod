package com.gitlab.nosrick.soilbois.mixin;

import com.gitlab.nosrick.soilbois.item.TagineItem;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @Unique
    protected Item cachedItem;

    @Unique
    protected ItemStack cachedItemStack;

    @Inject(at = @At("HEAD"), method = "eat")
    protected void cacheEatParams(Item item, ItemStack itemStack, CallbackInfo info) {
        cachedItem = item;
        cachedItemStack = itemStack;
    }

    @ModifyVariable(
            at = @At(value = "INVOKE_ASSIGN",
                    target = "net/minecraft/item/Item.getFoodComponent()Lnet/minecraft/item/FoodComponent;"),
            method = "eat",
            ordinal = 0)
    private FoodComponent eat(FoodComponent comp) {
        if (cachedItem instanceof TagineItem) {
            return new FoodComponent.Builder()
                    .hunger(TagineItem.getHunger(cachedItemStack))
                    .saturationModifier(TagineItem.getSaturation(cachedItemStack))
                    .build();
        }

        return comp;
    }
}
