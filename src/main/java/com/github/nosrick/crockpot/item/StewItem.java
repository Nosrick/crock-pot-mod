package com.github.nosrick.crockpot.item;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.client.tooltip.StewContentsTooltip;
import com.github.nosrick.crockpot.config.ConfigManager;
import com.github.nosrick.crockpot.util.NbtListUtil;
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
import net.minecraft.potion.PotionUtil;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
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
    protected static final String EFFECTS_NBT = "Effects";
    protected static final String CURSED_NBT = "Cursed";

    public StewItem() {
        super(new FabricItemSettings()
                .food(
                        new FoodComponent.Builder()
                                .build())
                .recipeRemainder(Items.BOWL));
    }

    public static FoodComponent ConstructFoodComponent(ItemStack stack) {
        if(stack.getItem() instanceof StewItem) {
            return new FoodComponent.Builder()
                    .hunger(StewItem.getHunger(stack))
                    .saturationModifier(StewItem.getSaturation(stack))
                    .build();
        }

        return new FoodComponent.Builder().build();
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack playerStack = user.getStackInHand(hand);
        if(playerStack.getItem() instanceof StewItem) {
            CrockPotMod.FOOD_MANAGER.PlayerBeginsEating(user, StewItem.ConstructFoodComponent(playerStack));
        }

        return super.use(world, user, hand);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
        if(user instanceof PlayerEntity player) {
            CrockPotMod.FOOD_MANAGER.PlayerFinishesEating(player);
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack container = new ItemStack(stack.getItem().getRecipeRemainder());

        world.emitGameEvent(user, GameEvent.EAT, user.getBlockPos());
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
            FoodComponent foodComponent = CrockPotMod.FOOD_MANAGER.GetFoodForPlayer(player);
            player.getHungerManager().add(foodComponent.getHunger(), foodComponent.getSaturationModifier());
            List<StatusEffectInstance> statusEffects = PotionUtil.getPotionEffects(stack);

            if (!statusEffects.isEmpty()) {
                for (StatusEffectInstance effectInstance : statusEffects) {
                    player.addStatusEffect(effectInstance);
                }
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
                tooltip.set(0, Text.literal(tooltip.get(0).getString())
                        .setStyle(Style.EMPTY
                                .withColor(Formatting.DARK_GRAY)
                                .withItalic(true)
                                .withBold(true)));
                tooltip.add(Text.translatable("item.crockpot.stew.cowl_description"));
            } else if (getCurseLevel(stack) >= ConfigManager.stewMinNegativeLevelsEffect()) {
                tooltip.set(0, Text.literal(tooltip.get(0).getString())
                        .setStyle(Style.EMPTY
                                .withColor(Formatting.DARK_RED)
                                .withItalic(true)));
                tooltip.add(Text.translatable("item.crockpot.stew.cursed_description"));
            }
        }

        if (!CrockPotMod.MODS_LOADED.contains("appleskin")) {
            int hunger = getHunger(stack);
            int saturation = MathHelper.floor(hunger * getSaturation(stack) * 2f);

            tooltip.add(Text.translatable(
                            "item.crockpot.stew.hunger", hunger)
                    .setStyle(Style.EMPTY
                            .withColor(Formatting.YELLOW)));

            tooltip.add(Text.translatable(
                            "item.crockpot.stew.saturation", saturation)
                    .setStyle(Style.EMPTY
                            .withColor(Formatting.GOLD)));
        }
        tooltip.add(StewContentsTooltip.of(stack));

        List<StatusEffectInstance> statusEffects = PotionUtil.getPotionEffects(stack);
        if (!statusEffects.isEmpty()) {
            tooltip.add(Text.translatable("tooltip.crockpot.effects"));
            if(!ConfigManager.hideStewEffects()) {
                for (StatusEffectInstance effect : statusEffects) {
                    tooltip.add(Text.translatable(effect.getTranslationKey())
                            .append(Text.literal(" " + (effect.getAmplifier() + 1) + " - " + effect.getDuration() / 20))
                            .append(Text.translatable("tooltip.crockpot.seconds"))
                            .setStyle(Style.EMPTY)
                            .formatted(effect.getEffectType().isBeneficial()
                                    ? Formatting.GREEN
                                    : Formatting.RED));
                }
            }
            else {
                if(ConfigManager.useObfuscatedText()) {
                    tooltip.add(Text.literal("THIS DOES STUFF").setStyle(Style.EMPTY.withObfuscated(true)));
                }
                else {
                    tooltip.add(Text.translatable("tooltip.crockpot.hidden_effects"));
                }
            }
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

    public static int getCurseLevel(ItemStack stack) {
        return stack.getOrCreateNbt().getInt(CURSED_NBT);
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

    public static void addStatusEffect(ItemStack stack, StatusEffectInstance statusEffectInstance) {
        List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);
        effects.add(statusEffectInstance);

        PotionUtil.setCustomPotionEffects(stack, effects);
        stack.getOrCreateNbt().put(EFFECTS_NBT, NbtListUtil.nbtListFromStatusEffectInstances(effects));
    }

    public static List<StatusEffectInstance> getStatusEffects(ItemStack stack) {
        return PotionUtil.getCustomPotionEffects(stack);
    }
}
