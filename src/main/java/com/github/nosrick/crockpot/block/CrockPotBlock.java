package com.github.nosrick.crockpot.block;

import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.github.nosrick.crockpot.config.ConfigManager;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.github.nosrick.crockpot.registry.BlockRegistry;
import com.github.nosrick.crockpot.registry.ItemRegistry;
import com.github.nosrick.crockpot.tag.Tags;
import com.github.nosrick.crockpot.util.UUIDUtil;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CrockPotBlock extends BlockWithEntity {

    public static final MapCodec<CrockPotBlock> CODEC = createCodec(CrockPotBlock::new);

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty NEEDS_SUPPORT = BooleanProperty.of("needs_support");
    public static final BooleanProperty HAS_LIQUID = BooleanProperty.of("has_liquid");

    public static final BooleanProperty HAS_FOOD = BooleanProperty.of("has_food");

    public static final BooleanProperty EMITS_SIGNAL = BooleanProperty.of("emits_signal");

    //THIS IS GROSS
    public static final BooleanProperty UPDATE_ME = BooleanProperty.of("update_me");

    public CrockPotBlock(Settings settings) {
        super(settings);

        this.setDefaultState(
                this.getStateManager()
                        .getDefaultState()
                        .with(HAS_LIQUID, false)
                        .with(HAS_FOOD, false)
                        .with(NEEDS_SUPPORT, false)
                        .with(EMITS_SIGNAL, false));

        this.addMeToItemGroup();
    }

    protected void addMeToItemGroup() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(ItemRegistry.CROCK_POT));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CrockPotBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, HAS_LIQUID, HAS_FOOD, NEEDS_SUPPORT, EMITS_SIGNAL, UPDATE_ME);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();

        return getDefaultState()
                .with(FACING, context.getHorizontalPlayerFacing().getOpposite())
                .with(NEEDS_SUPPORT, needsSupport(world.getBlockState(blockPos.down())));
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return state.get(EMITS_SIGNAL);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {

        if (direction == Direction.UP || direction == Direction.DOWN) {
            return 0;
        }

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
                default -> {
                    return 0;
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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.CONSUME;
        }
        ItemStack held = player.getMainHandStack();
        CrockPotBlockEntity potBlockEntity = (CrockPotBlockEntity) world.getBlockEntity(pos);

        if (potBlockEntity == null) {
            return ActionResult.PASS;
        }

        if (held.getItem() == Items.NAME_TAG
                && ConfigManager.canLockPots()) {
            if (potBlockEntity.isOwner(UUIDUtil.NO_PLAYER)) {
                potBlockEntity.setOwner(player.getUuid());
            } else if (potBlockEntity.isOwner(player.getUuid())
                    || player.isCreative()) {
                potBlockEntity.setOwner(UUIDUtil.NO_PLAYER);
            } else {
                return ActionResult.FAIL;
            }

            return ActionResult.SUCCESS;
        }

        if (!potBlockEntity.isOwner(UUIDUtil.NO_PLAYER)
                && !potBlockEntity.isOwner(player.getUuid())
                && (!player.isCreative()
                || (player.isCreative() && !ConfigManager.creativePlayersIgnoreLocks()))) {
            return ActionResult.FAIL;
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

        if (held.getItem() == Blocks.REDSTONE_BLOCK.asItem()
                && this != BlockRegistry.ELECTRIC_CROCK_POT) {
            world.setBlockState(pos, BlockRegistry.ELECTRIC_CROCK_POT.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);

            if (!player.isCreative()) {
                held.decrement(1);
            }

            return ActionResult.SUCCESS;
        }

        float volume = ConfigManager.soundEffectVolume();

        if (!state.get(HAS_LIQUID)) {
            Item heldItem = held.getItem();

            boolean thing = heldItem.getRegistryEntry().isIn(ConventionalItemTags.ENTITY_WATER_BUCKETS);

            if (held.isIn(Tags.CONSUMABLE_WATER_SOURCES_ITEMS)) {
                if (!player.isCreative()) {
                    if (heldItem instanceof BucketItem) {
                        ItemStack emptyContainer = BucketItem.getEmptiedStack(held, player);
                        held.decrement(1);
                        player.giveItemStack(emptyContainer);
                    }
                }
            } else if (ConfigManager.canFillWithWaterBottle()
                    && heldItem instanceof PotionItem
                    && heldItem.getComponents().get(DataComponentTypes.POTION_CONTENTS).potion().get().value() == Potions.WATER.value()) {
                if (!player.isCreative()) {
                    held.decrement(1);
                    player.giveItemStack(new ItemStack(Items.GLASS_BOTTLE));
                }
            } else if (!held.isIn(Tags.INFINITE_WATER_SOURCES_ITEMS)) {
                return ActionResult.FAIL;
            }

            world.setBlockState(pos, state.with(HAS_LIQUID, true), 3);

            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.PLAYERS, volume, 1.0F);

            return ActionResult.SUCCESS;

        } else if (state.get(HAS_LIQUID) && potBlockEntity.canBoil()) {
            if (held.getItem() == Items.BOWL) {
                ItemStack out = potBlockEntity.take(world, held, player);
                if (out != null) {
                    player.giveItemStack(out);

                    if (potBlockEntity.hasFood()) {
                        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, volume, 1.0F);
                    } else {
                        potBlockEntity.flush();
                        world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, volume, 1.0F);
                    }
                }
            } else if (potBlockEntity.canAddFood(held)) {
                boolean result = potBlockEntity.addFood(held, player);
                if (result) {
                    world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, volume, 1.0F);

                    // if the food has a bowl, give it back to the player
                    if (held.getItem().getRecipeRemainder() != ItemStack.EMPTY) {
                        player.giveItemStack(held.getItem().getRecipeRemainder());
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
            WorldView world,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random) {

        if (direction == Direction.DOWN) {
            return state.with(NEEDS_SUPPORT, this.needsSupport(state));
        }

        return state;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient
                || type != BlockEntityTypesRegistry.CROCK_POT) {
            return null;
        }

        return CrockPotBlockEntity::tick;
    }

    protected boolean needsSupport(BlockState state) {
        return state.isIn(Tags.CROCK_POT_REQUIRES_SUPPORT);
    }
}
