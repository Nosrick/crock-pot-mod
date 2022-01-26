package com.gitlab.nosrick.soilbois.block;

import com.gitlab.nosrick.soilbois.registry.BlockEntityTypesRegistry;
import com.gitlab.nosrick.soilbois.blockentity.TaginePotBlockEntity;
import com.nhoryzon.mc.farmersdelight.tag.Tags;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TaginePotBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = DirectionProperty.of("facing");
    public static final BooleanProperty SUPPORTED = BooleanProperty.of("supported");

    public static final IntProperty LIQUID_LEVEL = IntProperty.of("liquid", 0, 2);
    public static final BooleanProperty HAS_FIRE = BooleanProperty.of("has_fire");

    public TaginePotBlock(){
        super(FabricBlockSettings.copyOf(Blocks.CAULDRON));

        this.setDefaultState(
                this.getStateManager()
                        .getDefaultState()
                        .with(LIQUID_LEVEL, 0)
                        .with(HAS_FIRE, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypesRegistry.TAGINE_POT.get().instantiate(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LIQUID_LEVEL, HAS_FIRE, FACING, SUPPORTED);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();

        return getDefaultState()
                .with(FACING, context.getPlayerFacing().getOpposite())
                .with(SUPPORTED, needsTrayForHeatSource(world.getBlockState(blockPos.down())));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        ItemStack held = player.getMainHandStack();

        if (state.get(LIQUID_LEVEL) == 0 && held.getItem() == Items.WATER_BUCKET) {
            if (!world.isClient()) {
                world.setBlockState(pos, state.with(LIQUID_LEVEL, 1), 3);

                world.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.PLAYERS, 0.8F, 1.0F);
            }

            return ActionResult.SUCCESS;
        }
        else if (state.get(LIQUID_LEVEL) > 0 && state.get(HAS_FIRE)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TaginePotBlockEntity pot) {
                if (!world.isClient()) {
                    if (held.getItem() == Items.BOWL) {
                        ItemStack out = pot.take(world, pos, state, held);
                        if (out != null) {
                            player.giveItemStack(out);

                            if (pot.getPortions() > 0) {
                                world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            } else {
                                world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5F, 1.0F);
                            }
                        }

                    } else if (held.isFood()) {
                        boolean result = pot.addFood(world, pos, state, held);
                        if (result) {
                            world.playSound(null, pos, SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, SoundCategory.BLOCKS, 0.5F, 1.0F);

                            // if the food has a bowl, give it back to the player
                            if (held.getItem().hasRecipeRemainder()){
                                player.giveItemStack(new ItemStack(held.getItem().getRecipeRemainder()));
                            }
                        }
                    }
                }

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;

    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, BlockEntityTypesRegistry.TAGINE_POT.get(), TaginePotBlockEntity::tick);
    }

    protected boolean needsTrayForHeatSource(BlockState state) {
        return Tags.TRAY_HEAT_SOURCES.contains(state.getBlock());
    }
}
