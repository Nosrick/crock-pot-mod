package com.github.nosrick.crockpot.client.render.block.model;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.block.CrockPotBlock;
import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.github.nosrick.crockpot.config.ConfigManager;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.github.nosrick.crockpot.util.UUIDUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.joml.*;

import java.lang.Math;

public class CrockPotBlockEntityRenderer implements BlockEntityRenderer<CrockPotBlockEntity> {

    protected ModelPart lidModel;

    protected ModelPart padlockModel;
    protected ModelPart liquidModel;

    public static EntityModelLayer POT_MODEL_LAYER = new EntityModelLayer(CrockPotMod.createIdentifier("crock_pot_lid"), "crock_pot_lid");
    public static EntityModelLayer POT_LIQUID_LAYER = new EntityModelLayer(CrockPotMod.createIdentifier("crock_pot_lid"), "crock_pot_liquid");
    public static EntityModelLayer PADLOCK_MODEL_LAYER = new EntityModelLayer(CrockPotMod.createIdentifier("padlock"), "padlock");
    public static Identifier POT_LID_TEXTURE_ID = CrockPotMod.createIdentifier("textures/block/crock_pot_lid.png");
    public static Identifier ELECTRIC_POT_LID_TEXTURE_ID = CrockPotMod.createIdentifier("textures/block/electric_crock_pot_lid.png");
    public static Identifier PADLOCK_TEXTURE_ID = CrockPotMod.createIdentifier("textures/block/crock_pot_padlock.png");

    public static Identifier LIQUID_WATER = CrockPotMod.createIdentifier("textures/block/crock_pot_liquid.png");
    public static Identifier LIQUID_STEW = CrockPotMod.createIdentifier("textures/block/crock_pot_stew.png");

    protected float xRot;
    protected float zRot;
    protected float yTrans;

    public CrockPotBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.lidModel = context.getLayerModelPart(POT_MODEL_LAYER);
        this.padlockModel = context.getLayerModelPart(PADLOCK_MODEL_LAYER);
        this.liquidModel = context.getLayerModelPart(POT_LIQUID_LAYER);
    }

    @Override
    public void render(
            CrockPotBlockEntity entity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay) {

        World world = entity.getWorld();

        if (world == null || MinecraftClient.getInstance().isPaused()) {
            return;
        }

        BlockState blockState = entity.getCachedState();

        if(blockState.get(CrockPotBlock.HAS_FOOD)) {
            matrices.push();
                this.liquidModel.render(
                    matrices,
                    vertexConsumers.getBuffer(RenderLayer.getEntitySolid(LIQUID_STEW)),
                    light,
                    overlay);
            matrices.pop();

            var random = entity.getWorld().random;
            float time = world.getTime() + tickDelta;

            float lastX = xRot;
            float lastZ = zRot;

            float lidIntensity = ConfigManager.lidAnimationIntensity();

            if (time % 3 < 1f) {
                yTrans = random.nextFloat() * lidIntensity;
                xRot = (random.nextFloat() - 0.5f) * lidIntensity;
                zRot = (random.nextFloat() - 0.5f) * lidIntensity;
            }

        Vector3f rotation = new Vector3f(lastX, 0, lastZ);
        Vector3f newRotation = new Vector3f(xRot, 0, zRot);
            rotation.lerp(newRotation, tickDelta);
            float boilingIntensity = entity.getBoilingIntensity();
            rotation = new Vector3f(rotation.x * boilingIntensity, 0, rotation.z * boilingIntensity);

            matrices.push();
            if (ConfigManager.animateBoilingLid()) {
                matrices.translate(0f, ((yTrans * lidIntensity) + 0.02d) * boilingIntensity, 0f);
                matrices.multiply(new Quaternionf().rotateXYZ(rotation.x, 0, rotation.z));
            }

            Identifier textureID = entity.getType() == BlockEntityTypesRegistry.ELECTRIC_CROCK_POT.get()
                    ? ELECTRIC_POT_LID_TEXTURE_ID
                    : POT_LID_TEXTURE_ID;

            lidModel.render(
                    matrices,
                    vertexConsumers.getBuffer(RenderLayer.getEntitySolid(textureID)),
                    light,
                    overlay);
            matrices.pop();
        }
        else if(blockState.get(CrockPotBlock.HAS_LIQUID)) {
            matrices.push();
                this.liquidModel.render(
                    matrices,
                    vertexConsumers.getBuffer(RenderLayer.getEntitySolid(LIQUID_WATER)),
                    light,
                    overlay);
            matrices.pop();
        }

        if (!entity.isOwner(UUIDUtil.NO_PLAYER)) {
            if (ConfigManager.displayOwnerName()) {
                Entity player = MinecraftClient.getInstance().cameraEntity;
                if (player == null) {
                    return;
                }
                Vec3d playerPos = player.getPos();
                BlockPos entityPos = entity.getPos();
                Vector3d playerRot = new Vector3d(playerPos.x - entityPos.getX(), playerPos.y - entityPos.getY(), playerPos.z - entityPos.getZ());
                Vector3d rot = new Vector3d(playerRot.cross(new Vector3d(0, 1, 0)));

                Text ownerName = entity.getOwnerName();

                matrices.push();
                this.renderLabel(ownerName, matrices, vertexConsumers, rot, light);
                matrices.pop();
            }

            if (ConfigManager.renderPadlock()) {
                padlockModel.render(
                        matrices,
                        vertexConsumers.getBuffer(RenderLayer.getEntitySolid(PADLOCK_TEXTURE_ID)),
                        light,
                        overlay,
                        1f,
                        1f,
                        1f,
                        1f);
            }
        }
    }

    protected void renderLabel(
            Text text,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumerProvider,
            Vector3d rotation,
            int light) {
        matrices.push();

        matrices.translate(0.5f, 1f, 0.5f);

        float scale = 0.025f;
        matrices.scale(-scale, -scale, scale);

        float rot = (float) Math.atan2(rotation.z, rotation.x);
        matrices.multiply(new Quaternionf(new AxisAngle4f(rot, 0, 1, 0)));

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int backgroundOpacity = ConfigManager.labelBackgroundOpacity() << 24;
        float x = -(textRenderer.getWidth(text) / 2f);
        textRenderer.draw(text, x, 0, ConfigManager.textColor(), false, matrix4f, vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL, backgroundOpacity, light);
        matrices.pop();
    }

    public static TexturedModelData createLiquidModelData() {
        var data = new ModelData();

        data.getRoot().addChild("pot_liquid",
                ModelPartBuilder
                        .create()
                        .uv(0, 0)
                        .cuboid(3, 4, 3,
                                10, 1, 10),
                ModelTransform.NONE);

        return TexturedModelData.of(data, 16, 16);
    }

    public static TexturedModelData createPotLidModelData() {
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

    public static TexturedModelData createPadlockModelData() {
        var data = new ModelData();
        data.getRoot().addChild("padlock_body",
                ModelPartBuilder
                        .create()
                        .uv(0, 0)
                        .cuboid(3f, 3f, 1f, 3f, 2f, 1f),
                ModelTransform.NONE);

        data.getRoot().addChild("padlock_shank_left",
                ModelPartBuilder
                        .create()
                        .uv(0, 5)
                        .cuboid(3f, 5f, 1f, 1f, 1f, 1f),
                ModelTransform.NONE);

        data.getRoot().addChild("padlock_shank_right",
                ModelPartBuilder
                        .create()
                        .uv(0, 9)
                        .cuboid(5f, 5f, 1f, 1f, 1f, 1f),
                ModelTransform.NONE);

        data.getRoot().addChild("padlock_shank_top",
                ModelPartBuilder
                        .create()
                        .uv(0, 13)
                        .cuboid(3f, 6f, 1f, 3f, 1f, 1f),
                ModelTransform.NONE);

        return TexturedModelData.of(data, 16, 16);
    }
}
