package com.github.nosrick.crockpot.block;

import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.github.nosrick.crockpot.blockentity.ElectricCrockPotBlockEntity;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.github.nosrick.crockpot.registry.BlockRegistry;
import com.github.nosrick.crockpot.registry.ItemRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ElectricCrockPotBlock extends CrockPotBlock {

    public ElectricCrockPotBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void addMeToItemGroup() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(BlockRegistry.ELECTRIC_CROCK_POT.asItem()));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricCrockPotBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient
                || type != BlockEntityTypesRegistry.ELECTRIC_CROCK_POT) {
            return null;
        }

        return ElectricCrockPotBlockEntity::tick;
    }
}
