package com.gitlab.nosrick.soilbois.block;

import com.nhoryzon.mc.farmersdelight.util.BlockStateUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

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


}
