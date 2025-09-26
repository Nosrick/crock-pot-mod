package com.github.nosrick.crockpot.client.render.block.model;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.block.CrockPotBlock;
import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.github.nosrick.crockpot.client.render.block.entity.state.CrockPotBlockEntityRenderState;
import com.github.nosrick.crockpot.config.ConfigManager;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.github.nosrick.crockpot.registry.BlockRegistry;
import com.github.nosrick.crockpot.util.UUIDUtil;
import me.shedaniel.math.Color;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.class_12075;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.CampfireBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;

public class CrockPotBlockEntityRenderer implements BlockEntityRenderer<CrockPotBlockEntity, CrockPotBlockEntityRenderState> {

    protected ModelPart lidModel;

    protected ModelPart padlockModel;
    protected ModelPart liquidModel;

    protected BlockEntityRendererFactory.Context context;

    public static EntityModelLayer POT_MODEL_LAYER = new EntityModelLayer(CrockPotMod.createIdentifier("crock_pot_lid"), "crock_pot_lid");
    public static EntityModelLayer POT_LIQUID_LAYER = new EntityModelLayer(CrockPotMod.createIdentifier("crock_pot_lid"), "crock_pot_liquid");
    public static EntityModelLayer PADLOCK_MODEL_LAYER = new EntityModelLayer(CrockPotMod.createIdentifier("padlock"), "padlock");
    public static SpriteIdentifier POT_LID_TEXTURE_ID = CrockPotMod.createSpriteIdentifier("textures/block/crock_pot_lid.png");
    public static SpriteIdentifier ELECTRIC_POT_LID_TEXTURE_ID = CrockPotMod.createSpriteIdentifier("textures/block/electric_crock_pot_lid.png");
    public static SpriteIdentifier PADLOCK_TEXTURE_ID = CrockPotMod.createSpriteIdentifier("textures/block/crock_pot_padlock.png");

    public final SpriteIdentifier LIQUID_WATER = CrockPotMod.createSpriteIdentifier("textures/block/crock_pot_liquid.png");
    public final SpriteIdentifier LIQUID_STEW = CrockPotMod.createSpriteIdentifier("textures/block/crock_pot_stew.png");

    public final SpriteHolder materials;

    protected float xRot;
    protected float zRot;
    protected float yTrans;
    protected float boilingIntensity;
    protected Vector3f lidRotation;
    protected Vector3d labelRotation;

    public CrockPotBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.materials = context.spriteHolder();
        this.lidModel = context.getLayerModelPart(POT_MODEL_LAYER);
        this.padlockModel = context.getLayerModelPart(PADLOCK_MODEL_LAYER);
        this.liquidModel = context.getLayerModelPart(POT_LIQUID_LAYER);
        this.context = context;
    }

    protected void renderLabel(CrockPotBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, Text text) {
        matrices.push();
            matrices.translate(0.5f, 1f, 0.5f);

            float scale = 0.025f;
            matrices.scale(-scale, -scale, scale);

            float rot = (float) Math.atan2(this.labelRotation.z, this.labelRotation.x);
            matrices.multiply(new Quaternionf(new AxisAngle4f(rot, 0, 1, 0)));

            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            TextRenderer textRenderer = this.context.textRenderer();
            int backgroundOpacity = ConfigManager.labelBackgroundOpacity() << 24;
            float x = -(textRenderer.getWidth(text) / 2f);
            int color = Color.ofOpaque(ConfigManager.textColor()).getColor();
            queue.submitText(matrices, x, 0, text.asOrderedText(), false, TextRenderer.TextLayerType.SEE_THROUGH, state.lightmapCoordinates, color, 0, 0);
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

    @Override
    public CrockPotBlockEntityRenderState createRenderState() {
        return new CrockPotBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(CrockPotBlockEntity blockEntity, CrockPotBlockEntityRenderState state, float tickProgress, Vec3d cameraPos, @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);

        World world = blockEntity.getWorld();

        if (world == null || MinecraftClient.getInstance().isPaused()) {
            return;
        }

        boolean bl = blockEntity.getWorld() != null;
        BlockState blockState = bl ? blockEntity.getCachedState() : BlockRegistry.CROCK_POT.getDefaultState();

        //Update label rotation
        Entity player = MinecraftClient.getInstance().getCameraEntity();
        if(player != null)
        {
            Vec3d playerPos = player.getPos();
            BlockPos blockPos = blockEntity.getPos();
            Vector3d playerRot = new Vector3d(playerPos.x - blockPos.getX(), playerPos.y - blockPos.getY(), playerPos.z - blockPos.getZ());
            this.labelRotation = new Vector3d(playerRot.cross(new Vector3d(0, 1, 0)));
        }

        if (blockState.get(CrockPotBlock.HAS_FOOD)) {
            var random = blockEntity.getWorld().random;
            float time = world.getTime() + tickProgress;

            float lastX = xRot;
            float lastZ = zRot;

            float lidIntensity = ConfigManager.lidAnimationIntensity();
            this.boilingIntensity = blockEntity.getBoilingIntensity();

            if (time % 3 < 1f) {
                yTrans = random.nextFloat() * lidIntensity;
                xRot = (random.nextFloat() - 0.5f) * lidIntensity;
                zRot = (random.nextFloat() - 0.5f) * lidIntensity;
            }

            Vector3f rotation = new Vector3f(lastX, 0, lastZ);
            Vector3f newRotation = new Vector3f(xRot, 0, zRot);
            rotation.lerp(newRotation, tickProgress);
            float boilingIntensity = blockEntity.getBoilingIntensity();
            this.lidRotation = new Vector3f(rotation.x * boilingIntensity, 0, rotation.z * boilingIntensity);
        }
    }

    @Override
    public void render(CrockPotBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, class_12075 arg) {
        BlockState blockState = state.blockState;

        if (blockState.get(CrockPotBlock.HAS_FOOD)) {
            matrices.push();
            queue.submitModelPart(
                    this.liquidModel,
                    matrices,
                    RenderLayer.getCutout(),
                    state.lightmapCoordinates,
                    OverlayTexture.DEFAULT_UV,
                    this.materials.getSprite(this.LIQUID_STEW));
            matrices.pop();

            float lidIntensity = ConfigManager.lidAnimationIntensity();

            if (ConfigManager.animateBoilingLid()) {
                matrices.translate(0f, ((yTrans * lidIntensity) + 0.02d) * boilingIntensity, 0f);
                matrices.multiply(new Quaternionf().rotateXYZ(this.lidRotation.x, 0, this.lidRotation.z));
            }

            SpriteIdentifier textureID = blockState.getBlock().getDefaultState().isOf(BlockRegistry.ELECTRIC_CROCK_POT)
                    ? ELECTRIC_POT_LID_TEXTURE_ID
                    : POT_LID_TEXTURE_ID;

            matrices.push();
            queue.submitModelPart(this.lidModel, matrices, RenderLayer.getCutout(), state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, this.materials.getSprite(textureID));
            matrices.pop();

        } else if (blockState.get(CrockPotBlock.HAS_LIQUID)) {
            matrices.push();
            queue.submitModelPart(this.liquidModel, matrices, RenderLayer.getCutout(), state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, this.materials.getSprite(this.LIQUID_WATER));
            matrices.pop();
        }

        var world = MinecraftClient.getInstance().world;

        if (world != null)
        {
            CrockPotBlockEntity entity = (CrockPotBlockEntity) world.getBlockEntity(state.pos);

            if (entity == null)
            {
                return;
            }

            if (!entity.isOwner(UUIDUtil.NO_PLAYER)) {
                if (ConfigManager.displayOwnerName()) {
                    Entity player = MinecraftClient.getInstance().getCameraEntity();
                    if (player == null) {
                        return;
                    }
                    Vec3d playerPos = player.getPos();
                    BlockPos entityPos = entity.getPos();
                    Vector3d playerRot = new Vector3d(playerPos.x - entityPos.getX(), playerPos.y - entityPos.getY(), playerPos.z - entityPos.getZ());
                    Vector3d rot = new Vector3d(playerRot.cross(new Vector3d(0, 1, 0)));

                    Text ownerName = entity.getOwnerName();

                    matrices.push();
                    this.renderLabel(state, matrices, queue, ownerName);
                    matrices.pop();
                }

                if (ConfigManager.renderPadlock()) {
                    queue.submitModelPart(this.padlockModel, matrices, RenderLayer.getCutout(), state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, this.materials.getSprite(PADLOCK_TEXTURE_ID));
                }
            }
        }
    }
}
