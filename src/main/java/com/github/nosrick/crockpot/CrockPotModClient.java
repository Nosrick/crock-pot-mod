package com.github.nosrick.crockpot;

import com.github.nosrick.crockpot.client.render.block.model.CrockPotBlockEntityRenderer;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.github.nosrick.crockpot.registry.BlockRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class CrockPotModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(CrockPotBlockEntityRenderer.POT_MODEL_LAYER, CrockPotBlockEntityRenderer::createPotLidModelData);
        EntityModelLayerRegistry.registerModelLayer(CrockPotBlockEntityRenderer.PADLOCK_MODEL_LAYER, CrockPotBlockEntityRenderer::createPadlockModelData);
        EntityModelLayerRegistry.registerModelLayer(CrockPotBlockEntityRenderer.POT_LIQUID_LAYER, CrockPotBlockEntityRenderer::createLiquidModelData);
        BlockEntityRendererFactories.register(BlockEntityTypesRegistry.CROCK_POT, CrockPotBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(BlockEntityTypesRegistry.ELECTRIC_CROCK_POT, CrockPotBlockEntityRenderer::new);
        BlockRenderLayerMap.putBlock(BlockRegistry.CROCK_POT, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(BlockRegistry.ELECTRIC_CROCK_POT, BlockRenderLayer.CUTOUT);

        /*
        ClientPlayNetworking.registerGlobalReceiver(CrockPotMod.CROCK_POT_CHANNEL, (payload, context) -> {
            context.
            BlockPos pos = buf.readBlockPos();
            NbtCompound nbt = buf.readNbt();

            if(nbt == null){
                return;
            }

            if(client.world.getBlockEntity(pos) instanceof CrockPotBlockEntity crockPotBlockEntity){
                crockPotBlockEntity.readNbt(nbt);
            }
        });
         */
    }
}
