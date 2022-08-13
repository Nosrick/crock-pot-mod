package com.github.nosrick.crockpot.client.render.block.model;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.github.nosrick.crockpot.client.colours.CrockPotBlockColourProvider;
import com.github.nosrick.crockpot.config.ConfigManager;
import com.github.nosrick.crockpot.util.UUIDUtil;
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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class CrockPotBlockEntityRenderer implements BlockEntityRenderer<CrockPotBlockEntity> {

    protected ModelPart lidModel;

    protected ModelPart padlockModel;

    public static EntityModelLayer POT_MODEL_LAYER = new EntityModelLayer(new Identifier(CrockPotMod.MOD_ID, "crock_pot_lid"), "crock_pot_lid");
    public static EntityModelLayer PADLOCK_MODEL_LAYER = new EntityModelLayer(new Identifier(CrockPotMod.MOD_ID, "padlock"), "padlock");
    public static Identifier POT_LID_TEXTURE_ID = new Identifier(CrockPotMod.MOD_ID, "textures/block/crock_pot_lid.png");
    public static Identifier PADLOCK_TEXTURE_ID = new Identifier(CrockPotMod.MOD_ID, "textures/block/crock_pot_padlock.png");

    protected float xRot;
    protected float zRot;
    protected float yTrans;

    public CrockPotBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.lidModel = context.getLayerModelPart(POT_MODEL_LAYER);
        this.padlockModel = context.getLayerModelPart(PADLOCK_MODEL_LAYER);
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

        if (entity.isOwner(UUIDUtil.NO_PLAYER)) {
            return;
        }

        if (ConfigManager.displayOwnerName()) {
            Entity player = MinecraftClient.getInstance().cameraEntity;
            if (player == null) {
                return;
            }
            Vec3d playerPos = player.getPos();
            BlockPos entityPos = entity.getPos();
            Vec3d playerRot = new Vec3d(playerPos.x - entityPos.getX(), playerPos.y - entityPos.getY(), playerPos.z - entityPos.getZ());
            Vec3f rot = new Vec3f(playerRot.crossProduct(new Vec3d(Vec3f.POSITIVE_Y)));

            Text ownerName = entity.getOwnerName();

            matrices.push();
            this.renderLabel(ownerName, matrices, vertexConsumers, rot, light);
            matrices.pop();
        }

        if(ConfigManager.renderPadlock()) {
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

    protected void renderLabel(
            Text text,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumerProvider,
            Vec3f rotation,
            int light) {
        matrices.push();

        matrices.translate(0.5f, 1f, 0.5f);

        float scale = 0.025f;
        matrices.scale(-scale, -scale, scale);

        float rot = (float) Math.atan2(rotation.getZ(), rotation.getX());
        CrockPotMod.LOGGER.info("" + rot);
        matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(rot));

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int backgroundOpacity = (int) (ConfigManager.labelBackgroundOpacity() * 255.0f) << 24;
        float x = -(textRenderer.getWidth(text) / 2f);
        textRenderer.draw(text, x, 0, ConfigManager.textColor(), false, matrix4f, vertexConsumerProvider, false, backgroundOpacity, light);
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
