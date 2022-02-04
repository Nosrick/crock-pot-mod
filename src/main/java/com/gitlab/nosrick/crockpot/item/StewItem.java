package com.gitlab.nosrick.crockpot.item;

import com.gitlab.nosrick.crockpot.CrockPotMod;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class StewItem extends Item {

    protected static final String CONTENTS_NBT = "Contents";
    protected static final String HUNGER_NBT = "Hunger";
    protected static final String SATURATION_NBT = "Saturation";

    public StewItem() {
        super(new ModItemSettings()
                .food(
                        new FoodComponent.Builder()
                                .build()));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack container = new ItemStack(stack.getItem().getRecipeRemainder());

        world.emitGameEvent(GameEvent.EAT, user);
        world.playSound(
                user.getX(),
                user.getY(),
                user.getZ(),
                user.getEatSound(stack),
                SoundCategory.NEUTRAL,
                1.0f,
                1.0f,
                true);

        if (user instanceof PlayerEntity player) {
            player.getHungerManager().eat(this, stack);
            player.incrementStat(Stats.USED.getOrCreateStat(this));

            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        if (stack.isEmpty()) {
            return container;
        } else {
            if (user instanceof PlayerEntity player) {
                if (!player.getAbilities().creativeMode
                        && !player.getInventory().insertStack(container)) {
                    player.dropStack(container);
                } else {
                    player.giveItemStack(container);
                }
            }

            return stack;
        }
    }

    public static List<String> getContents(ItemStack stack) {
        NbtList list = stack.getOrCreateNbt().getList(CONTENTS_NBT, 8);
        return list.stream().map(NbtElement::asString).collect(Collectors.toList());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        if(context.isAdvanced()) {
            if (!CrockPotMod.MODS_LOADED.contains("appleskin")) {
                tooltip.add(new TranslatableText(
                        "item.crockpot.stew.hunger",
                        getHunger(stack))
                        .setStyle(Style.EMPTY
                                .withColor(Formatting.YELLOW)));

                tooltip.add(new TranslatableText(
                        "item.crockpot.stew.saturation",
                        String.format("%.2f",
                                getSaturation(stack)))
                        .setStyle(Style.EMPTY
                                .withColor(Formatting.GOLD)));
            }
        }
    }

    public static int getHunger(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(HUNGER_NBT);
    }

    public static float getSaturation(ItemStack stack) {
        return stack.getOrCreateNbt().getFloat(SATURATION_NBT);
    }

    public static void setContents(ItemStack stack, List<String> contents) {
        NbtList list = new NbtList();
        list.addAll(contents.stream().map(NbtString::of).toList());
        stack.getOrCreateNbt().put(CONTENTS_NBT, list);
    }

    public static void setHunger(ItemStack stack, int hunger) {
        stack.getOrCreateNbt().putInt(HUNGER_NBT, hunger);
    }

    public static void setSaturation(ItemStack stack, float saturation) {
        stack.getOrCreateNbt().putFloat(SATURATION_NBT, saturation);
    }
}
