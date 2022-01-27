package com.gitlab.nosrick.crockpot.item;

import com.gitlab.nosrick.crockpot.CrockPotMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

public class ModItemSettings extends FabricItemSettings {
    public ModItemSettings() {
        super();
        group(CrockPotMod.ITEM_GROUP);
    }
}