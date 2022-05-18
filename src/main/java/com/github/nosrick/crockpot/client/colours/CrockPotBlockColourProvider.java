package com.github.nosrick.crockpot.client.colours;

import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public class CrockPotBlockColourProvider implements BlockColorProvider {

    public static int POT_COLOUR = ColorHelper.Argb.getArgb(255, 253, 143, 68);
    public static int ELECTRIC_COLOUR = ColorHelper.Argb.getArgb(255, 222, 100, 100);
    public static int WATER_COLOUR = ColorHelper.Argb.getArgb(255, 91, 110, 225);
    public static int FOOD_COLOUR = ColorHelper.Argb.getArgb(255, 102, 57, 49);

    @Override
    public int getColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex) {

        if (world == null) {
            return 0;
        }

        if(!(world.getBlockEntity(pos) instanceof CrockPotBlockEntity potBlockEntity)){
            return 0;
        }

        if (tintIndex == 0) {
            if (potBlockEntity.isElectric()) {
                return ELECTRIC_COLOUR;
            }

            return POT_COLOUR;
        } else if (tintIndex == 1) {
            if (potBlockEntity.hasFood()) {
                return FOOD_COLOUR;
            } else {
                return WATER_COLOUR;
            }
        }

        return 0;
    }
}
