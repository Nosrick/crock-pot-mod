package com.gitlab.nosrick.crockpot;

import com.gitlab.nosrick.crockpot.client.render.block.model.CrockPotBlockLidModelProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;

public class CrockPotModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new CrockPotBlockLidModelProvider());
    }
}
