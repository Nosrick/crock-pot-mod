package com.gitlab.nosrick.soilbois.block;

import com.gitlab.nosrick.soilbois.registry.ItemRegistry;
import com.nhoryzon.mc.farmersdelight.registry.BlocksRegistry;
import com.nhoryzon.mc.farmersdelight.util.BlockStateUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.Random;

public class OatCropBlock extends CropBlock implements Fertilizable {
    public static final IntProperty AGE = Properties.AGE_7;
    private static final int MATURITY_AGE = 7;
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
            Block.createCuboidShape(.0d, .0d, .0d, 16.d, 6.d, 16.d),
            Block.createCuboidShape(.0d, .0d, .0d, 16.d, 6.d, 16.d),
            Block.createCuboidShape(.0d, .0d, .0d, 16.d, 10.d, 16.d),
            Block.createCuboidShape(.0d, .0d, .0d, 16.d, 10.d, 16.d),
            Block.createCuboidShape(.0d, .0d, .0d, 16.d, 13.d, 16.d),
            Block.createCuboidShape(.0d, .0d, .0d, 16.d, 13.d, 16.d),
            Block.createCuboidShape(.0d, .0d, .0d, 16.d, 16.d, 16.d),
            Block.createCuboidShape(.0d, .0d, .0d, 16.d, 16.d, 16.d)};

    public OatCropBlock() {
        super(FabricBlockSettings.copyOf(Blocks.WHEAT));
        setDefaultState(getStateManager().getDefaultState().with(AGE, 0));
    }

    @Override
    public IntProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
        return !isMature(state);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        int newAge = getAge(state) + getGrowthAmount(world);
        int maxAge = getMaxAge();
        if (newAge > maxAge) {
            newAge = maxAge;
        }

        world.setBlockState(pos, withAge(newAge), BlockStateUtils.BLOCK_UPDATE);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND) || floor.isOf(BlocksRegistry.RICH_SOIL_FARMLAND.get());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(ItemRegistry.OATS.get());
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return (world.getLightLevel(pos, 0) >= 8 || world.isSkyVisible(pos)) && super.canPlaceAt(state, world, pos);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return state.get(AGE) < MATURITY_AGE;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof RavagerEntity && world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            world.breakBlock(pos, true, entity);
        }

        super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE_BY_AGE[state.get(AGE)];
    }
}
