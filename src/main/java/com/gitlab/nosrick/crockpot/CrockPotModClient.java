package com.gitlab.nosrick.crockpot;

import com.gitlab.nosrick.crockpot.client.render.block.model.CrockPotBlockEntityRenderer;
import com.gitlab.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public class CrockPotModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(CrockPotBlockEntityRenderer.MODEL_LAYER, CrockPotBlockEntityRenderer::createModelData);
        BlockEntityRendererRegistry.register(BlockEntityTypesRegistry.CROCK_POT.get(), CrockPotBlockEntityRenderer::new);
    }
}
