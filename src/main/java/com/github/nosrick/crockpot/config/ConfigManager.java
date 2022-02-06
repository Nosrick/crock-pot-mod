package com.github.nosrick.crockpot.config;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.api.cloth.CrockPotConfig;

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
            return CrockPotConfig.getConfig().cursedStew;
        }

        return true;
    }

    public static boolean useItemPositiveEffects() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().itemPositiveEffects;
        }

        return true;
    }

    public static boolean useItemNegativeEffects() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().itemNegativeEffects;
        }

        return true;
    }

    public static int boilTimePerLevel() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().boilTicksPerLevel;
        }

        return 20 * 60 * 2;
    }

    public static int minCowlLevel() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().cowlCurseLevels;
        }

        return 5;
    }

    public static int maxBonusLevels() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().maxBonusLevels;
        }

        return 5;
    }

    public static int maxPortionsPerPot() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().maxPortions;
        }

        return 64;
    }

    public static int stewMinPositiveLevelsEffect() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().itemMinPositiveBonusLevel;
        }

        return 5;
    }

    public static int stewMinNegativeLevelsEffect() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().itemMinNegativeCurseLevel;
        }

        return 1;
    }

    public static int maxStewNameLength() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().maxStewNameLength;
        }

        return 32;
    }

    public static int baseNauseaDuration() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().baseNauseaDuration;
        }

        return 5;
    }

    public static boolean cappedNauseaDuration() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().cappedNauseaDuration;
        }

        return true;
    }

    public static int maxNauseaDuration() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().maxNauseaDuration;
        }

        return baseNauseaDuration() * 20 * minCowlLevel();
    }

    public static int minSatisfyingLevels() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().minSatisfyingLevel;
        }

        return 1;
    }

    public static int minFillingLevels() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().minFillingLevel;
        }

        return 3;
    }

    public static int minHeartyLevels() {
        if(clothPresent()) {
            return CrockPotConfig.getConfig().minHeartyLevel;
        }

        return 5;
    }
}
