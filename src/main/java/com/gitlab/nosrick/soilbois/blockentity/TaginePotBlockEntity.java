package com.gitlab.nosrick.soilbois.blockentity;

import com.gitlab.nosrick.soilbois.block.TaginePotBlock;
import com.gitlab.nosrick.soilbois.item.TagineItem;
import com.gitlab.nosrick.soilbois.registry.BlockEntityTypesRegistry;
import com.gitlab.nosrick.soilbois.registry.ItemRegistry;
import com.nhoryzon.mc.farmersdelight.tag.Tags;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class TaginePotBlockEntity extends BlockEntity {

    protected static final String PORTIONS_NBT = "Portions";
    protected static final String HUNGER_NBT = "Hunger";
    protected static final String SATURATION_NBT = "Saturation";
    protected static final String CONTENTS_NBT = "Contents";
    protected static final String NAME_NBT = "Name";

    protected static final int MAX_PORTIONS = 64;

    protected String name = "";
    protected int portions = 0;
    protected int hunger = 0;
    protected float saturation = 0.0F;
    protected float displayTicks = 0.0F;
    protected int displayIndex = 0;
    protected List<String> contents = new ArrayList<>();

    public TaginePotBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesRegistry.TAGINE_POT.get(), pos, state);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        this.name = tag.getString(NAME_NBT);
        this.portions = tag.getInt(PORTIONS_NBT);
        this.hunger = tag.getInt(HUNGER_NBT);
        this.saturation = tag.getFloat(SATURATION_NBT);

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

            this.portions++;
            int foodHunger = foodComponent.getHunger();
            float foodSaturation = foodComponent.getSaturationModifier();

            this.hunger = Math.round((100f * (((this.portions - 1) * this.hunger) + foodHunger) / this.portions) / 100);
            this.saturation = (float) Math.round((100 * (((this.portions - 1) * this.saturation) + foodSaturation) / this.portions) / 100);

            if (!contents.contains(foodItem.getTranslationKey())) {
                contents.add(foodItem.getTranslationKey());
            }

            this.markDirty();

            food.decrement(1);
            return true;
        }

        return false;
    }

    public static void tick(World world, BlockPos pos, BlockState state, TaginePotBlockEntity blockEntity) {
        if (!world.isClient()) {
            serverTick(world, blockEntity);
        } else {
            clientTick(blockEntity);
        }
    }

    protected static void serverTick(World world, TaginePotBlockEntity blockEntity) {

        BlockState blockState = world.getBlockState(blockEntity.pos);

        if (blockEntity.isAboveLitHeatSource() != blockState.get(TaginePotBlock.HAS_FIRE)) {
            world.setBlockState(blockEntity.pos, blockState.with(TaginePotBlock.HAS_FIRE, blockEntity.isAboveLitHeatSource()));
        }
    }

    protected static void clientTick(TaginePotBlockEntity blockEntity) {
        if (blockEntity.isAboveLitHeatSource()) {
            blockEntity.animate();
        }
    }

    @Nullable
    public ItemStack take(World world, BlockPos pos, BlockState state, ItemStack container) {

        if (container.getItem() != Items.BOWL) {
            return null;
        }

        if (this.portions > 0) {
            // create a stew from the pot's contents
            ItemStack stew = new ItemStack(ItemRegistry.TAGINE.get());
            TagineItem.setHunger(stew, this.hunger);
            TagineItem.setSaturation(stew, this.saturation);
            TagineItem.setContents(stew, this.contents);
            container.decrement(1);

            // if no more portions in the pot, flush out the pot data
            if (--this.portions <= 0) {
                this.flush(world, pos, state);
            }

            this.markDirty();

            return stew;
        }

        return null;
    }

    public static FoodComponent getFoodComponent(World world, BlockPos pos, BlockState state){
        if(world.getBlockEntity(pos) instanceof TaginePotBlockEntity taginePotBlockEntity){
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
        world.setBlockState(pos, state.with(TaginePotBlock.LIQUID_LEVEL, 0), 2);

        this.markDirty();
    }

    protected void animate() {
        World world = getWorld();
        if (world != null) {
            BlockPos blockpos = getPos();
            Random random = world.random;
            if (random.nextFloat() < .2f) {
                double baseX = blockpos.getX() + .5d + (random.nextDouble() * .6d - .3d);
                double baseY = blockpos.getY() + .7d;
                double baseZ = blockpos.getZ() + .5d + (random.nextDouble() * .6d - .3d);
                world.addParticle(ParticleTypes.BUBBLE_POP, baseX, baseY, baseZ, .0d, .0d, .0d);
            }
            if (random.nextFloat() < .05f) {
                double baseX = blockpos.getX() + .5d + (random.nextDouble() * .4d - .2d);
                double baseY = blockpos.getY() + .7d;
                double baseZ = blockpos.getZ() + .5d + (random.nextDouble() * .4d - .2d);
                world.addParticle(ParticleTypes.EFFECT, baseX, baseY, baseZ, .0d, .0d, .0d);
            }
        }
    }

    public boolean isAboveLitHeatSource() {
        if (world == null) {
            return false;
        }

        BlockState checkState = world.getBlockState(pos.down());
        if (Tags.HEAT_SOURCES.contains(checkState.getBlock())) {
            if (checkState.contains(Properties.LIT)) {
                return checkState.get(Properties.LIT);
            }

            return true;
        }

        return false;
    }

    public int getPortions() {
        return this.portions;
    }

    public void decrementPortions() {
        this.portions -= 1;
    }

}
