package com.github.nosrick.crockpot.compat.cloth;

import com.github.nosrick.crockpot.CrockPotMod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

import java.util.function.Consumer;

public class ClothConfigManager {

    private static ConfigHolder<CrockPotConfig> holder;

    public static final Consumer<CrockPotConfig> DEFAULT = (config) -> {
        config.gameplay = new CrockPotConfig.GameplayCategory();
        config.graphics = new CrockPotConfig.GraphicsCategory();
        config.sound = new CrockPotConfig.SoundEffects();
    };

    public static void registerAutoConfig(){
        if(holder == null) {
            holder = AutoConfig.register(CrockPotConfig.class, JanksonConfigSerializer::new);
            if(holder.getConfig().nothing == null || holder.getConfig().nothing.isEmpty())
            {
                DEFAULT.accept(holder.getConfig());
            }
            CrockPotMod.MODS_LOADED.add("cloth");

            holder.save();
        }
    }

    public static CrockPotConfig getConfig() {
        if(holder == null) {
            return new CrockPotConfig();
        }

        return holder.getConfig();
    }

    public static void load() {
        if(holder == null) {
            registerAutoConfig();
        }

        holder.load();
    }

    public static void save() {
        if(holder == null) {
            registerAutoConfig();
        }

        holder.save();
    }
}
