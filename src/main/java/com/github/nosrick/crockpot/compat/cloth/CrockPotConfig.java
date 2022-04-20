package com.github.nosrick.crockpot.compat.cloth;

import com.github.nosrick.crockpot.CrockPotMod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = CrockPotMod.MOD_ID)
public class CrockPotConfig implements ConfigData {

    public String resetMe = "Delete me to reset.";

    @ConfigEntry.Category("gameplay")
    @ConfigEntry.Gui.TransitiveObject
    public GameplayCategory gameplay = new GameplayCategory();

    @Config(name = "gameplay")
    public static class GameplayCategory implements ConfigData {
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

        public int maxNauseaDuration = baseNauseaDuration * cowlCurseLevels;

        @Comment("Too much of a good thing, right?")
        public boolean cappedPositiveDuration = true;

        @Comment("Number of seconds to add to the saturation effect per bonus level")
        public int basePositiveDuration = 5;

        public int maxPositiveDuration = basePositiveDuration * maxBonusLevels;

        @Comment("Minimum bonus levels before a stew can be called 'satisfying'")
        public int minSatisfyingLevel = 1;
        @Comment("Minimum bonus levels before a stew can be called 'filling'")
        public int minFillingLevel = 3;
        @Comment("Minimum bonus levels before a stew can be called 'hearty'")
        public int minHeartyLevel = 5;
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
