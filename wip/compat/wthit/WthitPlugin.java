package com.github.nosrick.crockpot.compat.wthit;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import mcp.mobius.waila.api.*;

public class WthitPlugin implements IWailaPlugin {
    @Override
    public void register(IRegistrar registrar) {
        registrar.addComponent(CrockPotProvider.INSTANCE, TooltipPosition.BODY, CrockPotBlockEntity.class);
        registrar.addBlockData(CrockPotProvider.INSTANCE, CrockPotBlockEntity.class);
        CrockPotMod.LOGGER.info("What the hell is in this crock pot?");
    }
}
