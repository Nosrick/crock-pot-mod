package com.github.nosrick.crockpot.registry;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.block.CrockPotBlock;
import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.github.nosrick.crockpot.blockentity.ElectricCrockPotBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.Arrays;
import java.util.function.Supplier;

public class BlockEntityTypesRegistry {

    public static final BlockEntityType<CrockPotBlockEntity> CROCK_POT = register(
            "crock_pot",
            FabricBlockEntityTypeBuilder.create(CrockPotBlockEntity::new, BlockRegistry.CROCK_POT).build());

    public static final BlockEntityType<ElectricCrockPotBlockEntity> ELECTRIC_CROCK_POT = register(
            "electric_crock_pot",
            FabricBlockEntityTypeBuilder.create(ElectricCrockPotBlockEntity::new, BlockRegistry.ELECTRIC_CROCK_POT).build());

    public static void initialize() {}

    public static <T extends BlockEntityType<?>> T register(String path,T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, CrockPotMod.createIdentifier(path), blockEntityType);
    }
}
