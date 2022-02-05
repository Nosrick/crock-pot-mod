package com.gitlab.nosrick.crockpot.blockentity;

import com.gitlab.nosrick.crockpot.CrockPotMod;
import com.gitlab.nosrick.crockpot.block.CrockPotBlock;
import com.gitlab.nosrick.crockpot.inventory.CrockPotInventory;
import com.gitlab.nosrick.crockpot.item.StewItem;
import com.gitlab.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.gitlab.nosrick.crockpot.registry.CrockPotSoundRegistry;
import com.gitlab.nosrick.crockpot.registry.ItemRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class CrockPotBlockEntity extends BlockEntity implements CrockPotInventory, SidedInventory {

    protected static final String PORTIONS_NBT = "Portions";
    protected static final String HUNGER_NBT = "Hunger";
    protected static final String SATURATION_NBT = "Saturation";
    protected static final String CONTENTS_NBT = "Contents";
    protected static final String NAME_NBT = "Name";
    protected static final String CURSE_LEVEL = "Curse Level";
    protected static final String BONUS_LEVELS = "Bonus Levels";
    protected static final String BOILING_TIME = "Boiling Time";
    protected static final String LAST_TIME = "Last Time";

    public static final int MAX_BONUS_STAGES = 5;
    public static final int MAX_PORTIONS = 64;
    public static final int MAX_BOILING_TIME = 20 * 60 * 2;
    public static final Identifier PACKET_ID = new Identifier(CrockPotMod.MOD_ID, "block.entity.crockpot.update");

    protected String name = "";
    protected int portions = 0;
    protected int hunger = 0;
    protected float saturation = 0.0F;
    protected int curseLevel = 0;
    protected int bonusLevels = 0;

    protected long boilingTime = 0;
    protected long lastTime = 0;

    protected FoodComponent foodComponent;

    protected final CrockPotHungerManager myHungerManager = new CrockPotHungerManager(this);

    protected final DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    protected static final int INVENTORY_SIZE = 9;
    protected static final int OUTPUT_SLOT = 8;

    public CrockPotBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesRegistry.CROCK_POT.get(), pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.name = nbt.getString(NAME_NBT);
        this.portions = nbt.getInt(PORTIONS_NBT);
        this.hunger = nbt.getInt(HUNGER_NBT);
        this.saturation = nbt.getFloat(SATURATION_NBT);

        this.bonusLevels = nbt.getInt(BONUS_LEVELS);
        this.boilingTime = nbt.getLong(BOILING_TIME);
        this.lastTime = nbt.getLong(LAST_TIME);

        this.curseLevel = nbt.getInt(CURSE_LEVEL);

        Inventories.readNbt(nbt, this.items);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putString(NAME_NBT, this.name);
        nbt.putInt(PORTIONS_NBT, this.portions);
        nbt.putInt(HUNGER_NBT, this.hunger);
        nbt.putFloat(SATURATION_NBT, this.saturation);

        nbt.putInt(BONUS_LEVELS, this.bonusLevels);
        nbt.putLong(BOILING_TIME, this.boilingTime);
        nbt.putLong(LAST_TIME, this.lastTime);

        nbt.putInt(CURSE_LEVEL, this.curseLevel);

        Inventories.writeNbt(nbt, this.items);
    }

    public void add(int food, float saturationModifier) {
        int hunger = Math.round((100f * (((this.portions - 1) * this.hunger) + food) / this.portions) / 100);
        float saturation = ((100f * (((this.portions - 1) * this.saturation) + saturationModifier) / this.portions) / 100f);

        this.foodComponent = new FoodComponent.Builder()
                .hunger(hunger)
                .saturationModifier(saturation)
                .build();
    }

    public boolean addFood(ItemStack food) {
        if (!food.isFood() || !this.hasEmptySlot()) {
            return false;
        }

        Item foodItem = food.getItem();

        if (this.portions < MAX_PORTIONS) {
            if (this.portions == 0) {
                this.items.clear();
            }

            this.portions++;

            if (food.getItem() instanceof StewItem) {
                this.curseLevel += 1;
            }

            this.myHungerManager.eat(foodItem, food);
            this.boilingTime = 0;
            this.bonusLevels = 0;
            this.hunger = this.foodComponent.getHunger();
            this.saturation = this.foodComponent.getSaturationModifier();

            if (!this.hasStackOfType(foodItem)) {
                this.setStack(this.getFirstEmptySlot(), new ItemStack(foodItem));
            }

            ItemStack stew = this.makeStew();
            stew.increment(this.portions - 1);
            this.setStack(OUTPUT_SLOT, stew);

            this.markDirty();
            food.decrement(1);

            sendPacketToClient(this.getWorld(), this);

            return true;
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

        ItemStack stew = this.makeStew();
        if (stew != null) {
            this.decrementPortions();
            container.decrement(1);

            if (this.portions <= 0) {
                this.flush(world, pos, state);
            }

            sendPacketToClient(world, this);
            this.markDirty();

            return stew;
        }

        return null;
    }

    protected ItemStack makeStew() {

        if (this.portions > 0) {
            ItemStack stew = new ItemStack(ItemRegistry.STEW_ITEM.get());
            float boilingIntensity = this.getBoilingIntensity() / 2f;

            if (curseLevel == 0) {
                StewItem.setHunger(stew, this.hunger + (int) (this.hunger * boilingIntensity));
                StewItem.setSaturation(stew, this.saturation * (1.0f + (boilingIntensity / 2f)));
                if (this.bonusLevels == MAX_BONUS_STAGES) {
                    StewItem.setStatusEffect(
                            stew,
                            new StatusEffectInstance(
                                    StatusEffects.SATURATION,
                                    60 * 20 * this.bonusLevels,
                                    this.bonusLevels));
                }
            } else {
                StewItem.setHunger(stew, 0);
                StewItem.setSaturation(stew, 0);
                StewItem.setStatusEffect(
                        stew,
                        new StatusEffectInstance(
                                StatusEffects.NAUSEA,
                                5 * 20 * this.curseLevel,
                                this.curseLevel));
            }

            DefaultedList<ItemStack> contents = this.getContents();
            StewItem.setCurseLevel(stew, this.curseLevel);
            StewItem.setContents(stew, contents);

            TranslatableText statusText = new TranslatableText(this.getStewTypeTranslationKey());
            statusText.append(" ");
            if (this.curseLevel > 5) {
                statusText.append(new TranslatableText("item.crockpot.stew.cowl"));
            } else if (this.curseLevel > 0) {
                statusText.append(new TranslatableText("item.crockpot.stew.cursed"));
            } else {

                if (this.filledSlotCount() < 4) {
                    String total = "";
                    for (ItemStack itemStack : contents) {
                        String content = itemStack.getName().getString();
                        total = total.concat(content + " ");
                    }

                    total = total.trim();

                    if (total.length() > 64) {
                        statusText.append(new TranslatableText("item.crockpot.stew.mixed"));
                    } else {
                        List<Text> list = new ArrayList<>();
                        for (int i = 0; i < contents.size(); i++) {
                            ItemStack content = contents.get(i);
                            TranslatableText text = new TranslatableText(content.getTranslationKey());
                            list.add(text);
                            if (i < contents.size() - 2) {
                                list.add(new LiteralText(", "));
                            } else if (i < contents.size() - 1) {
                                list.add(new LiteralText(" & "));
                            }
                        }

                        list.forEach(statusText::append);
                    }
                } else {
                    statusText.append(new TranslatableText("item.crockpot.stew.mixed"));
                }
            }

            if (this.curseLevel <= 5) {
                statusText = new TranslatableText("item.crockpot.stew", statusText);
            }
            stew.setCustomName(statusText);

            return stew;
        }

        return null;
    }

    protected DefaultedList<ItemStack> getContents() {
        DefaultedList<ItemStack> contents = DefaultedList.of();
        contents.addAll(this.items.stream().limit(OUTPUT_SLOT).takeWhile(itemStack -> !itemStack.isEmpty()).toList());
        return contents;
    }

    public void flush(World world, BlockPos pos, BlockState state) {
        this.items.clear();
        this.hunger = 0;
        this.saturation = 0;
        this.portions = 0;
        this.boilingTime = 0;

        this.curseLevel = 0;

        world.setBlockState(
                pos,
                state
                        .with(CrockPotBlock.HAS_LIQUID, false)
                        .with(CrockPotBlock.HAS_FOOD, false));

        this.markDirty();

        sendPacketToClient(this.getWorld(), this);
    }

    public float getBoilingIntensity() {
        return this.bonusLevels / ((float) MAX_BONUS_STAGES);
    }

    public String getStewTypeTranslationKey() {
        int bonusLevels = this.bonusLevels;

        if (bonusLevels == CrockPotBlockEntity.MAX_BONUS_STAGES) {
            return "item.crockpot.stew.hearty";
        }
        if (bonusLevels > 2) {
            return "item.crockpot.stew.filling";
        }
        if (bonusLevels > 0) {
            return "item.crockpot.stew.satisfying";
        }

        return "item.crockpot.stew.plain";
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

    public int getBonusLevels() {
        return this.bonusLevels;
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

            if (blockEntity.boilingTime > MAX_BOILING_TIME
                    && blockEntity.bonusLevels < MAX_BONUS_STAGES) {
                blockEntity.bonusLevels += 1;
                blockEntity.boilingTime = 0;
            }
        }

        if (blockEntity.isAboveLitHeatSource() != blockState.get(CrockPotBlock.HAS_FIRE)) {
            world.setBlockState(blockEntity.pos, blockState.with(CrockPotBlock.HAS_FIRE, blockEntity.isAboveLitHeatSource()));
        }

        sendPacketToClient(world, blockEntity);
    }

    protected static void sendPacketToClient(World world, CrockPotBlockEntity blockEntity) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(blockEntity.pos);
        NbtCompound nbt = new NbtCompound();
        blockEntity.writeNbt(nbt);
        buf.writeNbt(nbt);

        for (ServerPlayerEntity serverPlayer : PlayerLookup.tracking(blockEntity)) {
            ServerPlayNetworking.send(serverPlayer, PACKET_ID, buf);
        }
    }

    public static int getBonusLevels(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof CrockPotBlockEntity potBlockEntity) {
            return potBlockEntity.bonusLevels;
        }

        return 0;
    }

    public static float getBoilingIntensity(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof CrockPotBlockEntity potBlockEntity) {
            return potBlockEntity.getBoilingIntensity();
        }

        return 0;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.UP) {
            return IntStream.range(0, OUTPUT_SLOT).toArray();
        }

        return new int[]{OUTPUT_SLOT};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return dir == Direction.UP && this.portions < MAX_PORTIONS;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir != Direction.UP && this.portions > 0;
    }

    private static class CrockPotHungerManager extends HungerManager {
        private final CrockPotBlockEntity crockPotBlockEntity;

        public CrockPotHungerManager(CrockPotBlockEntity cpbe) {
            this.crockPotBlockEntity = cpbe;
        }

        @Override
        public void add(int food, float saturationModifier) {
            crockPotBlockEntity.add(food, saturationModifier);
        }
    }
}
