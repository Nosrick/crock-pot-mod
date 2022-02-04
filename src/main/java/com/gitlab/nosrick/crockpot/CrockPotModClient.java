package com.gitlab.nosrick.crockpot;

import com.gitlab.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.gitlab.nosrick.crockpot.client.render.block.model.CrockPotBlockEntityRenderer;
import com.gitlab.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class CrockPotModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(CrockPotBlockEntityRenderer.MODEL_LAYER, CrockPotBlockEntityRenderer::createModelData);
        BlockEntityRendererRegistry.register(BlockEntityTypesRegistry.CROCK_POT.get(), CrockPotBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(
                CrockPotBlockEntity.PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    BlockPos pos = buf.readBlockPos();

                    ClientWorld world = handler.getWorld();

                    if(world == null){
                        return;
                    }

                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (blockEntity instanceof CrockPotBlockEntity pot) {
                        NbtCompound nbt = buf.readUnlimitedNbt();
                        pot.readNbt(nbt);
                    }
                });
    }
}
