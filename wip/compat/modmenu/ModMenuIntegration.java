package com.github.nosrick.crockpot.compat.modmenu;

import com.github.nosrick.crockpot.compat.cloth.ClothConfigManager;
import com.github.nosrick.crockpot.compat.cloth.CrockPotConfig;
import com.github.nosrick.crockpot.config.ConfigManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (ConfigManager.clothPresent()) {
            ClothConfigManager.registerAutoConfig();
        }

        return screen -> AutoConfig.getConfigScreen(CrockPotConfig.class, screen).get();
    }
}