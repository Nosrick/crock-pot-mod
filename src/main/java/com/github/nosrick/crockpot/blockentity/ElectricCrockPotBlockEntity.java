package com.github.nosrick.crockpot.blockentity;

import com.github.nosrick.crockpot.block.CrockPotBlock;
import com.github.nosrick.crockpot.config.ConfigManager;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class ElectricCrockPotBlockEntity extends CrockPotBlockEntity{

    public ElectricCrockPotBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntityTypesRegistry.ELECTRIC_CROCK_POT.get(), pos, state);
    }

    protected ElectricCrockPotBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.potionEffects = new ArrayList<>();
    }

    @Override
    public boolean canBoil() {

        boolean hasLiquid = this.getCachedState().get(CrockPotBlock.HAS_LIQUID);

        if(ConfigManager.redstoneNeedsPower()) {
            if(this.hasWorld()
                && this.world.getReceivedRedstonePower(this.pos) > ConfigManager.redstonePowerThreshold()) {
                return hasLiquid;
            }
            return false;
        }

        return hasLiquid;
    }
}
