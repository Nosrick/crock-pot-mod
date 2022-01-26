package com.gitlab.nosrick.soilbois.registry;

import com.gitlab.nosrick.soilbois.SoilBoisMod;
import com.gitlab.nosrick.soilbois.blockentity.TaginePotBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.function.Supplier;

public enum BlockEntityTypesRegistry {

    TAGINE_POT("tagine_pot", TaginePotBlockEntity.class, TaginePotBlockEntity::new, BlockRegistry.TAGINE_POT);

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
                    Registry.BLOCK_ENTITY_TYPE,
                    new Identifier(SoilBoisMod.MOD_ID, value.pathName),
                    value.get());
        }
    }

    @SuppressWarnings("unchecked")
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
