package com.gitlab.nosrick.crockpot.client.render.block.model;

import com.gitlab.nosrick.crockpot.CrockPotMod;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class CrockPotBlockLidModelProvider implements ModelResourceProvider {

    public static final Identifier LID_MODEL_ID = new Identifier(CrockPotMod.MOD_ID, "block/crock_pot_lid");

    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
        if(resourceId == LID_MODEL_ID) {
            return new CrockPotBlockLidModel();
        }

        return null;
    }
}
