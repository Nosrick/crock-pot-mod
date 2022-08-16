package com.github.nosrick.crockpot.config;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.compat.cloth.ClothConfigManager;

public class ConfigManager {

    protected static boolean clothPresent = false;

    public static boolean clothPresent() {
        boolean cloth = CrockPotMod.MODS_LOADED.contains("cloth");
        if(cloth != clothPresent) {
            clothPresent = cloth;
        }

        return clothPresent;
    }

    public static boolean useCursedStew() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.cursedStew;
        }

        return true;
    }

    public static boolean useItemPositiveEffects() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.itemPositiveEffects;
        }

        return true;
    }

    public static boolean useItemNegativeEffects() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.itemNegativeEffects;
        }

        return true;
    }

    public static int boilTimePerLevel() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.boilSecondsPerLevel * 20;
        }

        return 20 * 60 * 2;
    }

    public static int minCowlLevel() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.cowlCurseLevels;
        }

        return 5;
    }

    public static int maxBonusLevels() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.maxBonusLevels;
        }

        return 5;
    }

    public static int maxPortionsPerPot() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.maxPortions;
        }

        return 64;
    }

    public static int stewMinPositiveLevelsEffect() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.itemMinPositiveBonusLevel;
        }

        return 5;
    }

    public static int stewMinNegativeLevelsEffect() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.itemMinNegativeCurseLevel;
        }

        return 1;
    }

    public static int maxStewNameLength() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.maxStewNameLength;
        }

        return 32;
    }

    public static int baseNauseaDuration() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.baseNauseaDuration;
        }

        return 5;
    }

    public static boolean cappedNauseaDuration() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.cappedNauseaDuration;
        }

        return true;
    }

    public static int maxNauseaDuration() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.maxNauseaMultiplier * 20;
        }

        return baseNauseaDuration() * 20 * minCowlLevel();
    }

    public static int basePositiveDuration() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.basePositiveDuration;
        }

        return 5;
    }

    public static boolean cappedPositiveDuration() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.cappedPositiveDuration;
        }

        return true;
    }

    public static int maxPositiveDuration() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.maxPositiveMultiplier * 20;
        }

        return basePositiveDuration() * 20 * maxBonusLevels();
    }

    public static int minSatisfyingLevels() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.minSatisfyingLevel;
        }

        return 1;
    }

    public static int minFillingLevels() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.minFillingLevel;
        }

        return 3;
    }

    public static int minHeartyLevels() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().stews.minHeartyLevel;
        }

        return 5;
    }

    public static float soundEffectVolume() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().sound.soundEffectVolume;
        }

        return 0.3f;
    }

    public static int bubbleSoundChance() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().sound.bubbleSoundChance;
        }

        return 100;
    }

    public static int boilSoundChance() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().sound.boilSoundChance;
        }

        return 100;
    }

    public static boolean useBubbleSound() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().sound.useBubbleSound;
        }

        return true;
    }

    public static boolean useBoilSound() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().sound.useBoilSound;
        }

        return true;
    }

    public static int bubbleParticleChance() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().graphics.bubbleParticleChance;
        }

        return 50;
    }

    public static int boilParticleChance() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().graphics.boilParticleChance;
        }

        return 50;
    }

    public static boolean useBoilParticles() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().graphics.useBoilParticles;
        }

        return true;
    }

    public static boolean useBubbleParticles() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().graphics.useBubbleParticles;
        }

        return true;
    }

    public static boolean animateBoilingLid() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().graphics.animateBoilingLid;
        }

        return true;
    }

    public static boolean redstoneNeedsPower() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().mechanics.redstoneNeedsPower;
        }

        return false;
    }

    public static int redstonePowerThreshold() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().mechanics.redstonePowerThreshold;
        }

        return 8;
    }

    public static boolean canFillWithWaterBottle() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().mechanics.canFillWithWaterBottle;
        }

        return true;
    }

    public static int ingredientSlots() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().mechanics.ingredientSlots;
        }

        return 8;
    }

    public static boolean canAddPotions() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().potions.canAddPotions;
        }

        return true;
    }

    public static int effectPerPot() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().potions.effectsPerPot;
        }

        return 2;
    }

    public static boolean effectsOverride() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().potions.effectsOverride;
        }

        return true;
    }

    public static boolean hideStewEffects() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().potions.hideStewEffects;
        }

        return false;
    }

    public static boolean useObfuscatedText() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().potions.useObfuscatedText;
        }

        return false;
    }

    public static boolean diluteEffects() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().potions.diluteEffects;
        }

        return true;
    }

    public static float dilutionModifier() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().potions.dilutionModifier;
        }

        return 0.5f;
    }

    public static boolean canLockPots() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().locking.canLockPots;
        }

        return true;
    }

    public static boolean creativePlayersIgnoreLocks() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().locking.creativePlayersIgnoreLocks;
        }

        return true;
    }

    public static boolean displayOwnerName() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().locking.displayOwnerName;
        }

        return true;
    }

    public static boolean renderPadlock() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().locking.renderPadlock;
        }

        return true;
    }

    public static int textColor() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().locking.textColor;
        }

        return 0xCCFFFFFF;
    }

    public static int labelBackgroundOpacity() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().locking.labelBackgroundOpacity;
        }

        return 65;
    }
}
