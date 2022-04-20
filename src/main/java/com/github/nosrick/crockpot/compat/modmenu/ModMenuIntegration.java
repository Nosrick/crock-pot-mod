package com.github.nosrick.crockpot.compat.modmenu;

import com.github.nosrick.crockpot.compat.cloth.ClothConfigManager;
import com.github.nosrick.crockpot.compat.cloth.CrockPotConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        ClothConfigManager.registerAutoConfig();

        return screen -> AutoConfig.getConfigScreen(CrockPotConfig.class, screen).get();
    }
}