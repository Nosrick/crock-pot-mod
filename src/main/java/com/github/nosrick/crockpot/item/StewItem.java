package com.github.nosrick.crockpot.item;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.client.tooltip.StewContentsTooltip;
import com.github.nosrick.crockpot.config.ConfigManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.component.type.FoodComponent.Builder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

public class StewItem extends Item {

    protected static final String CONTENTS_NBT = "Contents";
    protected static final String HUNGER_NBT = "Hunger";
    protected static final String SATURATION_NBT = "Saturation";
    protected static final String EFFECTS_NBT = "Effects";
    protected static final String CURSED_NBT = "Cursed";

    public StewItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack playerStack = user.getStackInHand(hand);
        if (playerStack.getItem() instanceof StewItem) {
            CrockPotMod.FOOD_MANAGER.PlayerBeginsEating(user, playerStack.getOrDefault(DataComponentTypes.FOOD, FoodComponents.APPLE));
        }

        return super.use(world, user, hand);
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        if (user instanceof PlayerEntity player) {
            CrockPotMod.FOOD_MANAGER.PlayerFinishesEating(player);
            return true;
        }

        return false;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack container = stack.getItem().getRecipeRemainder();

        world.emitGameEvent(user, GameEvent.EAT, user.getBlockPos());
        world.playSound(
                user,
                user.getBlockPos(),
                SoundEvents.ENTITY_GENERIC_EAT.value(),
                SoundCategory.NEUTRAL,
                1.0f,
                1.0f);

        if (user instanceof PlayerEntity player) {
            FoodComponent foodComponent = CrockPotMod.FOOD_MANAGER.GetFoodForPlayer(player);
            player.getHungerManager().add(foodComponent.nutrition(), foodComponent.saturation());
            var statusEffects = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getEffects();

            for (StatusEffectInstance effectInstance : statusEffects) {
                player.addStatusEffect(effectInstance);
            }

            player.incrementStat(Stats.USED.getOrCreateStat(this));

            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }

            CrockPotMod.FOOD_MANAGER.PlayerFinishesEating(player);
        }

        if (stack.isEmpty()) {
            return container;
        } else {
            if (user instanceof PlayerEntity player) {
                if (!player.getAbilities().creativeMode) {
                    if (!player.getInventory().insertStack(container)
                            && world instanceof ServerWorld serverWorld) {
                        player.dropStack(serverWorld, container);
                    } else {
                        player.giveItemStack(container);
                    }
                }
            }

            return stack;
        }
    }

    @Override
    public void appendTooltip(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplayComponent displayComponent,
            Consumer<Text> textConsumer,
            TooltipType type) {

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);

        if (ConfigManager.useCursedStew()) {
            if (getCurseLevel(stack) >= ConfigManager.minCowlLevel()) {
                textConsumer.accept(Text.translatable("item.crockpot.stew.cowl_description")
                        .setStyle(Style.EMPTY
                                .withColor(Formatting.DARK_GRAY)
                                .withItalic(true)
                                .withBold(true)));
            } else if (getCurseLevel(stack) >= ConfigManager.stewMinNegativeLevelsEffect()) {
                textConsumer.accept(Text.translatable("item.crockpot.stew.cursed_description")
                        .setStyle(Style.EMPTY
                                .withColor(Formatting.DARK_RED)
                                .withItalic(true)));
            }
        }

        if (!CrockPotMod.MODS_LOADED.contains("appleskin")) {
            int hunger = getHunger(stack);
            String saturation = String.format("%.2g", getSaturation(stack));

            textConsumer.accept(Text.translatable(
                            "item.crockpot.stew.hunger", hunger)
                    .setStyle(Style.EMPTY
                            .withColor(Formatting.YELLOW)));

            textConsumer.accept(Text.translatable(
                            "item.crockpot.stew.saturation", saturation)
                    .setStyle(Style.EMPTY
                            .withColor(Formatting.GOLD)));
        }
        textConsumer.accept(StewContentsTooltip.of(stack));
    }

    public static int getHunger(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.FOOD, FoodComponents.APPLE).nutrition();
    }

    public static float getSaturation(ItemStack stack) {

        return stack.getOrDefault(DataComponentTypes.FOOD, FoodComponents.APPLE).saturation();
    }

    public static List<Item> getContents(ItemStack stack) {
        var nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        List<Item> returnItems = new ArrayList<>();
        if (nbt == null) {
            return returnItems;
        }

        NbtCompound value = nbt.copyNbt();
        NbtList contents = (NbtList) value.get(CONTENTS_NBT);
        contents.stream().map(NbtElement::asString).forEach(string -> returnItems.add(Registries.ITEM.getEntry(Identifier.of(string.orElse(""))).get().value()));

        return returnItems;
    }

    public static int getCurseLevel(ItemStack stack) {
        var nbt = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (nbt == null) {
            return 0;
        }

        NbtCompound value = nbt.copyNbt();
        return value.getInt(CURSED_NBT).orElse(0);
    }

    public static void setContents(ItemStack stack, DefaultedList<ItemStack> contents) {
        NbtList list = new NbtList();
        List<String> strings = contents
                .stream()
                .map(content -> Registries.ITEM
                        .getId(content.getItem())
                        .toString())
                .toList();

        list.addAll(strings.stream().map(NbtString::of).toList());
        NbtCompound compound = new NbtCompound();
        compound.put(CONTENTS_NBT, list);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));
    }

    public static void setHunger(ItemStack stack, int hunger) {
        var oldFood = stack.getOrDefault(DataComponentTypes.FOOD, FoodComponents.DRIED_KELP);
        var builder = new Builder()
                .nutrition(hunger)
                .saturationModifier(oldFood.saturation());
        FoodComponent newFoodComponent = builder.build();
        stack.set(DataComponentTypes.FOOD, newFoodComponent);
    }

    public static void setSaturation(ItemStack stack, float saturation) {
        var oldFood = stack.getOrDefault(DataComponentTypes.FOOD, FoodComponents.DRIED_KELP);
        var builder = new Builder()
                .nutrition(oldFood.nutrition())
                .saturationModifier(saturation);
        FoodComponent newFoodComponent = builder.build();
        stack.set(DataComponentTypes.FOOD, newFoodComponent);
    }

    public static void setCurseLevel(ItemStack stack, int curseLevel) {
        NbtCompound newCurse = new NbtCompound();
        newCurse.putInt(CURSED_NBT, curseLevel);
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, newCurse);
    }

    public static void addStatusEffect(ItemStack stack, StatusEffectInstance statusEffectInstance) {
        var component = stack.getOrDefault(
                        DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT)
                .with(statusEffectInstance);

        stack.set(DataComponentTypes.POTION_CONTENTS, component);
    }

    public static List<StatusEffectInstance> getStatusEffects(ItemStack stack) {

        return StreamSupport.stream(
                        stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT)
                                .getEffects()
                                .spliterator(),
                        false)
                .toList();
    }
}
