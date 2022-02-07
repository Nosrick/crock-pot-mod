package com.github.nosrick.crockpot.config;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.compat.cloth.CrockPotConfig;

public class ConfigManager {

    protected static boolean clothPresent = false;

    protected static boolean clothPresent() {
        boolean cloth = CrockPotMod.MODS_LOADED.contains("cloth");
        if(cloth != clothPresent){
            clothPresent = cloth;
        }

        return clothPresent;
    }

    public static boolean useCursedStew() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.cursedStew;
        }

        return true;
    }

    public static boolean useItemPositiveEffects() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.itemPositiveEffects;
        }

        return true;
    }

    public static boolean useItemNegativeEffects() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.itemNegativeEffects;
        }

        return true;
    }

    public static int boilTimePerLevel() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.boilTicksPerLevel;
        }

        return 20 * 60 * 2;
    }

    public static int minCowlLevel() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.cowlCurseLevels;
        }

        return 5;
    }

    public static int maxBonusLevels() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.maxBonusLevels;
        }

        return 5;
    }

    public static int maxPortionsPerPot() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.maxPortions;
        }

        return 64;
    }

    public static int stewMinPositiveLevelsEffect() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.itemMinPositiveBonusLevel;
        }

        return 5;
    }

    public static int stewMinNegativeLevelsEffect() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.itemMinNegativeCurseLevel;
        }

        return 1;
    }

    public static int maxStewNameLength() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.maxStewNameLength;
        }

        return 32;
    }

    public static int baseNauseaDuration() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.baseNauseaDuration;
        }

        return 5;
    }

    public static boolean cappedNauseaDuration() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.cappedNauseaDuration;
        }

        return true;
    }

    public static int maxNauseaDuration() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.maxNauseaDuration;
        }

        return baseNauseaDuration() * 20 * minCowlLevel();
    }

    public static int basePositiveDuration() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.basePositiveDuration;
        }

        return 5;
    }

    public static boolean cappedPositiveDuration() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.cappedPositiveDuration;
        }

        return true;
    }

    public static int maxPositiveDuration() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.maxPositiveDuration;
        }

        return basePositiveDuration() * 20 * maxBonusLevels();
    }

    public static int minSatisfyingLevels() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.minSatisfyingLevel;
        }

        return 1;
    }

    public static int minFillingLevels() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.minFillingLevel;
        }

        return 3;
    }

    public static int minHeartyLevels() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().gameplay.minHeartyLevel;
        }

        return 5;
    }

    public static float soundEffectVolume() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().sound.soundEffectVolume;
        }

        return 0.3f;
    }

    public static int bubbleSoundChance() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().sound.bubbleSoundChance;
        }

        return 100;
    }

    public static int boilSoundChance() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().sound.boilSoundChance;
        }

        return 100;
    }

    public static boolean useBubbleSound() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().sound.useBubbleSound;
        }

        return true;
    }

    public static boolean useBoilSound() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().sound.useBoilSound;
        }

        return true;
    }

    public static int bubbleParticleChance() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().graphics.bubbleParticleChance;
        }

        return 50;
    }

    public static int boilParticleChance() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().graphics.boilParticleChance;
        }

        return 50;
    }

    public static boolean useBoilParticles() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().graphics.useBoilParticles;
        }

        return true;
    }

    public static boolean useBubbleParticles() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().graphics.useBubbleParticles;
        }

        return true;
    }

    public static boolean animateBoilingLid() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().graphics.animateBoilingLid;
        }

        return true;
    }
}
