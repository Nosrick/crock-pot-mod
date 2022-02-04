package com.gitlab.nosrick.crockpot.blockentity;

import com.gitlab.nosrick.crockpot.CrockPotMod;
import com.gitlab.nosrick.crockpot.block.CrockPotBlock;
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
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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
    protected List<String> contents = new ArrayList<>();
    protected int curseLevel = 0;
    protected int bonusLevels = 0;

    protected long boilingTime = 0;
    protected long lastTime = 0;

    protected FoodComponent foodComponent;

    protected final CrockPotHungerManager myHungerManager = new CrockPotHungerManager(this);

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

        this.bonusLevels = tag.getInt(BONUS_LEVELS);
        this.boilingTime = tag.getLong(BOILING_TIME);
        this.lastTime = tag.getLong(LAST_TIME);

        this.curseLevel = tag.getInt(CURSE_LEVEL);

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

        nbt.putInt(BONUS_LEVELS, this.bonusLevels);
        nbt.putLong(BOILING_TIME, this.boilingTime);
        nbt.putLong(LAST_TIME, this.lastTime);

        nbt.putInt(CURSE_LEVEL, this.curseLevel);

        NbtList list = new NbtList();
        this.contents.forEach(
                item -> list.add(NbtString.of(item)));
        nbt.put(CONTENTS_NBT, list);
    }

    // this method gets called when you call fakeHunger.eat
    public void add(int food, float saturationModifier) {
        int hunger = Math.round((100f * (((this.portions - 1) * this.hunger) + food) / this.portions) / 100);
        float saturation = ((100 * (((this.portions - 1) * this.saturation) + saturationModifier) / this.portions) / 100);

        this.foodComponent = new FoodComponent.Builder()
                .hunger(hunger)
                .saturationModifier(saturation)
                .build();
    }

    public boolean addFood(ItemStack food) {
        if (!food.isFood()) {
            return false;
        }

        Item foodItem = food.getItem();

        if (this.portions < MAX_PORTIONS) {
            if (this.portions == 0) {
                this.contents = new ArrayList<>();
            }

            this.portions++;

            if (food.getItem() instanceof StewItem) {
                this.curseLevel += 1;
            }

            this.myHungerManager.eat(foodItem, food);
            this.boilingTime = 0;
            this.hunger = this.foodComponent.getHunger();
            this.saturation = this.foodComponent.getSaturationModifier();

            String id = foodItem.getTranslationKey();

            if (!contents.contains(id)) {
                contents.add(id);
            }

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

        if (this.portions > 0) {
            // create a stew from the pot's contents
            ItemStack stew = new ItemStack(ItemRegistry.STEW_ITEM.get());
            float boilingIntensity = this.getBoilingIntensity() / 2f;

            if (curseLevel == 0) {
                StewItem.setHunger(stew, this.hunger + (int) (this.hunger * boilingIntensity));
                StewItem.setSaturation(stew, this.saturation + (this.saturation * boilingIntensity));
            } else {
                StewItem.setHunger(stew, 0);
                StewItem.setSaturation(stew, 0);
                StewItem.setStatusEffect(stew, new StatusEffectInstance(StatusEffects.NAUSEA, 30, 1));
            }

            StewItem.setCurseLevel(stew, this.curseLevel);
            StewItem.setContents(stew, this.contents);

            TranslatableText statusText = new TranslatableText(this.getStewTypeTranslationKey());
            statusText.append(" ");
            if (this.curseLevel > 5) {
                statusText.append(new TranslatableText("item.crockpot.stew.cowl"));
            } else if (this.curseLevel > 0) {
                statusText.append(new TranslatableText("item.crockpot.stew.cursed"));
            } else {
                if (this.contents.size() < 4) {
                    List<Text> list = new ArrayList<>();
                    for (int i = 0; i < this.contents.size(); i++) {
                        String content = this.contents.get(i);
                        TranslatableText text = new TranslatableText(content);
                        list.add(text);
                        if (i < this.contents.size() - 2) {
                            list.add(new LiteralText(", "));
                        } else if (i < this.contents.size() - 1) {
                            list.add(new LiteralText(" & "));
                        }
                    }

                    list.forEach(statusText::append);
                } else {
                    statusText.append(new TranslatableText("item.crockpot.stew.mixed"));
                }
            }

            if (this.curseLevel <= 5) {
                statusText = new TranslatableText("item.crockpot.stew", statusText);
            }
            stew.setCustomName(statusText);
            container.decrement(1);

            this.decrementPortions();

            // if no more portions in the pot, flush out the pot data
            if (this.portions <= 0) {
                this.flush(world, pos, state);
            }

            this.markDirty();
            sendPacketToClient(world, this);

            return stew;
        }

        return null;
    }

    private void flush(World world, BlockPos pos, BlockState state) {
        this.contents = new ArrayList<>();
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
