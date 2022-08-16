package com.github.nosrick.crockpot.compat.cloth;

import com.github.nosrick.crockpot.CrockPotMod;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = CrockPotMod.MOD_ID)
public class CrockPotConfig implements ConfigData {

    public String nothing = "Move along.";

    @ConfigEntry.Category("stews")
    @ConfigEntry.Gui.TransitiveObject
    public StewGameplayCategory stews = new StewGameplayCategory();

    @Config(name = "stews")
    public static class StewGameplayCategory implements ConfigData {

        public int maxBonusLevels = 5;

        @Comment("How gross is too gross?")
        public int cowlCurseLevels = 5;

        @Comment("Is reboiling stew really all that bad?")
        public boolean cursedStew = true;

        public int boilSecondsPerLevel = 60 * 2;
        public int maxPortions = 64;

        public boolean itemPositiveEffects = true;
        public int itemMinPositiveBonusLevel = 5;
        public boolean itemNegativeEffects = true;
        public int itemMinNegativeCurseLevel = 2;

        public int maxStewNameLength = 32;

        @Comment("It's probably a good idea to keep this enabled")
        public boolean cappedNauseaDuration = true;

        @Comment("Number of seconds added to the nausea effect per curse level")
        public int baseNauseaDuration = 5;

        public int maxNauseaMultiplier = baseNauseaDuration * cowlCurseLevels;

        public boolean cappedPositiveDuration = true;

        @Comment("Number of seconds to add to the saturation effect per bonus level")
        public int basePositiveDuration = 5;

        public int maxPositiveMultiplier = basePositiveDuration * maxBonusLevels;

        @Comment("Minimum bonus levels before a stew can be called 'satisfying'")
        public int minSatisfyingLevel = 1;
        @Comment("Minimum bonus levels before a stew can be called 'filling'")
        public int minFillingLevel = 3;
        @Comment("Minimum bonus levels before a stew can be called 'hearty'")
        public int minHeartyLevel = 5;
    }

    @ConfigEntry.Category("mechanics")
    @ConfigEntry.Gui.TransitiveObject
    public MechanicsCategory mechanics = new MechanicsCategory();

    @Config(name = "mechanics")
    public static class MechanicsCategory implements ConfigData {
        @Comment("A bottle is the same as a bucket, right?")
        public boolean canFillWithWaterBottle = true;

        public boolean redstoneNeedsPower = false;

        @Comment("What's the minimum signal strength a redstone pot needs?")
        public int redstonePowerThreshold = 8;

        @Comment("How many different foods can go into a single pot")
        public int ingredientSlots = 8;
    }

    @ConfigEntry.Category("potions")
    @ConfigEntry.Gui.TransitiveObject
    public PotionsCategory potions = new PotionsCategory();

    @Config(name = "potions")
    public static class PotionsCategory implements ConfigData {
        public boolean canAddPotions = true;

        public int effectsPerPot = 2;

        public boolean effectsOverride = true;

        public boolean hideStewEffects = false;

        public boolean useObfuscatedText = false;

        public boolean diluteEffects = true;

        @Comment("effect duration / (portions * dilutionModifier)")
        public float dilutionModifier = 0.5f;
    }


    @ConfigEntry.Category("locking")
    @ConfigEntry.Gui.TransitiveObject
    public LockingCategory locking = new LockingCategory();

    @Config(name = "locking")
    public static class LockingCategory implements ConfigData {
        public boolean canLockPots = true;

        public boolean creativePlayersIgnoreLocks = true;

        public boolean displayOwnerName = true;

        public boolean renderPadlock = true;

        @ConfigEntry.ColorPicker
        public int textColor = 0xFFFFFF;

        @ConfigEntry.BoundedDiscrete(min = 0, max = 255)
        public int labelBackgroundOpacity = 65;
    }

    @ConfigEntry.Category("sound")
    @ConfigEntry.Gui.TransitiveObject
    public SoundEffects sound = new SoundEffects();

    @Config(name = "sound")
    public static class SoundEffects implements ConfigData {
        @Comment("This is a 1/X chance of sound per tick")
        public int boilSoundChance = 100;

        @Comment("The default value is about 5% per second")
        public int bubbleSoundChance = 100;

        public boolean useBoilSound = true;
        public boolean useBubbleSound = true;

        public float soundEffectVolume = 0.3f;
    }

    @ConfigEntry.Category("graphics")
    @ConfigEntry.Gui.TransitiveObject
    public GraphicsCategory graphics = new GraphicsCategory();

    @Config(name = "graphics")
    public static class GraphicsCategory implements ConfigData {
        @Comment("This is a 1/X chance of a particle per tick")
        public int boilParticleChance = 50;

        @Comment("The default value is about 10% per tick")
        public int bubbleParticleChance = 50;

        public boolean useBoilParticles = true;
        public boolean useBubbleParticles = true;

        public boolean animateBoilingLid = true;
    }
}
