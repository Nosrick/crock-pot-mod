package com.github.nosrick.crockpot.config;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.compat.cloth.ClothConfigManager;
import com.github.nosrick.crockpot.compat.cloth.CrockPotConfig;

public class ConfigManager {

    protected static boolean clothPresent = false;

    protected static boolean clothPresent() {
        boolean cloth = CrockPotMod.MODS_LOADED.contains("cloth");
        if(cloth != clothPresent) {
            clothPresent = cloth;
        }

        return clothPresent;
    }

    public static boolean useCursedStew() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.cursedStew;
        }

        return true;
    }

    public static boolean useItemPositiveEffects() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.itemPositiveEffects;
        }

        return true;
    }

    public static boolean useItemNegativeEffects() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.itemNegativeEffects;
        }

        return true;
    }

    public static int boilTimePerLevel() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.boilSecondsPerLevel * 20;
        }

        return 20 * 60 * 2;
    }

    public static int minCowlLevel() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.cowlCurseLevels;
        }

        return 5;
    }

    public static int maxBonusLevels() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.maxBonusLevels;
        }

        return 5;
    }

    public static int maxPortionsPerPot() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.maxPortions;
        }

        return 64;
    }

    public static int stewMinPositiveLevelsEffect() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.itemMinPositiveBonusLevel;
        }

        return 5;
    }

    public static int stewMinNegativeLevelsEffect() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.itemMinNegativeCurseLevel;
        }

        return 1;
    }

    public static int maxStewNameLength() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.maxStewNameLength;
        }

        return 32;
    }

    public static int baseNauseaDuration() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.baseNauseaDuration;
        }

        return 5;
    }

    public static boolean cappedNauseaDuration() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.cappedNauseaDuration;
        }

        return true;
    }

    public static int maxNauseaDuration() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.maxNauseaDuration * 20;
        }

        return baseNauseaDuration() * 20 * minCowlLevel();
    }

    public static int basePositiveDuration() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.basePositiveDuration;
        }

        return 5;
    }

    public static boolean cappedPositiveDuration() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.cappedPositiveDuration;
        }

        return true;
    }

    public static int maxPositiveDuration() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.maxPositiveDuration * 20;
        }

        return basePositiveDuration() * 20 * maxBonusLevels();
    }

    public static int minSatisfyingLevels() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.minSatisfyingLevel;
        }

        return 1;
    }

    public static int minFillingLevels() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.minFillingLevel;
        }

        return 3;
    }

    public static int minHeartyLevels() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.minHeartyLevel;
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
            return ClothConfigManager.getConfig().gameplay.redstoneNeedsPower;
        }

        return false;
    }

    public static int redstonePowerThreshold() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.redstonePowerThreshold;
        }

        return 8;
    }

    public static boolean canFillWithWaterBottle() {
        if(clothPresent()) {
            return ClothConfigManager.getConfig().gameplay.canFillWithWaterBottle;
        }

        return true;
    }
}
