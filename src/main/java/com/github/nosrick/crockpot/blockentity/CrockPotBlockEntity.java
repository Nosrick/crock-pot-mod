package com.github.nosrick.crockpot.blockentity;

import com.github.nosrick.crockpot.config.ConfigManager;
import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.block.CrockPotBlock;
import com.github.nosrick.crockpot.item.StewItem;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.github.nosrick.crockpot.registry.CrockPotSoundRegistry;
import com.github.nosrick.crockpot.registry.ItemRegistry;
import com.github.nosrick.crockpot.tag.Tags;
import com.github.nosrick.crockpot.util.NbtListUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.IntStream;

public class CrockPotBlockEntity extends BlockEntity implements Inventory, SidedInventory {

    public static final String PORTIONS_NBT = "Portions";
    public static final String HUNGER_NBT = "Hunger";
    public static final String SATURATION_NBT = "Saturation";
    public static final String NAME_NBT = "Name";
    public static final String EFFECTS_NBT = "Effects";
    public static final String CURSE_LEVEL = "Curse Level";
    public static final String BONUS_LEVELS = "Bonus Levels";
    public static final String BOILING_TIME = "Boiling Time";
    public static final String LAST_TIME = "Last Time";
    public static final String REDSTONE_OUTPUT = "Redstone Output";

    public static final String ELECTRIC_NBT = "Electric";

    public static final Identifier PACKET_ID = new Identifier(CrockPotMod.MOD_ID, "block.entity.crockpot.update");

    protected String name = "";
    protected int hunger = 0;

    protected int cachedPortions = 0;
    protected boolean portionsDirty = false;

    protected float saturation = 0.0F;
    protected int curseLevel = 0;
    protected int bonusLevels = 0;

    protected long boilingTime = 0;
    protected long lastTime = 0;

    protected boolean electric = false;

    protected RedstoneOutputType redstoneOutputType = RedstoneOutputType.BONUS_LEVELS;

    protected final DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

    protected List<StatusEffectInstance> potionEffects;

    protected static final int INVENTORY_SIZE = ConfigManager.ingredientSlots();
    protected static final int OUTPUT_SLOT = INVENTORY_SIZE + 1;

    public enum RedstoneOutputType implements StringIdentifiable {
        NONE("values.crockpot.redstone_output.none", "none", 0),
        BONUS_LEVELS("values.crockpot.redstone_output.bonus_levels", "bonus_levels", 1),
        PORTIONS("values.crockpot.redstone_output.portions", "portions", 2);

        private static final Map<Integer, RedstoneOutputType> VALUES = new HashMap<>();

        public final int value;
        public final String name;
        public final Text localName;

        static {
            for (RedstoneOutputType type : values()) {
                VALUES.put(type.value, type);
            }
        }

        RedstoneOutputType(String translationKey, String name, int value) {
            this.name = name;
            this.localName = Text.translatable(translationKey);
            this.value = value;
        }

        public static RedstoneOutputType getByValue(int value) {
            if (VALUES.containsKey(value)) {
                return VALUES.get(value);
            }

            return VALUES.get(0);
        }

        @Override
        public String asString() {
            return this.name;
        }
    }

    public CrockPotBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntityTypesRegistry.CROCK_POT.get(), pos, state);
    }

    protected CrockPotBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.potionEffects = new ArrayList<>();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.name = nbt.getString(NAME_NBT);
        this.hunger = nbt.getInt(HUNGER_NBT);
        this.saturation = nbt.getFloat(SATURATION_NBT);

        this.bonusLevels = nbt.getInt(BONUS_LEVELS);
        this.boilingTime = nbt.getLong(BOILING_TIME);
        this.lastTime = nbt.getLong(LAST_TIME);

        this.curseLevel = nbt.getInt(CURSE_LEVEL);
        this.electric = nbt.getBoolean(ELECTRIC_NBT);

        this.setRedstoneOutputType(RedstoneOutputType.valueOf(nbt.getString(REDSTONE_OUTPUT)));

        Inventories.readNbt(nbt, this.items);

        this.potionEffects.clear();

        if (nbt.contains(EFFECTS_NBT)) {
            NbtList nbtList = (NbtList) nbt.get(EFFECTS_NBT);
            this.potionEffects = NbtListUtil.effectInstanceCollectionFromNbtList(nbtList).stream().toList();
        }

        this.markPortionsDirty();

        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putString(NAME_NBT, this.name);
        nbt.putInt(HUNGER_NBT, this.hunger);
        nbt.putFloat(SATURATION_NBT, this.saturation);

        nbt.putInt(BONUS_LEVELS, this.bonusLevels);
        nbt.putLong(BOILING_TIME, this.boilingTime);
        nbt.putLong(LAST_TIME, this.lastTime);

        nbt.putInt(CURSE_LEVEL, this.curseLevel);
        nbt.putBoolean(ELECTRIC_NBT, this.electric);

        nbt.putString(REDSTONE_OUTPUT, this.redstoneOutputType.toString());

        Inventories.writeNbt(nbt, this.items);

        if (!this.potionEffects.isEmpty()) {
            nbt.put(EFFECTS_NBT, NbtListUtil.nbtListFromStatusEffectInstances(this.potionEffects));
        }

        super.writeNbt(nbt);
    }

    public void addFoodValues(int hunger, float saturationModifier) {
        int portions = this.getPortions();
        this.hunger = Math.round((100f * (((portions - 1) * this.hunger) + hunger) / portions) / 100);
        this.saturation = ((100f * (((portions - 1) * this.saturation) + saturationModifier) / portions) / 100f);
    }

    public boolean canAddFood(ItemStack food) {
        if ((!ConfigManager.canAddPotions()
                && food.getItem() instanceof PotionItem
                || this.potionEffects.size() >= ConfigManager.effectPerPot())
                && !food.isFood()) {
            return false;
        }

        if (!this.canBoil()) {
            return false;
        }

        if (!this.hasEmptySlot() && !this.hasStackOfType(food.getItem())) {
            return false;
        }

        if (this.getPortions() >= ConfigManager.maxPortionsPerPot()) {
            return false;
        }

        return true;
    }

    public boolean addFood(ItemStack food, PlayerEntity player) {
        if (this.addFood(food) && !player.isCreative()) {
            food.decrement(1);
            return true;
        }

        return false;
    }

    public boolean addFood(ItemStack food) {
        if (ConfigManager.canAddPotions()
            && food.getItem() instanceof PotionItem) {
            Potion potion = PotionUtil.getPotion(food);
            if (this.potionEffects.size() < ConfigManager.effectPerPot()) {
                for (StatusEffectInstance effectInstance : potion.getEffects()) {
                    if (this.potionEffects.size() < ConfigManager.effectPerPot()) {
                        if (this.potionEffects.stream()
                                .noneMatch(effect ->
                                        effect.getEffectType() == effectInstance.getEffectType())) {
                            this.potionEffects.add(effectInstance);
                        }
                        else {
                            var oldEffectOptional = this.potionEffects.stream().filter(effect ->
                                    effect.getEffectType() == effectInstance.getEffectType()).findFirst();

                            if(oldEffectOptional.isPresent()) {
                                StatusEffectInstance oldEffect = oldEffectOptional.get();
                                if(oldEffect.getDuration() < effectInstance.getDuration()
                                    || oldEffect.getDuration() < effectInstance.getAmplifier())
                                {
                                    this.potionEffects.remove(oldEffect);
                                    this.potionEffects.add(effectInstance);
                                }
                            }
                        }
                    }
                    else {
                        break;
                    }
                }

                this.markDirty();
                if (this.hasWorld()) {
                    this.world.updateNeighborsAlways(this.pos, this.getCachedState().getBlock());
                }
                this.updateNearby();

                return true;
            }

            return false;
        }

        if (!this.canAddFood(food)) {
            return false;
        }

        Item foodItem = food.getItem();

        if (food.getItem() instanceof StewItem
                && ConfigManager.useCursedStew()) {
            this.curseLevel += 1;
        }

        FoodComponent foodComponent = foodItem.getFoodComponent();
        if(foodComponent == null)
        {
            return false;
        }
        this.boilingTime = 0;
        this.bonusLevels = 0;

        if (!this.hasStackOfType(foodItem)) {
            this.items.set(this.getFirstEmptySlot(), new ItemStack(foodItem));
            this.markPortionsDirty();
        } else {
            this.getStackOfType(foodItem).increment(1);
            this.markPortionsDirty();
        }

        this.addFoodValues(foodComponent.getHunger(), foodComponent.getSaturationModifier());

        this.markDirty();
        if (this.hasWorld()) {
            this.world.updateNeighborsAlways(this.pos, this.getCachedState().getBlock());
        }
        this.updateNearby();

        return true;
    }

    @Nullable
    public ItemStack take(World world, ItemStack container, PlayerEntity player) {

        if (world.isClient) {
            return null;
        }

        if (container.getItem() != Items.BOWL) {
            return null;
        }

        ItemStack stew = this.makeStew();
        if (stew != null) {
            if (!player.isCreative()) {
                this.decrementPortions(1);
                container.decrement(1);
            }

            if (this.getPortions() <= 0) {
                this.flush();
            }

            this.markDirty();

            if (this.hasWorld()) {
                this.world.updateNeighborsAlways(this.pos, this.getCachedState().getBlock());
            }

            this.updateNearby();

            return stew;
        }

        return null;
    }

    protected ItemStack makeStew() {

        if (this.getPortions() > 0) {
            ItemStack stew = new ItemStack(ItemRegistry.STEW_ITEM.get());
            float boilingIntensity = this.getBoilingIntensity() / 2f;

            if (!ConfigManager.useCursedStew() || this.curseLevel < ConfigManager.stewMinNegativeLevelsEffect()) {
                StewItem.setHunger(stew, this.hunger + (int) (this.hunger * boilingIntensity));
                StewItem.setSaturation(stew, this.saturation * (1.0f + (boilingIntensity / 2f)));
                if (ConfigManager.useItemPositiveEffects()
                    && this.bonusLevels >= ConfigManager.stewMinPositiveLevelsEffect()) {
                    if(!ConfigManager.effectsOverride()) {
                        this.addEffectToStew(stew);
                    }
                    else if(this.potionEffects.isEmpty()) {
                        this.addEffectToStew(stew);
                    }
                }
                if (ConfigManager.canAddPotions()) {
                    int max = Math.min(ConfigManager.effectPerPot(), this.potionEffects.size());
                    for (int i = 0; i < max; i++) {
                        StatusEffectInstance effect = this.potionEffects.get(i);
                        StewItem.addStatusEffect(
                                stew,
                                effect);
                    }
                }
            } else {
                StewItem.setHunger(stew, 0);
                StewItem.setSaturation(stew, 0);
                if (ConfigManager.useItemNegativeEffects()) {
                    int duration = ConfigManager.baseNauseaDuration() * 20 * this.curseLevel;
                    StewItem.addStatusEffect(
                            stew,
                            new StatusEffectInstance(
                                    StatusEffects.NAUSEA,
                                    ConfigManager.cappedNauseaDuration()
                                            ? Math.min(ConfigManager.maxNauseaDuration(), duration)
                                            : duration,
                                    Math.min(this.curseLevel, 5)));
                }
            }

            DefaultedList<ItemStack> contents = this.getContents();
            StewItem.setCurseLevel(stew, ConfigManager.useCursedStew() ? this.curseLevel : 0);
            StewItem.setContents(stew, contents);

            MutableText statusText = Text.translatable(this.getStewTypeTranslationKey());
            statusText.append(" ");
            if (ConfigManager.useCursedStew()
                    && this.curseLevel >= ConfigManager.minCowlLevel()) {
                statusText.append(Text.translatable("item.crockpot.stew.cowl"));
            } else if (ConfigManager.useCursedStew()
                    && this.curseLevel >= ConfigManager.stewMinNegativeLevelsEffect()) {
                statusText.append(Text.translatable("item.crockpot.stew.cursed"));
            } else {
                if (this.filledSlotCount() < 4) {
                    String total = "";
                    for (ItemStack itemStack : contents) {
                        String content = itemStack.getName().getString();
                        total = total.concat(content + " ");
                    }

                    total = total.trim();

                    if (total.length() > ConfigManager.maxStewNameLength()) {
                        statusText.append(Text.translatable("item.crockpot.stew.mixed"));
                    } else {
                        List<Text> list = new ArrayList<>();
                        for (int i = 0; i < contents.size(); i++) {
                            ItemStack content = contents.get(i);

                            Text text = Text.translatable(
                                    content.getItem() instanceof StewItem
                                            ? "item.crockpot.stew_name"
                                            : content.getTranslationKey());

                            list.add(text);
                            if (i < contents.size() - 2) {
                                list.add(Text.of(", "));
                            } else if (i < contents.size() - 1) {
                                list.add(Text.of(" & "));
                            }
                        }

                        list.forEach(statusText::append);
                    }
                } else {
                    statusText.append(Text.translatable("item.crockpot.stew.mixed"));
                }
            }

            if (!ConfigManager.useCursedStew()
                    || this.curseLevel < ConfigManager.minCowlLevel()) {
                statusText = Text.translatable("item.crockpot.stew", statusText);
            }
            stew.setCustomName(statusText);

            return stew;
        }

        return ItemStack.EMPTY;
    }

    protected void addEffectToStew(ItemStack stew) {
        int duration = ConfigManager.basePositiveDuration() * 20 * this.bonusLevels;
        StewItem.addStatusEffect(
                stew,
                new StatusEffectInstance(
                        StatusEffects.SATURATION,
                        ConfigManager.cappedPositiveDuration()
                                ? Math.min(ConfigManager.maxPositiveDuration(), duration)
                                : duration,
                        Math.min(this.bonusLevels, ConfigManager.maxBonusLevels())));
    }

    public DefaultedList<ItemStack> getContents() {
        DefaultedList<ItemStack> contents = DefaultedList.ofSize(ConfigManager.ingredientSlots());
        contents.addAll(this.items.stream().takeWhile(itemStack -> !itemStack.isEmpty()).toList());
        return contents;
    }

    public void flush() {
        this.hunger = 0;
        this.saturation = 0;
        this.boilingTime = 0;
        this.bonusLevels = 0;

        this.curseLevel = 0;

        if (this.hasWorld()) {
            this.getWorld().setBlockState(
                    this.pos,
                    this.getCachedState()
                            .with(CrockPotBlock.HAS_LIQUID, false));
        }

        this.clear();
        this.updateNearby();
    }

    public boolean hasFood() {
        return this.getPortions() > 0;
    }

    public boolean isElectric() {
        return this.electric;
    }

    public void setElectric(boolean value) {
        this.electric = value;
        this.markDirty();
        this.updateNearby();
    }

    public float getBoilingIntensity() {
        return this.bonusLevels > 0
                ? this.bonusLevels / ((float) ConfigManager.maxBonusLevels())
                : 0;
    }

    public void setRedstoneOutputType(RedstoneOutputType type) {
        this.redstoneOutputType = type;

        this.markDirty();

        if (!this.hasWorld()) {
            return;
        }

        if (this.redstoneOutputType == RedstoneOutputType.NONE) {
            this.world.setBlockState(this.pos, this.getCachedState().with(CrockPotBlock.EMITS_SIGNAL, false));
        } else {
            this.world.setBlockState(this.pos, this.getCachedState().with(CrockPotBlock.EMITS_SIGNAL, true));
        }

        if (this.world.isClient) {
            return;
        }

        this.updateNearby();
    }

    protected void updateNearby() {
        if (!this.hasWorld()) {
            return;
        }

        for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) this.world, this.pos)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(this.pos);
            buf.writeNbt(this.createNbt());
            ServerPlayNetworking.send(player, CrockPotMod.CROCK_POT_CHANNEL, buf);
        }
    }

    public boolean canBoil() {
        if (this.isElectric()) {
            if (ConfigManager.redstoneNeedsPower()) {
                if (this.hasWorld()
                        && this.world.getReceivedRedstonePower(this.pos) > ConfigManager.redstonePowerThreshold()) {
                    return true;
                } else {
                    return false;
                }
            }
            return true;
        }

        return this.isAboveLitHeatSource();
    }

    public RedstoneOutputType getRedstoneOutputType() {
        return this.redstoneOutputType;
    }

    public String getStewTypeTranslationKey() {
        int bonusLevels = this.bonusLevels;

        if (bonusLevels >= ConfigManager.minHeartyLevels()) {
            return "item.crockpot.stew.hearty";
        }
        if (bonusLevels >= ConfigManager.minFillingLevels()) {
            return "item.crockpot.stew.filling";
        }
        if (bonusLevels >= ConfigManager.minSatisfyingLevels()) {
            return "item.crockpot.stew.satisfying";
        }

        return "item.crockpot.stew.plain";
    }

    public boolean isAboveLitHeatSource() {
        if (world == null) {
            return false;
        }

        BlockState checkState = world.getBlockState(pos.down());

        return checkState.isIn(Tags.HEAT_SOURCES);
    }

    public int getPortions() {
        return this.getFullStackCount();
    }

    public List<StatusEffectInstance> getPotionEffects() {
        return new ArrayList<>(this.potionEffects);
    }

    public int getBonusLevels() {
        return this.bonusLevels;
    }

    public void decrementPortions(int amount) {
        int ticker = amount;
        for (ItemStack content : this.items) {
            if (ticker <= 0) {
                break;
            }

            if (content.getCount() >= ticker) {
                content.decrement(ticker);
                ticker = 0;
            } else {
                ticker -= content.getCount();
                content.decrement(content.getCount());
            }
        }

        this.markPortionsDirty();
        this.markDirty();

        if (this.getPortions() == 0) {
            this.flush();
        }
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        if (world == null) {
            return;
        }

        CrockPotBlockEntity crockPotBlockEntity = (CrockPotBlockEntity) blockEntity;

        if (!world.isClient) {
            serverTick(world, crockPotBlockEntity);
        }

        Random random = world.random;

        float volume = ConfigManager.soundEffectVolume();

        if (ConfigManager.useBoilSound()
                && blockState.get(CrockPotBlock.HAS_LIQUID)) {
            if (random.nextInt(ConfigManager.boilSoundChance()) == 0) {
                float variation = random.nextFloat() / 5f - 0.1f;
                world.playSound(null, blockPos, CrockPotSoundRegistry.CROCK_POT_BOIL.get(), SoundCategory.BLOCKS, volume, 1.0f + variation);
            }
        }

        if (ConfigManager.useBubbleSound()
                && crockPotBlockEntity.getPortions() > 0) {
            if (random.nextInt(ConfigManager.bubbleSoundChance()) == 0) {
                float variation = random.nextFloat() / 5f - 0.1f;
                world.playSound(null, blockPos, CrockPotSoundRegistry.CROCK_POT_BUBBLE.get(), SoundCategory.BLOCKS, volume, 1.0f + variation);
            }
        }
    }

    protected static void serverTick(World world, CrockPotBlockEntity blockEntity) {

        BlockState blockState = world.getBlockState(blockEntity.pos);

        if (blockEntity.canBoil()) {
            if (blockEntity.getPortions() > 0) {
                long time = world.getTime();
                if (blockEntity.lastTime != 0) {
                    blockEntity.boilingTime += time - blockEntity.lastTime;
                }
                blockEntity.lastTime = time;

                if (blockEntity.boilingTime > ConfigManager.boilTimePerLevel()
                        && blockEntity.bonusLevels < ConfigManager.maxBonusLevels()) {
                    blockEntity.bonusLevels += 1;
                    blockEntity.boilingTime -= ConfigManager.boilTimePerLevel();
                    blockEntity.markDirty();
                    world.updateListeners(blockEntity.pos, blockState, blockState, 0);
                    world.updateNeighborsAlways(blockEntity.pos, blockState.getBlock());
                }
            }
        } else {
            blockEntity.lastTime = world.getTime();
        }
    }

    protected void markPortionsDirty() {
        this.portionsDirty = true;
    }

    //Sums the number of items in each stack in the pot
    public int getFullStackCount() {
        if (!this.portionsDirty) {
            return this.cachedPortions;
        }

        int total = 0;

        if (!this.isEmpty()) {
            DefaultedList<ItemStack> contents = this.getContents();
            for (int i = 0; i < contents.size(); i++) {
                ItemStack stack = contents.get(i);

                total += stack.getCount();
            }
        }

        this.cachedPortions = total;
        this.portionsDirty = false;
        return total;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot < this.size()) {
            return this.items.get(slot);
        } else if (slot == OUTPUT_SLOT && this.getPortions() > 0) {
            ItemStack stew = this.makeStew();
            this.decrementPortions(1);
            return stew;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot < this.size()) {
            ItemStack result = Inventories.splitStack(this.items, slot, amount);
            if (!result.isEmpty()) {
                this.markPortionsDirty();
                this.markDirty();
            }
        } else if (slot == this.size()) {
            ItemStack stew = this.makeStew();
            this.decrementPortions(amount);
            if (amount > 1) {
                int count = amount - 1;
                stew.increment(count);
            }
            this.markPortionsDirty();
            this.markDirty();
            return stew;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return this.removeStack(slot, 1);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot < this.size() && this.canAddFood(stack)) {
            if (this.hasStackOfType(stack.getItem())) {
                ItemStack currentStack = this.getStackOfType(stack.getItem());
                currentStack.increment(stack.getCount());
            }
            this.addFood(stack);
            this.markPortionsDirty();
            this.markDirty();
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.items.clear();
        this.markPortionsDirty();
        this.markDirty();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.UP) {
            return IntStream.range(0, this.size()).toArray();
        }

        return new int[]{OUTPUT_SLOT};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        boolean result = dir == Direction.UP
                && this.canBoil()
                && this.getCachedState().get(CrockPotBlock.HAS_LIQUID)
                && this.getPortions() < ConfigManager.maxPortionsPerPot()
                && (this.hasEmptySlot() || this.hasStackOfType(stack.getItem()));

        return result;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir != Direction.UP && this.getPortions() > 0;
    }

    protected int filledSlotCount() {
        int count = 0;
        for (int i = 0; i < size(); i++) {
            if (getStack(i) != ItemStack.EMPTY) {
                count++;
            }
        }

        return count;
    }

    protected int emptySlotCount() {
        int count = 0;
        for (int i = 0; i < size(); i++) {
            if (getStack(i) == ItemStack.EMPTY) {
                count++;
            }
        }

        return count;
    }

    protected int getFirstEmptySlot() {
        for (int i = 0; i < size(); i++) {
            if (getStack(i) == ItemStack.EMPTY) {
                return i;
            }
        }

        return -1;
    }

    protected boolean hasEmptySlot() {
        for (int i = 0; i < size(); i++) {
            if (getStack(i) == ItemStack.EMPTY) {
                return true;
            }
        }

        return false;
    }

    protected boolean hasStackOfType(Item item) {
        for (int i = 0; i < size(); i++) {
            if (getStack(i).getItem() == item) {
                return true;
            }
        }

        return false;
    }

    protected ItemStack getStackOfType(Item item) {
        for (int i = 0; i < size(); i++) {
            if (this.getStack(i).getItem() == item) {
                return this.getStack(i);
            }
        }

        return ItemStack.EMPTY;
    }
}
