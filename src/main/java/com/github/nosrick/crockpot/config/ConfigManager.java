package com.github.nosrick.crockpot.config;

import com.github.nosrick.crockpot.CrockPotMod;
//import com.github.nosrick.crockpot.compat.cloth.ClothConfigManager;

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
        /**/

        return true;
    }

    public static boolean useItemPositiveEffects() {
        /**/

        return true;
    }

    public static boolean useItemNegativeEffects() {
        /**/

        return true;
    }

    public static int boilTimePerLevel() {
        /**/

        return 20 * 60 * 2;
    }

    public static int minCowlLevel() {
        /**/

        return 5;
    }

    public static int maxBonusLevels() {
        /**/

        return 5;
    }

    public static int maxPortionsPerPot() {
        /**/

        return 64;
    }

    public static int stewMinPositiveLevelsEffect() {
        /**/

        return 5;
    }

    public static int stewMinNegativeLevelsEffect() {
        /**/

        return 1;
    }

    public static int maxStewNameLength() {
        /**/

        return 32;
    }

    public static int baseNauseaDuration() {
        /**/

        return 5;
    }

    public static boolean cappedNauseaDuration() {
        /**/

        return true;
    }

    public static int maxNauseaDuration() {
        /**/

        return baseNauseaDuration() * 20 * minCowlLevel();
    }

    public static int basePositiveDuration() {
        /**/

        return 5;
    }

    public static boolean cappedPositiveDuration() {
        /**/

        return true;
    }

    public static int maxPositiveDuration() {
        /**/

        return basePositiveDuration() * 20 * maxBonusLevels();
    }

    public static int minSatisfyingLevels() {
        /**/

        return 1;
    }

    public static int minFillingLevels() {
        /**/

        return 3;
    }

    public static int minHeartyLevels() {
        /**/

        return 5;
    }

    public static float soundEffectVolume() {
        /**/

        return 0.3f;
    }

    public static int bubbleSoundChance() {
        /**/

        return 100;
    }

    public static int boilSoundChance() {
        /**/

        return 100;
    }

    public static boolean useBubbleSound() {
        /**/

        return true;
    }

    public static boolean useBoilSound() {
        /**/

        return true;
    }

    public static int bubbleParticleChance() {
        /**/

        return 50;
    }

    public static int boilParticleChance() {
        /**/

        return 50;
    }

    public static boolean useBoilParticles() {
        /**/

        return true;
    }

    public static boolean useBubbleParticles() {
        /**/

        return true;
    }

    public static boolean animateBoilingLid() {
        /**/

        return true;
    }

    public static boolean redstoneNeedsPower() {
        /**/

        return false;
    }

    public static int redstonePowerThreshold() {
        /**/

        return 8;
    }

    public static boolean canFillWithWaterBottle() {
        /**/

        return true;
    }

    public static int ingredientSlots() {
        /**/

        return 8;
    }

    public static boolean cookRawFood() {
         /**/

         return true;
    }

    public static boolean cookFoodOnlyOnLevelUp() {
         /**/

         return true;
    }

    public static boolean canAddPotions() {
        /**/

        return true;
    }

    public static int effectPerPot() {
        /**/

        return 2;
    }

    public static boolean effectsOverride() {
        

        return true;
    }

    public static boolean hideStewEffects() {
        

        return false;
    }

    public static boolean useObfuscatedText() {
        

        return false;
    }

    public static boolean diluteEffects() {
        

        return true;
    }

    public static float dilutionModifier() {
        

        return 0.5f;
    }

    public static boolean canLockPots() {
        

        return true;
    }

    public static boolean creativePlayersIgnoreLocks() {
        

        return true;
    }

    public static boolean displayOwnerName() {
        

        return true;
    }

    public static boolean renderPadlock() {
        

        return true;
    }

    public static int textColor() {
        

        return 0xCCFFFFFF;
    }

    public static int labelBackgroundOpacity() {
        

        return 65;
    }
}
