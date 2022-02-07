package com.github.nosrick.crockpot.item;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.client.tooltip.StewContentsTooltip;
import com.github.nosrick.crockpot.config.ConfigManager;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StewItem extends Item {

    protected static final String CONTENTS_NBT = "Contents";
    protected static final String HUNGER_NBT = "Hunger";
    protected static final String SATURATION_NBT = "Saturation";
    protected static final String EFFECT_NAME_NBT = "Effect Name";
    protected static final String EFFECT_DURATION_NBT = "Effect Duration";
    protected static final String EFFECT_AMP_NBT = "Effect Amplification";
    protected static final String CURSED_NBT = "Cursed";

    public StewItem() {
        super(new FabricItemSettings()
                .food(
                        new FoodComponent.Builder()
                                .build())
                .recipeRemainder(Items.BOWL));
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
            StatusEffectInstance statusEffectInstance = getStatusEffect(stack);
            if (statusEffectInstance != null) {
                player.addStatusEffect(statusEffectInstance);
            }
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

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        if (ConfigManager.useCursedStew()) {
            if (getCurseLevel(stack) >= ConfigManager.minCowlLevel()) {
                tooltip.set(0, new LiteralText(tooltip.get(0).getString())
                        .setStyle(Style.EMPTY
                                .withColor(Formatting.DARK_GRAY)
                                .withItalic(true)
                                .withBold(true)));
                tooltip.add(new TranslatableText("item.crockpot.stew.cowl_description"));
            } else if (getCurseLevel(stack) >= ConfigManager.stewMinNegativeLevelsEffect()) {
                tooltip.set(0, new LiteralText(tooltip.get(0).getString())
                        .setStyle(Style.EMPTY
                                .withColor(Formatting.DARK_RED)
                                .withItalic(true)));
                tooltip.add(new TranslatableText("item.crockpot.stew.cursed_description"));
            }
        }

        if (!CrockPotMod.MODS_LOADED.contains("appleskin")) {
            int hunger = getHunger(stack);
            int saturation = MathHelper.floor(hunger * getSaturation(stack) * 2f);

            tooltip.add(new TranslatableText(
                    "item.crockpot.stew.hunger", hunger)
                    .setStyle(Style.EMPTY
                            .withColor(Formatting.YELLOW)));

            tooltip.add(new TranslatableText(
                    "item.crockpot.stew.saturation", saturation)
                    .setStyle(Style.EMPTY
                            .withColor(Formatting.GOLD)));
        }
        tooltip.add(new StewContentsTooltip(stack));

        StatusEffectInstance effectInstance = getStatusEffect(stack);
        if (effectInstance != null) {
            tooltip.add(new TranslatableText("tooltip.crockpot.effect",
                    new TranslatableText(effectInstance.getTranslationKey())
                            .append(new LiteralText(" " + (effectInstance.getAmplifier() + 1) + " - " + (effectInstance.getDuration() / 20))))
                    .setStyle(Style.EMPTY)
                    .formatted(effectInstance.getEffectType().isBeneficial()
                            ? Formatting.GREEN
                            : Formatting.RED));
        }
    }

    public static int getHunger(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(HUNGER_NBT);
    }

    public static float getSaturation(ItemStack stack) {
        return stack.getOrCreateNbt().getFloat(SATURATION_NBT);
    }

    public static List<Item> getContents(ItemStack stack) {
        NbtList list = stack.getOrCreateNbt().getList(CONTENTS_NBT, 8);
        List<Item> returnItems = new ArrayList<>();
        list.stream().map(NbtElement::asString).forEach(string -> returnItems.add(Registry.ITEM.get(new Identifier(string))));

        return returnItems;
    }

    public static StatusEffectInstance getStatusEffect(ItemStack stack) {
        String name = stack.getOrCreateNbt().getString(EFFECT_NAME_NBT);
        StatusEffect statusEffect = Registry.STATUS_EFFECT.get(new Identifier(name));
        if (statusEffect == null) {
            return null;
        }

        int duration = stack.getOrCreateNbt().getInt(EFFECT_DURATION_NBT);
        int amp = stack.getOrCreateNbt().getInt(EFFECT_AMP_NBT);
        return new StatusEffectInstance(statusEffect, duration, amp);
    }

    public static int getCurseLevel(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(CURSED_NBT);
    }

    public static void setContents(ItemStack stack, List<String> contents) {
        NbtList list = new NbtList();
        list.addAll(contents.stream().map(NbtString::of).toList());
        stack.getOrCreateNbt().put(CONTENTS_NBT, list);
    }

    public static void setContents(ItemStack stack, DefaultedList<ItemStack> contents) {
        NbtList list = new NbtList();
        List<String> strings = contents
                .stream()
                .map(content -> Registry.ITEM
                        .getId(content.getItem())
                        .toString())
                .toList();

        list.addAll(strings.stream().map(NbtString::of).toList());
        stack.getOrCreateNbt().put(CONTENTS_NBT, list);
    }

    public static void setHunger(ItemStack stack, int hunger) {
        stack.getOrCreateNbt().putInt(HUNGER_NBT, hunger);
    }

    public static void setSaturation(ItemStack stack, float saturation) {
        stack.getOrCreateNbt().putFloat(SATURATION_NBT, saturation);
    }

    public static void setCurseLevel(ItemStack stack, int curseLevel) {
        stack.getOrCreateNbt().putInt(CURSED_NBT, curseLevel);
    }

    public static void setStatusEffect(ItemStack stack, StatusEffectInstance statusEffectInstance) {
        Identifier statusId = Registry.STATUS_EFFECT.getId(statusEffectInstance.getEffectType());
        if (statusId != null) {
            stack.getOrCreateNbt().putString(EFFECT_NAME_NBT, statusId.toString());
            stack.getOrCreateNbt().putInt(EFFECT_DURATION_NBT, statusEffectInstance.getDuration());
            stack.getOrCreateNbt().putInt(EFFECT_AMP_NBT, statusEffectInstance.getAmplifier());
        }
    }
}
