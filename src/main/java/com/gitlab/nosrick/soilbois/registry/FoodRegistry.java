package com.gitlab.nosrick.soilbois.registry;

import com.nhoryzon.mc.farmersdelight.registry.EffectsRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;

public enum FoodRegistry {

    RAW_SEITAN(4, 0.4f, false),
    COOKED_SEITAN(16, 0.8f, false),
    RAW_SEITAN_PATTY(1, 0.1f, false),
    COOKED_SEITAN_PATTY(4, 0.4f, false),
    RAW_SEITAN_BACON(1, 0f, false),
    SEITAN_BACON(2, 0.1f, false),
    SEITAN_SANDWICH(
            12,
            0.85f,
            new StatusEffectInstance(EffectsRegistry.NOURISHED.get()),
            100f,
            false,
            false,
            false),

    OAT_MILK(
            4,
            0.2f,
            new StatusEffectInstance(EffectsRegistry.COMFORT.get()),
            100f,
            false,
            true,
            true),
    PLAIN_PORRIDGE(
            10,
            0.7f,
            false),
    FRUIT_PORRIDGE(
            12,
            0.73f,
            false);

    private final FoodComponent food;

    FoodRegistry(int hunger, float saturation) {
        this(hunger, saturation, null, .0f, false, false, false);
    }

    FoodRegistry(int hunger, float saturation, boolean isMeat) {
        this(hunger, saturation, null, .0f, isMeat, false, false);
    }

    FoodRegistry(int hunger, float saturation, StatusEffectInstance effect, float effectChance) {
        this(hunger, saturation, effect, effectChance, false, false, false);
    }

    FoodRegistry(int hunger, float saturation, StatusEffectInstance effect, float effectChance, boolean isMeat) {
        this(hunger, saturation, effect, effectChance, isMeat, false, false);
    }

    FoodRegistry(int hunger, float saturation, StatusEffectInstance effect, float effectChance, boolean isMeat, boolean isFastToEat, boolean alwaysEdible) {
        FoodComponent.Builder builder = new FoodComponent.Builder();
        builder.hunger(hunger).saturationModifier(saturation);
        if (effect != null) {
            builder.statusEffect(effect, effectChance);
        }
        if (isMeat) {
            builder.meat();
        }
        if (isFastToEat) {
            builder.snack();
        }
        if (alwaysEdible) {
            builder.alwaysEdible();
        }

        food = builder.build();
    }

    public FoodComponent get() {
        return food;
    }
}
