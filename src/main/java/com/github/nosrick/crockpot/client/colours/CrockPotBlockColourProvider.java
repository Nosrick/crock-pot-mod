package com.github.nosrick.crockpot.client.colours;

import com.github.nosrick.crockpot.block.CrockPotBlock;
import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public class CrockPotBlockColourProvider implements BlockColorProvider {

    public static int POT_COLOUR = ColorHelper.Argb.getArgb(255, 223, 113, 38);
    public static int ELECTRIC_COLOUR = ColorHelper.Argb.getArgb(255, 172, 50, 50);
    public static int WATER_COLOUR = ColorHelper.Argb.getArgb(255, 91, 110, 225);
    public static int FOOD_COLOUR = ColorHelper.Argb.getArgb(255, 102, 57, 49);

    @Override
    public int getColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex) {

        if(world == null){
            return 0;
        }

        CrockPotBlockEntity crockPotBlockEntity = (CrockPotBlockEntity) world.getBlockEntity(pos);
        if(crockPotBlockEntity == null) {
            return 0;
        }

        if(tintIndex == 0) {
            if (state.get(CrockPotBlock.ELECTRIC)) {
                return ELECTRIC_COLOUR;
            }

            return POT_COLOUR;
        }
        else if(tintIndex == 1) {
            if(state.get(CrockPotBlock.HAS_FOOD)) {
                return FOOD_COLOUR;
            }
            else
            {
                return WATER_COLOUR;
            }
        }

        return 0;
    }
}
