package com.github.nosrick.crockpot.client.render.block.model;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.block.CrockPotBlock;
import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.github.nosrick.crockpot.client.colours.CrockPotBlockColourProvider;
import com.github.nosrick.crockpot.config.ConfigManager;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

import java.util.Random;

public class CrockPotBlockEntityRenderer implements BlockEntityRenderer<CrockPotBlockEntity> {

    protected ModelPart lidModel;
    public static EntityModelLayer MODEL_LAYER = new EntityModelLayer(new Identifier(CrockPotMod.MOD_ID, "crock_pot_lid"), "crock_pot_lid");
    public static Identifier POT_LID_TEXTURE_ID = new Identifier(CrockPotMod.MOD_ID, "textures/block/crock_pot_lid.png");

    protected float xRot;
    protected float zRot;
    protected float yTrans;

    public CrockPotBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.lidModel = context.getLayerModelPart(MODEL_LAYER);
    }

    @Override
    public void render(
            CrockPotBlockEntity entity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay) {

        if (!entity.hasFood()) {
            return;
        }

        World world = entity.getWorld();

        if (world == null || MinecraftClient.getInstance().isPaused()) {
            return;
        }

        Random random = entity.getWorld().random;
        float time = world.getTime() + tickDelta;

        float lastX = xRot;
        float lastZ = zRot;

        if (time % 3 < 1f) {
            yTrans = random.nextFloat();
            xRot = random.nextFloat() - 0.5f;
            zRot = random.nextFloat() - 0.5f;
        }

        Vec3f rotation = new Vec3f(lastX * 5, 0, lastZ * 5);
        Vec3f newRotation = new Vec3f(xRot * 5, 0, zRot * 5);
        rotation.lerp(newRotation, tickDelta);
        float boilingIntensity = entity.getBoilingIntensity();
        rotation = new Vec3f(rotation.getX() * boilingIntensity, 0, rotation.getZ() * boilingIntensity);

        matrices.push();
        if (ConfigManager.animateBoilingLid()) {
            matrices.translate(0f, ((yTrans * 0.1d) + 0.02d) * boilingIntensity, 0f);
            matrices.multiply(
                    Quaternion.fromEulerXyzDegrees(rotation));
        }

        int colour = entity.isElectric()
                ? CrockPotBlockColourProvider.ELECTRIC_COLOUR
                : CrockPotBlockColourProvider.POT_COLOUR;

        float r, g, b, a;
        r = ColorHelper.Argb.getRed(colour) / 255f;
        g = ColorHelper.Argb.getGreen(colour) / 255f;
        b = ColorHelper.Argb.getBlue(colour) / 255f;
        a = ColorHelper.Argb.getAlpha(colour) / 255f;

        lidModel.render(
                matrices,
                vertexConsumers.getBuffer(RenderLayer.getEntitySolid(POT_LID_TEXTURE_ID)),
                light,
                overlay,
                r,
                g,
                b,
                a);
        matrices.pop();
    }

    public static TexturedModelData createPotModelData() {
        var data = new ModelData();
        data.getRoot().addChild("crock_pot_lid_bottom",
                ModelPartBuilder
                        .create()
                        .uv(0, 0)
                        .cuboid(3f, 6f, 3f, 10f, 1f, 10f),
                ModelTransform.NONE);
        data.getRoot().addChild("crock_pot_lid_top",
                ModelPartBuilder
                        .create()
                        .uv(0, 0)
                        .cuboid(6f, 7f, 6f, 4f, 1f, 4f),
                ModelTransform.NONE);
        return TexturedModelData.of(data, 16, 16);
    }
}
