package com.github.nosrick.crockpot.registry;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public enum BlockEntityTypesRegistry {

    CROCK_POT("crock_pot", CrockPotBlockEntity.class, CrockPotBlockEntity::new, BlockRegistry.CROCK_POT);

    private final String pathName;
    private final Class<? extends BlockEntity> blockEntityClass;
    private final Supplier<BlockEntityType<? extends BlockEntity>> blockEntityTypeSupplier;
    private BlockEntityType<? extends BlockEntity> blockEntityType;

    BlockEntityTypesRegistry(
            String pathName,
            Class<? extends BlockEntity> blockEntityClass,
            FabricBlockEntityTypeBuilder.Factory<? extends BlockEntity> blockEntitySupplier,
            BlockRegistry... blockRegistry) {
        this.pathName = pathName;
        this.blockEntityClass = blockEntityClass;
        this.blockEntityTypeSupplier = () -> FabricBlockEntityTypeBuilder.create(
                        blockEntitySupplier,
                        Arrays.stream(blockRegistry)
                                .map(BlockRegistry::get)
                                .toArray(Block[]::new))
                .build();
    }

    public static void registerAll() {
        for (BlockEntityTypesRegistry value : values()){
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(CrockPotMod.MOD_ID, value.pathName),
                    value.get());
        }
    }

    public <T extends BlockEntity> BlockEntityType<T> get() {
        return (BlockEntityType<T>) get(blockEntityClass);
    }

    private <T extends BlockEntity> BlockEntityType<T> get(Class<T> clazz) {
        if(blockEntityType == null) {
            blockEntityType = blockEntityTypeSupplier.get();
        }

        return (BlockEntityType<T>) blockEntityType;
    }
}
