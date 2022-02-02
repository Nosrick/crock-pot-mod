package com.gitlab.nosrick.crockpot.client.render.block.model;

import com.gitlab.nosrick.crockpot.CrockPotMod;
import com.gitlab.nosrick.crockpot.block.CrockPotBlock;
import com.gitlab.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class CrockPotBlockEntityRenderer implements BlockEntityRenderer<CrockPotBlockEntity> {

    protected ModelPart lidModel;
    public static EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier(CrockPotMod.MOD_ID, "crock_pot_lid"), "crock_pot_lid");
    public static Identifier POT_LID_TEXTURE_ID = new Identifier(CrockPotMod.MOD_ID, "textures/block/crock_pot_lid.png");

    public CrockPotBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.lidModel = context.getLayerModelPart(MODEL_LAYER);
    }

    @Override
    public void render(CrockPotBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = entity.getWorld();

        if (world == null) {
            return;
        }

        BlockState state = entity.getCachedState();

        if (!state.get(CrockPotBlock.HAS_FOOD)) {
            return;
        }

        matrices.push();
        lidModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(POT_LID_TEXTURE_ID)), light, overlay);

            /*
            MinecraftClient
                    .getInstance()
                    .getBlockRenderManager()
                    .getModelRenderer()
                    .render(
                            world,
                            lidModel,
                            entity.getCachedState(),
                            entity.getPos(),
                            matrices,
                            vertexConsumers.getBuffer(RenderLayer.getSolid()),
                            true,
                            world.getRandom(),
                            0,
                            overlay);
             */
        matrices.pop();
    }

    public static TexturedModelData createModelData() {
        var data = new ModelData();
        data.getRoot().addChild("crock_pot_lid",
                ModelPartBuilder
                        .create()
                        .uv(0, 0)
                        .cuboid(3, 6f, 3f, 10f, 1f, 10f)
                        .cuboid(6f, 7f, 6f, 4f, 1f, 4f),
                ModelTransform.NONE
        );
        return TexturedModelData.of(data, 16, 16);
    }
}
