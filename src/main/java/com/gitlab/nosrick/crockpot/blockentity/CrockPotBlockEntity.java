package com.gitlab.nosrick.crockpot.blockentity;

import com.gitlab.nosrick.crockpot.block.CrockPotBlock;
import com.gitlab.nosrick.crockpot.item.StewItem;
import com.gitlab.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.gitlab.nosrick.crockpot.registry.CrockPotSoundRegistry;
import com.gitlab.nosrick.crockpot.registry.ItemRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CrockPotBlockEntity extends BlockEntity {

    protected static final String PORTIONS_NBT = "Portions";
    protected static final String HUNGER_NBT = "Hunger";
    protected static final String SATURATION_NBT = "Saturation";
    protected static final String CONTENTS_NBT = "Contents";
    protected static final String NAME_NBT = "Name";
    protected static final String BOILING_TIME = "Boiling Time";
    protected static final String LAST_TIME = "Last Time";

    public static final int MAX_PORTIONS = 64;
    public static final int MAX_BOILING_TIME = 20 * 2;

    protected String name = "";
    protected int portions = 0;
    protected int hunger = 0;
    protected float saturation = 0.0F;
    protected List<String> contents = new ArrayList<>();

    protected long boilingTime = 0;
    protected long lastTime = 0;

    public CrockPotBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesRegistry.CROCK_POT.get(), pos, state);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        this.name = tag.getString(NAME_NBT);
        this.portions = tag.getInt(PORTIONS_NBT);
        this.hunger = tag.getInt(HUNGER_NBT);
        this.saturation = tag.getFloat(SATURATION_NBT);

        this.boilingTime = tag.getLong(BOILING_TIME);
        this.lastTime = tag.getLong(LAST_TIME);

        this.contents = new ArrayList<>();
        NbtList list = tag.getList(CONTENTS_NBT, 8);
        list.stream()
                .map(NbtElement::asString)
                .forEach(item -> this.contents.add(item));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putString(NAME_NBT, this.name);
        nbt.putInt(PORTIONS_NBT, this.portions);
        nbt.putInt(HUNGER_NBT, this.hunger);
        nbt.putFloat(SATURATION_NBT, this.saturation);

        nbt.putLong(BOILING_TIME, this.boilingTime);
        nbt.putLong(LAST_TIME, this.lastTime);

        NbtList list = new NbtList();
        this.contents.forEach(
                item -> list.add(NbtString.of(item)));
        nbt.put(CONTENTS_NBT, list);
    }

    public boolean addFood(World world, BlockPos pos, BlockState state, ItemStack food) {
        if (!food.isFood()) {
            return false;
        }

        Item foodItem = food.getItem();

        FoodComponent foodComponent = foodItem.getFoodComponent();
        if (foodComponent == null) {
            return false;
        }

        if (this.portions < MAX_PORTIONS) {
            if (this.portions == 0) {
                this.contents = new ArrayList<>();
            }

            if (!contents.contains(foodItem.getTranslationKey())) {
                this.portions++;
                this.boilingTime = 0;
                int foodHunger = foodComponent.getHunger();
                float foodSaturation = foodComponent.getSaturationModifier();

                this.hunger = Math.round((100f * (((this.portions - 1) * this.hunger) + foodHunger) / this.portions) / 100);
                this.saturation = (float) Math.round((100 * (((this.portions - 1) * this.saturation) + foodSaturation) / this.portions) / 100);
                contents.add(foodItem.getTranslationKey());

                this.markDirty();
                food.decrement(1);

                return true;
            }
        }

        return false;
    }

    @Nullable
    public ItemStack take(World world, BlockPos pos, BlockState state, ItemStack container) {

        if (world.isClient) {
            return null;
        }

        if (container.getItem() != Items.BOWL) {
            return null;
        }

        if (this.portions > 0) {
            // create a stew from the pot's contents
            ItemStack stew = new ItemStack(ItemRegistry.STEW_ITEM.get());
            StewItem.setHunger(stew, this.hunger);
            StewItem.setSaturation(stew, this.saturation);
            StewItem.setContents(stew, this.contents);
            container.decrement(1);

            this.decrementPortions();

            // if no more portions in the pot, flush out the pot data
            if (--this.portions <= 0) {
                this.flush(world, pos, state);
            }

            this.markDirty();

            return stew;
        }

        return null;
    }

    public static FoodComponent getFoodComponent(World world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof CrockPotBlockEntity taginePotBlockEntity) {
            return new FoodComponent.Builder()
                    .hunger(taginePotBlockEntity.hunger)
                    .saturationModifier(taginePotBlockEntity.saturation)
                    .build();
        }

        return null;
    }

    private void flush(World world, BlockPos pos, BlockState state) {
        this.contents = new ArrayList<>();
        this.hunger = 0;
        this.saturation = 0;
        this.portions = 0;
        this.boilingTime = 0;
        world.setBlockState(
                pos,
                state
                        .with(CrockPotBlock.HAS_LIQUID, false)
                        .with(CrockPotBlock.HAS_FOOD, false)
                        .with(CrockPotBlock.BONUS_LEVELS, 0));

        this.markDirty();
    }

    public boolean isAboveLitHeatSource() {
        if (world == null) {
            return false;
        }

        BlockState checkState = world.getBlockState(pos);
        return checkState.get(CrockPotBlock.HAS_FIRE);
    }

    public int getPortions() {
        return this.portions;
    }

    public void decrementPortions() {
        this.portions -= 1;
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        if (world == null) {
            return;
        }

        if (!world.isClient
                && blockEntity instanceof CrockPotBlockEntity crockPotBlockEntity) {
            serverTick(world, crockPotBlockEntity);
        }

        Random random = world.random;

        if (blockState.get(CrockPotBlock.HAS_LIQUID)) {
            if (random.nextFloat() < 0.01f) {
                float variation = random.nextFloat() / 5f - 0.1f;
                world.playSound(null, blockPos, CrockPotSoundRegistry.CROCK_POT_BOIL.get(), SoundCategory.BLOCKS, 0.5f, 1.0f + variation);
            }
        }

        if (blockState.get(CrockPotBlock.HAS_FOOD)) {
            if (random.nextFloat() < 0.01f) {
                float variation = random.nextFloat() / 5f - 0.1f;
                world.playSound(null, blockPos, CrockPotSoundRegistry.CROCK_POT_BUBBLE.get(), SoundCategory.BLOCKS, 0.5f, 1.0f + variation);
            }
        }
    }

    protected static void serverTick(World world, CrockPotBlockEntity blockEntity) {

        BlockState blockState = world.getBlockState(blockEntity.pos);

        if (blockState.get(CrockPotBlock.HAS_FIRE)
                && blockState.get(CrockPotBlock.HAS_FOOD)) {
            long time = world.getTime();
            blockEntity.boilingTime += time - blockEntity.lastTime;
            blockEntity.lastTime = time;
            int bonusLevels = blockState.get(CrockPotBlock.BONUS_LEVELS);

            if (blockEntity.boilingTime > MAX_BOILING_TIME
                    && bonusLevels < CrockPotBlock.MAX_BONUS_STAGES) {
                world.setBlockState(blockEntity.pos, blockState.with(CrockPotBlock.BONUS_LEVELS, bonusLevels + 1));
                blockEntity.boilingTime = 0;
            }
        }

        if (blockEntity.isAboveLitHeatSource() != blockState.get(CrockPotBlock.HAS_FIRE)) {
            world.setBlockState(blockEntity.pos, blockState.with(CrockPotBlock.HAS_FIRE, blockEntity.isAboveLitHeatSource()));
        }
    }
}
