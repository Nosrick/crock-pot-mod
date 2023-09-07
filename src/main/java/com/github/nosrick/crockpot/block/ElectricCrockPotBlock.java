package com.github.nosrick.crockpot.block;

import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.github.nosrick.crockpot.blockentity.ElectricCrockPotBlockEntity;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
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

    @Override
    protected void addMeToItemGroup() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(ItemRegistry.ELECTRIC_CROCK_POT.get()));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypesRegistry.ELECTRIC_CROCK_POT.get().instantiate(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient
                || type != BlockEntityTypesRegistry.ELECTRIC_CROCK_POT.get()) {
            return null;
        }

        return ElectricCrockPotBlockEntity::tick;
    }
}
