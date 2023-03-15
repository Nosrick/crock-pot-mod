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
import com.github.nosrick.crockpot.util.UUIDUtil;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeType;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
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

    public static final String OWNER_NBT = "Owner";

    public static final Identifier PACKET_ID = new Identifier(CrockPotMod.MOD_ID, "block.entity.crockpot.update");

    protected String name = "";
    protected int hunger = 0;

    protected int portions = 0;

    protected float saturation = 0.0F;
    protected int curseLevel = 0;
    protected int bonusLevels = 0;

    protected long boilingTime = 0;
    protected long lastTime = 0;

    protected UUID owner = UUIDUtil.NO_PLAYER;
    protected Text ownerName = Text.empty();

    protected RedstoneOutputType redstoneOutputType = RedstoneOutputType.BONUS_LEVELS;

    protected List<StatusEffectInstance> potionEffects;

    protected final DefaultedList<ItemStack> items = DefaultedList.ofSize(TOTAL_SLOTS, ItemStack.EMPTY);

    protected static final int AVAILABLE_INVENTORY = ConfigManager.ingredientSlots();
    protected static final int TOTAL_SLOTS = AVAILABLE_INVENTORY + 2;
    protected static final int BOWL_SLOT = AVAILABLE_INVENTORY;
    protected static final int OUTPUT_SLOT = BOWL_SLOT + 1;

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
        this.portions = nbt.getInt(PORTIONS_NBT);

        this.bonusLevels = nbt.getInt(BONUS_LEVELS);
        this.boilingTime = nbt.getLong(BOILING_TIME);
        this.lastTime = nbt.getLong(LAST_TIME);

        this.curseLevel = nbt.getInt(CURSE_LEVEL);

        Inventories.readNbt(nbt, this.items);

        this.potionEffects.clear();

        if (ConfigManager.canLockPots()) {
            this.setOwner(nbt.getUuid(OWNER_NBT));
        }

        if (nbt.contains(EFFECTS_NBT)) {
            NbtList nbtList = (NbtList) nbt.get(EFFECTS_NBT);
            this.potionEffects = new ArrayList<>(NbtListUtil.effectInstanceCollectionFromNbtList(nbtList));
        }

        this.setRedstoneOutputType(RedstoneOutputType.valueOf(nbt.getString(REDSTONE_OUTPUT)));

        this.markDirty();

        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putString(NAME_NBT, this.name);
        nbt.putInt(HUNGER_NBT, this.hunger);
        nbt.putFloat(SATURATION_NBT, this.saturation);
        nbt.putInt(PORTIONS_NBT, this.portions);

        nbt.putInt(BONUS_LEVELS, this.bonusLevels);
        nbt.putLong(BOILING_TIME, this.boilingTime);
        nbt.putLong(LAST_TIME, this.lastTime);

        nbt.putInt(CURSE_LEVEL, this.curseLevel);

        nbt.putString(REDSTONE_OUTPUT, this.redstoneOutputType.toString());

        if (ConfigManager.canLockPots()) {
            nbt.putUuid(OWNER_NBT, this.owner);
        }

        Inventories.writeNbt(nbt, this.items);

        if (!this.potionEffects.isEmpty()) {
            nbt.put(EFFECTS_NBT, NbtListUtil.nbtListFromStatusEffectInstances(this.potionEffects));
        }

        super.writeNbt(nbt);
    }

    protected void recalculateFoodValues() {
        int portions = this.getPortions();

        if(portions == 0) {
            this.hunger = 0;
            this.saturation = 0;
            return;
        }

        int combinedHunger = 0;
        float combinedSaturation = 0f;

        for (ItemStack itemStack : this.getContents()) {
            Item item = itemStack.getItem();
            if (item.isFood()) {
                FoodComponent foodComponent = item.getFoodComponent();

                if (foodComponent == null) {
                    continue;
                }

                combinedHunger += foodComponent.getHunger();
                combinedSaturation += foodComponent.getSaturationModifier();
            }
        }

        this.hunger = combinedHunger / this.getContents().size();
        this.saturation = combinedSaturation / this.getContents().size();
    }

    public boolean canAddFood(ItemStack food) {
        if (!this.canAddPotion(food) && !food.isFood()) {
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

    public boolean canAddPotion(ItemStack potion) {
        Item potionItem = potion.getItem();

        if (!(potionItem instanceof PotionItem)) {
            return false;
        }

        if (!ConfigManager.canAddPotions()) {
            return false;
        }

        if (this.potionEffects.size() >= ConfigManager.effectPerPot()) {
            return false;
        }

        return true;
    }

    public boolean addFood(ItemStack food, PlayerEntity player) {
        if (this.addFood(food)) {
            if (!player.isCreative()) {
                food.decrement(1);
            }
            this.portions += 1;
            return true;
        }

        return false;
    }

    public boolean addFood(ItemStack food) {
        if (!this.canAddFood(food)) {
            return false;
        }

        Item foodItem = food.getItem();

        if (food.getItem() instanceof StewItem
                && ConfigManager.useCursedStew()) {
            this.curseLevel += 1;
        }

        FoodComponent foodComponent = foodItem.getFoodComponent();
        if (foodComponent == null && foodItem.isFood()) {
            return false;
        }

        this.boilingTime = 0;
        this.bonusLevels = 0;

        if (!this.hasStackOfType(foodItem)) {
            this.items.set(this.getFirstEmptySlot(), new ItemStack(foodItem));
        }

        this.markDirty();

        if (ConfigManager.canAddPotions()) {
            if (food.getItem() instanceof PotionItem) {
                Potion potion = PotionUtil.getPotion(food);
                if (this.potionEffects.size() < ConfigManager.effectPerPot()) {
                    this.addStatusEffects(potion.getEffects());

                    this.markDirty();
                    this.updateNearby();

                    return true;
                }

                return false;
            }
            else if (foodItem.getFoodComponent() != null) {
                var effects = foodItem.getFoodComponent().getStatusEffects();
                if (!effects.isEmpty()) {
                    this.addStatusEffects(effects.stream().map(Pair::getFirst).toList());
                }

                if (foodItem instanceof SuspiciousStewItem) {
                    List<StatusEffectInstance> effectsFromSuspiciousStew = NbtListUtil.getEffectsFromSuspiciousStew(food);
                    this.addStatusEffects(effectsFromSuspiciousStew);
                }
            }
        }
        this.recalculateFoodValues();

        if(this.hasWorld())
        {
            this.getWorld().setBlockState(this.pos, this.getCachedState()
                    .with(CrockPotBlock.HAS_FOOD, true));
        }
        this.updateNearby();

        return true;
    }

    protected void addStatusEffects(List<StatusEffectInstance> effects) {
        int countAdded = 0;

        for (StatusEffectInstance effectInstance : effects) {
            if (this.potionEffects.size() < ConfigManager.effectPerPot()) {
                if (this.potionEffects.stream()
                        .noneMatch(effect ->
                                effect.getEffectType() == effectInstance.getEffectType())) {
                    if (ConfigManager.diluteEffects()) {
                        this.potionEffects.add(this.diluteEffect(effectInstance));
                    } else {
                        this.potionEffects.add(effectInstance);
                    }
                    countAdded++;
                } else {
                    var oldEffectOptional = this.potionEffects.stream().filter(effect ->
                            effect.getEffectType() == effectInstance.getEffectType()).findFirst();

                    if (oldEffectOptional.isPresent()) {
                        StatusEffectInstance oldEffect = oldEffectOptional.get();
                        if (ConfigManager.diluteEffects()) {
                            StatusEffectInstance diluted = this.diluteEffect(effectInstance);
                            if (this.shouldReplaceEffect(oldEffect, diluted)) {
                                this.potionEffects.remove(oldEffect);
                                this.potionEffects.add(diluted);
                                countAdded++;
                            }
                        } else if (this.shouldReplaceEffect(oldEffect, effectInstance)) {
                            this.potionEffects.remove(oldEffect);
                            this.potionEffects.add(effectInstance);
                            countAdded++;
                        }
                    }
                }
            } else {
                break;
            }
        }

    }

    protected StatusEffectInstance diluteEffect(StatusEffectInstance effectInstance) {
        float durationModifier = this.getPortions() * ConfigManager.dilutionModifier();
        return new StatusEffectInstance(
                effectInstance.getEffectType(),
                (int) (effectInstance.getDuration() / durationModifier));
    }

    protected boolean shouldReplaceEffect(StatusEffectInstance oldEffect, StatusEffectInstance newEffect) {
        if (newEffect.getDuration() > oldEffect.getDuration()
                || newEffect.getAmplifier() > oldEffect.getAmplifier()) {
            return true;
        }

        return false;
    }

    protected void recalculateStatusEffects() {
        if(ConfigManager.diluteEffects())
        {
            for (int i = 0; i < this.potionEffects.size(); i++) {
                StatusEffectInstance effectInstance = potionEffects.get(i);

                StatusEffectInstance newDuration = new StatusEffectInstance(effectInstance.getEffectType(), (int) (effectInstance.getDuration()));
                this.potionEffects.set(i, newDuration);
            }
        }
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
                    if (!ConfigManager.effectsOverride()) {
                        this.addEffectToStew(stew);
                    } else if (this.potionEffects.isEmpty()) {
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
        contents.addAll(this.items.stream().filter(itemStack -> !itemStack.isEmpty() && itemStack.getItem() != ItemRegistry.STEW_ITEM.get()).toList());
        return contents;
    }

    public void flush() {
        this.hunger = 0;
        this.saturation = 0;
        this.portions = 0;
        this.boilingTime = 0;
        this.bonusLevels = 0;

        this.curseLevel = 0;

        if (this.world != null) {
            this.world.setBlockState(
                    this.pos,
                    this.getCachedState()
                            .with(CrockPotBlock.HAS_LIQUID, false)
                            .with(CrockPotBlock.HAS_FOOD, false));
        }

        this.clear();
        this.updateNearby();
    }

    public UUID getOwner() {
        return this.owner;
    }

    public Text getOwnerName() {
        if (!this.isOwner(UUIDUtil.NO_PLAYER)
                && Objects.equals(this.ownerName, Text.empty())
                && this.world != null) {
            PlayerEntity player = this.world.getPlayerByUuid(this.owner);
            if (player != null) {
                this.ownerName = player.getDisplayName();
            } else {
                return Text.translatable("tooltip.crockpot.no_player_name");
            }
        }

        return this.ownerName;
    }

    public boolean isOwner(UUID test) {
        return test.compareTo(this.owner) == 0;
    }

    public void setOwner(UUID owner) {
        if (owner != null && !this.isOwner(owner)) {
            this.owner = owner;
            if (this.world == null) {
                return;
            }
            PlayerEntity playerOwner = this.world.getPlayerByUuid(this.owner);
            if (playerOwner != null) {
                this.ownerName = playerOwner.getDisplayName();
            }
            this.markDirty();
            if (!this.world.isClient) {
                this.updateNearby();
            }
        } else if (owner == null) {
            this.owner = UUIDUtil.NO_PLAYER;
            this.ownerName = Text.empty();
            this.markDirty();
            if (this.world != null && !this.world.isClient) {
                this.updateNearby();
            }
        }
    }

    public boolean hasFood() {
        return this.getPortions() > 0;
    }

    public float getBoilingIntensity() {
        return this.bonusLevels > 0
                ? this.bonusLevels / ((float) ConfigManager.maxBonusLevels())
                : 0;
    }

    public void setRedstoneOutputType(RedstoneOutputType type) {
        this.redstoneOutputType = type;

        this.markDirty();

        if (this.world == null) {
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
        if (this.world == null) {
            return;
        }
        world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 0);
        world.updateNeighborsAlways(this.pos, this.getCachedState().getBlock());

        for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) this.world, this.pos)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(this.pos);
            buf.writeNbt(this.createNbt());
            ServerPlayNetworking.send(player, CrockPotMod.CROCK_POT_CHANNEL, buf);
        }
    }

    public boolean canBoil() {
        return this.getCachedState().get(CrockPotBlock.HAS_LIQUID) && this.isAboveLitHeatSource();
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
        return this.portions;
    }

    public List<StatusEffectInstance> getPotionEffects() {
        return new ArrayList<>(this.potionEffects);
    }

    public int getBonusLevels() {
        return this.bonusLevels;
    }

    public void decrementPortions(int amount) {
        int ticker = amount;

        this.portions = Math.max(0, this.portions - amount);

        this.getStack(OUTPUT_SLOT).decrement(amount);
        this.markDirty();

        if (this.portions == 0) {
            this.flush();
        }
    }

    protected void cookRawFood() {
        for (ItemStack stack : this.getContents()) {
            SimpleInventory inv = new SimpleInventory(stack);
            Optional<CampfireCookingRecipe> possibleRecipe = world.getRecipeManager()
                    .getFirstMatch(
                            RecipeType.CAMPFIRE_COOKING,
                            inv,
                            world);

            if (possibleRecipe.isEmpty()) {
                continue;
            }

            int rawCount = stack.getCount();
            int rawSlot = this.getSlotForItem(stack.getItem());

            var possibleStatus = PotionUtil.getPotionEffects(stack);

            Item rawItem = stack.getItem();
            if (rawItem.getFoodComponent() != null) {
                var foodEffects = rawItem
                        .getFoodComponent()
                        .getStatusEffects()
                        .stream()
                        .map(Pair::getFirst)
                        .toList();
                possibleStatus.addAll(foodEffects);
            }
            for (StatusEffectInstance effect : possibleStatus) {
                var removeEffects = this.potionEffects.stream()
                        .filter(status ->
                                status.getEffectType().equals(effect.getEffectType()))
                        .toList();

                this.potionEffects.removeAll(removeEffects);
            }

            Item cookedItem = possibleRecipe.get().getOutput().getItem();

            int cookedSlot = this.getSlotForItem(cookedItem);

            if (cookedSlot >= 0) {
                ItemStack cookedStack = this.getStack(cookedSlot);
                cookedStack.increment(rawCount);
                this.removeStack(rawSlot);
            } else {
                this.removeStack(rawSlot);
                this.setStack(rawSlot, new ItemStack(cookedItem, rawCount));
            }

            if (cookedItem.getFoodComponent() != null) {
                var cookedEffects = cookedItem.getFoodComponent()
                        .getStatusEffects()
                        .stream()
                        .map(Pair::getFirst)
                        .toList();
                this.addStatusEffects(cookedEffects);
            }
            this.addStatusEffects(PotionUtil.getPotionEffects(new ItemStack(cookedItem)));
        }

        this.recalculateFoodValues();
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        if (world == null) {
            return;
        }

        CrockPotBlockEntity crockPotBlockEntity = (CrockPotBlockEntity) blockEntity;

        if (!world.isClient) {
            serverTick(world, crockPotBlockEntity);
        }

        var random = world.random;

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

                if (ConfigManager.cookRawFood()
                        && !ConfigManager.cookFoodOnlyOnLevelUp()) {
                    blockEntity.cookRawFood();
                }

                long time = world.getTime();
                if (blockEntity.lastTime != 0) {
                    blockEntity.boilingTime += time - blockEntity.lastTime;
                }
                blockEntity.lastTime = time;

                if (blockEntity.boilingTime > ConfigManager.boilTimePerLevel()
                        && blockEntity.bonusLevels < ConfigManager.maxBonusLevels()) {
                    blockEntity.bonusLevels += 1;
                    blockEntity.boilingTime -= ConfigManager.boilTimePerLevel();

                    if (ConfigManager.cookRawFood()
                            && ConfigManager.cookFoodOnlyOnLevelUp()) {
                        blockEntity.cookRawFood();
                    }

                    blockEntity.markDirty();
                    world.updateListeners(blockEntity.pos, blockState, blockState, 0);
                    world.updateNeighborsAlways(blockEntity.pos, blockState.getBlock());
                }
            }
        } else {
            blockEntity.lastTime = world.getTime();
        }
    }

    protected void recalculateStews() {
        ItemStack stews;
        stews = this.makeStew();

        if (!(stews.getItem() instanceof StewItem)) {
            return;
        }

        stews.setCount(this.getPortions());
        this.items.set(OUTPUT_SLOT, stews);
    }

    @Override
    public void markDirty() {
        this.recalculateStatusEffects();
        this.recalculateStews();
        super.markDirty();
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
        if (slot < AVAILABLE_INVENTORY) {
            return this.items.get(slot);
        } else if (slot == BOWL_SLOT) {
            return this.items.get(BOWL_SLOT);
        } else if (slot == OUTPUT_SLOT
                && this.getPortions() > 0
                && this.items.get(BOWL_SLOT).getCount() > 0) {
            return this.items.get(OUTPUT_SLOT);
        }

        return ItemStack.EMPTY;
    }

    public int getSlotForItem(Item item) {
        if (!this.hasStackOfType(item)) {
            return -1;
        }

        for (int slot : this.getAvailableSlots(Direction.UP)) {
            ItemStack stack = this.getStack(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() == item) {
                return slot;
            }
        }

        return -1;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot < 0) {
            return ItemStack.EMPTY;
        }

        if (slot < AVAILABLE_INVENTORY) {
            ItemStack result = Inventories.splitStack(this.items, slot, amount);
            if (!result.isEmpty()) {
                this.markDirty();
            }
        } else if (slot == OUTPUT_SLOT
                && this.getPortions() >= amount
                && this.getStack(BOWL_SLOT).getCount() >= amount) {
            ItemStack stew = this.makeStew();
            this.decrementPortions(amount);
            this.getStack(BOWL_SLOT).decrement(amount);
            if (amount > 1) {
                int count = amount - 1;
                stew.increment(count);
            }
            this.markDirty();
            return stew;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return this.removeStack(slot, this.getStack(slot).getCount());
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot < 0) {
            return;
        }

        if (slot < AVAILABLE_INVENTORY
                && this.canAddFood(stack)) {
            this.items.set(slot, stack);
            //this.addFood(stack);
        } else if (slot == BOWL_SLOT
                && stack.getItem() == Items.BOWL
                && this.getStack(BOWL_SLOT).getCount() < this.getStack(BOWL_SLOT).getMaxCount()) {
            this.items.set(BOWL_SLOT, stack);
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.items.clear();
        this.potionEffects.clear();
        this.markDirty();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side != Direction.DOWN) {
            return IntStream.range(0, BOWL_SLOT + 1).toArray();
        }

        return new int[]{OUTPUT_SLOT};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        boolean result = dir != Direction.DOWN && slot != OUTPUT_SLOT;

        if (result) {
            if (slot < AVAILABLE_INVENTORY
                    && this.getPortions() + stack.getCount() <= ConfigManager.maxPortionsPerPot()) {
                result = this.canAddFood(stack);
            } else if (slot == BOWL_SLOT
                    && stack.getItem() == Items.BOWL) {
                ItemStack bowls = this.items.get(BOWL_SLOT);
                result = bowls.getCount() < bowls.getMaxCount();
            } else {
                result = false;
            }
        }

        return result;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return dir == Direction.DOWN && slot == OUTPUT_SLOT && this.getPortions() > 0;
    }

    protected int filledSlotCount() {
        int count = 0;
        for (int i = 0; i < AVAILABLE_INVENTORY; i++) {
            if (this.getStack(i) != ItemStack.EMPTY) {
                count++;
            }
        }

        return count;
    }

    protected int emptySlotCount() {
        int count = 0;
        for (int i = 0; i < AVAILABLE_INVENTORY; i++) {
            if (this.getStack(i) == ItemStack.EMPTY) {
                count++;
            }
        }

        return count;
    }

    protected int getFirstEmptySlot() {
        for (int i = 0; i < AVAILABLE_INVENTORY; i++) {
            if (this.getStack(i) == ItemStack.EMPTY) {
                return i;
            }
        }

        return -1;
    }

    protected boolean hasEmptySlot() {
        for (int i = 0; i < AVAILABLE_INVENTORY; i++) {
            if (this.getStack(i) == ItemStack.EMPTY) {
                return true;
            }
        }

        return false;
    }

    protected boolean hasStackOfType(Item item) {
        for (int i = 0; i < AVAILABLE_INVENTORY; i++) {
            if (this.getStack(i).getItem() == item) {
                return true;
            }
        }

        return false;
    }

    protected ItemStack getStackOfType(Item item) {
        for (int i = 0; i < AVAILABLE_INVENTORY; i++) {
            if (this.getStack(i).getItem() == item) {
                return this.getStack(i);
            }
        }

        return ItemStack.EMPTY;
    }
}
