package com.github.nosrick.crockpot.mixin;

import com.github.nosrick.crockpot.item.StewItem;
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
        if (cachedItem instanceof StewItem) {
            return new FoodComponent.Builder()
                    .hunger(StewItem.getHunger(cachedItemStack))
                    .saturationModifier(StewItem.getSaturation(cachedItemStack))
                    .statusEffect(StewItem.getStatusEffect(cachedItemStack), 1f)
                    .build();
        }

        return comp;
    }
}
