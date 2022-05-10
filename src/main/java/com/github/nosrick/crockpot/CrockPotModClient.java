package com.github.nosrick.crockpot;

import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.github.nosrick.crockpot.client.colours.CrockPotBlockColourProvider;
import com.github.nosrick.crockpot.client.render.block.model.CrockPotBlockEntityRenderer;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.github.nosrick.crockpot.registry.BlockRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class CrockPotModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(CrockPotBlockEntityRenderer.MODEL_LAYER, CrockPotBlockEntityRenderer::createPotModelData);
        BlockEntityRendererRegistry.register(BlockEntityTypesRegistry.CROCK_POT.get(), CrockPotBlockEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.CROCK_POT.get(), RenderLayer.getCutout());

        ColorProviderRegistry.BLOCK.register(new CrockPotBlockColourProvider(), BlockRegistry.CROCK_POT.get());

        ClientPlayNetworking.registerGlobalReceiver(CrockPotMod.CROCK_POT_CHANNEL, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            CrockPotBlockEntity.RedstoneOutputType outputType = buf.readEnumConstant(CrockPotBlockEntity.RedstoneOutputType.class);
            BlockEntity blockEntity = client.world.getBlockEntity(pos);

            if(blockEntity instanceof CrockPotBlockEntity crockPotBlockEntity){
                crockPotBlockEntity.setRedstoneOutputType(outputType);
            }
        });
    }
}
