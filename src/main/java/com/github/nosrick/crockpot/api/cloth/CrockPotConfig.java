package com.github.nosrick.crockpot.api.cloth;

import com.github.nosrick.crockpot.CrockPotMod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.util.ActionResult;

@Config(name = CrockPotMod.MOD_ID)
public class CrockPotConfig implements ConfigData {

    public CrockPotConfig() {
        CrockPotMod.MODS_LOADED.add("cloth");
    }

    @ConfigEntry.Gui.Excluded
    private static boolean REGISTERED = false;

    public int maxBonusLevels = 5;

    @Comment("How much is too much?")
    public int cowlCurseLevels = 5;

    @Comment("Is reboiling stew really all that bad?")
    public boolean cursedStew = true;

    @Comment("Remember; 20 ticks per Minecraft second!")
    public int boilTicksPerLevel = 20 * 60 * 2;
    public int maxPortions = 64;

    public boolean itemPositiveEffects = true;
    public int itemMinPositiveBonusLevel = 5;
    public boolean itemNegativeEffects = true;
    public int itemMinNegativeCurseLevel = 1;

    public int maxStewNameLength = 32;

    @Comment("Number of seconds added to the nausea effect per curse level")
    public int baseNauseaDuration = 5;

    @Comment("It's probably a good idea to keep this enabled")
    public boolean cappedNauseaDuration = true;

    public int maxNauseaDuration = baseNauseaDuration * 20 * cowlCurseLevels;

    public int minSatisfyingLevel = 1;
    public int minFillingLevel = 3;
    public int minHeartyLevel = 5;

    public static boolean isRegistered() {
        return REGISTERED;
    }

    public static synchronized CrockPotConfig getConfig() {
        if (!REGISTERED) {
            AutoConfig.register(CrockPotConfig.class, GsonConfigSerializer::new);
            REGISTERED = true;
        }

        return AutoConfig.getConfigHolder(CrockPotConfig.class).getConfig();
    }
}
