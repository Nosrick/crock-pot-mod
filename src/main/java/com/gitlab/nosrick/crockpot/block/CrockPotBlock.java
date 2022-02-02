package com.gitlab.nosrick.crockpot.block;

import com.gitlab.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.gitlab.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.gitlab.nosrick.crockpot.tag.Tags;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class CrockPotBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = DirectionProperty.of("facing");
    public static final BooleanProperty HAS_FIRE = BooleanProperty.of("has_fire");
    public static final BooleanProperty HAS_FOOD = BooleanProperty.of("has_food");
    public static final BooleanProperty HAS_LIQUID = BooleanProperty.of("has_liquid");
    public static final IntProperty BONUS_LEVELS = IntProperty.of("bonus_levels", 0, 5);

    public static final int MAX_BONUS_STAGES = 5;

    public CrockPotBlock() {
        super(FabricBlockSettings
                .of(Material.METAL)
                .strength(2.0f)
                .requiresTool()
                .nonOpaque());

        this.setDefaultState(
                this.getStateManager()
                        .getDefaultState()
                        .with(HAS_LIQUID, false)
                        .with(HAS_FIRE, false)
                        .with(HAS_FOOD, false)
                        .with(BONUS_LEVELS, 0));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypesRegistry.CROCK_POT.get().instantiate(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(BONUS_LEVELS, FACING, HAS_LIQUID, HAS_FIRE, HAS_FOOD);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();

        return getDefaultState()
                .with(FACING, context.getPlayerFacing().getOpposite())
                .with(HAS_FIRE, hasTrayHeatSource(world.getBlockState(blockPos.down())));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);

        if (world == null) {
            return;
        }


        if (world.isClient
                && state.get(CrockPotBlock.HAS_FIRE)) {
            if (state.get(CrockPotBlock.HAS_LIQUID)
                    && random.nextFloat() < .05f) {
                double baseX = pos.getX() + .5d + (random.nextDouble() * .4d - .2d);
                double baseY = pos.getY() + .7d;
                double baseZ = pos.getZ() + .5d + (random.nextDouble() * .4d - .2d);
                world.addParticle(ParticleTypes.EFFECT, baseX, baseY, baseZ, .0d, .0d, .0d);
            }

            if (state.get(CrockPotBlock.HAS_FOOD)
                    && random.nextFloat() < .05f) {
                double baseX = pos.getX() + .5d + (random.nextDouble() * .4d - .2d);
                double baseY = pos.getY() + .7d;
                double baseZ = pos.getZ() + .5d + (random.nextDouble() * .4d - .2d);
                world.addParticle(ParticleTypes.BUBBLE_POP, baseX, baseY, baseZ, .0d, .0d, .0d);
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        ItemStack held = player.getMainHandStack();

        if (!state.get(HAS_LIQUID) && held.getItem() == Items.WATER_BUCKET) {
            if (!world.isClient()) {
                world.setBlockState(pos, state.with(HAS_LIQUID, true), 3);

                world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.PLAYERS, 0.8F, 1.0F);
                held.decrement(1);
                player.giveItemStack(new ItemStack(Items.BUCKET));
            }

            return ActionResult.SUCCESS;
        } else if (state.get(HAS_LIQUID) && state.get(HAS_FIRE)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CrockPotBlockEntity pot) {
                if (!world.isClient()) {
                    if (held.getItem() == Items.BOWL) {
                        ItemStack out = pot.take(world, pos, state, held);
                        if (out != null) {
                            player.giveItemStack(out);

                            if (pot.getPortions() > 0) {
                                world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            } else {
                                world.setBlockState(
                                        pos,
                                        state
                                                .with(HAS_FOOD, false)
                                                .with(HAS_LIQUID, false)
                                                .with(BONUS_LEVELS, 0));
                                world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            }
                        }
                    } else if (held.isFood()) {
                        boolean result = pot.addFood(world, pos, state, held);
                        if (result) {
                            world.setBlockState(pos, state.with(HAS_FOOD, true).with(BONUS_LEVELS, 0));
                            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.5F, 1.0F);

                            // if the food has a bowl, give it back to the player
                            if (held.getItem().hasRecipeRemainder()) {
                                player.giveItemStack(new ItemStack(held.getItem().getRecipeRemainder()));
                            }
                        }
                    }
                }

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;

    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, BlockEntityTypesRegistry.CROCK_POT.get(), CrockPotBlockEntity::tick);
    }

    protected boolean hasTrayHeatSource(BlockState state) {
        return Tags.HEAT_SOURCES.contains(state.getBlock());
    }

    public static float getBoilingIntensity(World world, BlockState state) {
        if(!state.getProperties().contains(BONUS_LEVELS)) {
            return 0f;
        }

        return state.get(BONUS_LEVELS) / ((float) MAX_BONUS_STAGES);
    }
}
