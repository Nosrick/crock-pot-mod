package com.github.nosrick.crockpot.block;

import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.github.nosrick.crockpot.config.ConfigManager;
import com.github.nosrick.crockpot.tag.Tags;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

@SuppressWarnings("deprecation")
public class CrockPotBlock extends BlockWithEntity implements InventoryProvider {

    public static final DirectionProperty FACING = DirectionProperty.of("facing");
    public static final BooleanProperty NEEDS_SUPPORT = BooleanProperty.of("needs_support");
    public static final BooleanProperty HAS_FOOD = BooleanProperty.of("has_food");
    public static final BooleanProperty HAS_LIQUID = BooleanProperty.of("has_liquid");

    public CrockPotBlock() {
        super(FabricBlockSettings
                .of(Material.METAL)
                .strength(2.0f)
                .requiresTool()
                .nonOpaque());

        this.setDefaultState(
                this.getStateManager()
                        .getDefaultState()
                        .with(HAS_LIQUID, false)
                        .with(HAS_FOOD, false)
                        .with(NEEDS_SUPPORT, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypesRegistry.CROCK_POT.get().instantiate(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, HAS_LIQUID, NEEDS_SUPPORT, HAS_FOOD);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();

        return getDefaultState()
                .with(FACING, context.getPlayerFacing().getOpposite())
                .with(NEEDS_SUPPORT, needsSupport(world.getBlockState(blockPos.down())));
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {

        if (world.getBlockEntity(pos) instanceof CrockPotBlockEntity potBlockEntity) {
            switch (potBlockEntity.getRedstoneOutputType()) {
                case PORTIONS -> {
                    int portions = potBlockEntity.getPortions();
                    return portions > 0
                            ? Math.round(15 * ((float) portions / (float) ConfigManager.maxPortionsPerPot()))
                            : 0;
                }
                case BONUS_LEVELS -> {
                    int bonusLevels = potBlockEntity.getBonusLevels();
                    return bonusLevels > 0
                            ? Math.round(15 * ((float) bonusLevels / (float) ConfigManager.maxBonusLevels()))
                            : 0;
                }
            }
        }

        return 0;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);

        if (world == null) {
            return;
        }

        if (world.isClient) {
            CrockPotBlockEntity crockPotBlockEntity = (CrockPotBlockEntity) world.getBlockEntity(pos);
            if (crockPotBlockEntity == null) {
                return;
            }

            if (ConfigManager.useBoilParticles()
                    && crockPotBlockEntity.canBoil()
                    && state.get(CrockPotBlock.HAS_LIQUID)
                    && random.nextInt(ConfigManager.boilParticleChance()) == 0) {
                double baseX = pos.getX() + .5d + (random.nextDouble() * .4d - .2d);
                double baseY = pos.getY() + .7d;
                double baseZ = pos.getZ() + .5d + (random.nextDouble() * .4d - .2d);
                world.addParticle(ParticleTypes.EFFECT, baseX, baseY, baseZ, .0d, .0d, .0d);
            }

            if (ConfigManager.useBubbleParticles()
                    && crockPotBlockEntity.canBoil()
                    && state.get(CrockPotBlock.HAS_FOOD)
                    && random.nextInt(ConfigManager.bubbleParticleChance()) == 0) {
                double baseX = pos.getX() + .5d + (random.nextDouble() * .4d - .2d);
                double baseY = pos.getY() + .7d;
                double baseZ = pos.getZ() + .5d + (random.nextDouble() * .4d - .2d);
                world.addParticle(ParticleTypes.BUBBLE_POP, baseX, baseY, baseZ, .0d, .0d, .0d);
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.CONSUME;
        }
        ItemStack held = player.getMainHandStack();
        CrockPotBlockEntity potBlockEntity = (CrockPotBlockEntity) world.getBlockEntity(pos);

        if (potBlockEntity == null) {
            return ActionResult.PASS;
        }

        if (held.isEmpty()) {
            if (player.isSneaking()) {
                potBlockEntity.flush();
                return ActionResult.SUCCESS;
            } else {
                potBlockEntity.setRedstoneOutputType(
                        CrockPotBlockEntity.RedstoneOutputType.getByValue(
                                potBlockEntity.getRedstoneOutputType().value + 1));

                return ActionResult.SUCCESS;
            }
        }

        if (!potBlockEntity.isElectric()
                && held.getItem() == Blocks.REDSTONE_BLOCK.asItem()) {
            potBlockEntity.setElectric(true);

            if (!player.isCreative()) {
                held.decrement(1);
            }

            return ActionResult.SUCCESS;
        }

        float volume = ConfigManager.soundEffectVolume();

        if (!state.get(HAS_LIQUID) && held.getItem() == Items.WATER_BUCKET) {
            world.setBlockState(pos, state.with(HAS_LIQUID, true), 3);

            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.PLAYERS, volume, 1.0F);
            if (!player.isCreative()) {
                held.decrement(1);
                player.giveItemStack(new ItemStack(Items.BUCKET));
            }

            return ActionResult.SUCCESS;
        } else if (state.get(HAS_LIQUID) && potBlockEntity.canBoil()) {
            if (held.getItem() == Items.BOWL) {
                ItemStack out = potBlockEntity.take(world, held, player);
                if (out != null) {
                    player.giveItemStack(out);

                    if (potBlockEntity.getPortions() > 0) {
                        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, volume, 1.0F);
                    } else {
                        world.setBlockState(
                                pos,
                                state
                                        .with(HAS_FOOD, false)
                                        .with(HAS_LIQUID, false));
                        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, volume, 1.0F);
                    }
                }
            } else if (held.isFood()) {
                boolean result = potBlockEntity.addFood(held, player);
                if (result) {
                    world.setBlockState(pos, state.with(HAS_FOOD, true));
                    world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, volume, 1.0F);

                    // if the food has a bowl, give it back to the player
                    if (held.getItem().hasRecipeRemainder()) {
                        player.giveItemStack(new ItemStack(held.getItem().getRecipeRemainder()));
                    }
                }

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public BlockState getStateForNeighborUpdate(
            BlockState state,
            Direction direction,
            BlockState newState,
            WorldAccess world,
            BlockPos pos,
            BlockPos posFrom) {

        if (direction == Direction.DOWN) {
            return state.with(NEEDS_SUPPORT, this.needsSupport(newState));
        }

        return state;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, BlockEntityTypesRegistry.CROCK_POT.get(), CrockPotBlockEntity::tick);
    }

    protected boolean needsSupport(BlockState state) {
        return Tags.CROCK_POT_REQUIRES_SUPPORT.contains(state.getBlock());
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof CrockPotBlockEntity potBlockEntity) {
            return potBlockEntity;
        }

        return null;
    }
}
