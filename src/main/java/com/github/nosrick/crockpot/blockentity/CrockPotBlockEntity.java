package com.github.nosrick.crockpot.blockentity;

import com.github.nosrick.crockpot.CrockPotMod;
import com.github.nosrick.crockpot.block.CrockPotBlock;
import com.github.nosrick.crockpot.config.ConfigManager;
import com.github.nosrick.crockpot.item.StewItem;
import com.github.nosrick.crockpot.registry.BlockEntityTypesRegistry;
import com.github.nosrick.crockpot.registry.CrockPotSoundRegistry;
import com.github.nosrick.crockpot.registry.ItemRegistry;
import com.github.nosrick.crockpot.tag.Tags;
import com.github.nosrick.crockpot.util.NbtListUtil;
import com.github.nosrick.crockpot.util.UUIDUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

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

    public static final Identifier PACKET_ID = CrockPotMod.createIdentifier("block.entity.crockpot.update");

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
    protected List<StatusEffectInstance> dilutedPotionEffects;

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
        this(BlockEntityTypesRegistry.CROCK_POT, pos, state);
    }

    protected CrockPotBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.potionEffects = new ArrayList<>();
        this.dilutedPotionEffects = new ArrayList<>();
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.name = nbt.getString(NAME_NBT);
        this.hunger = nbt.getInt(HUNGER_NBT);
        this.saturation = nbt.getFloat(SATURATION_NBT);
        this.portions = nbt.getInt(PORTIONS_NBT);

        this.bonusLevels = nbt.getInt(BONUS_LEVELS);
        this.boilingTime = nbt.getLong(BOILING_TIME);
        this.lastTime = nbt.getLong(LAST_TIME);

        this.curseLevel = nbt.getInt(CURSE_LEVEL);

        Inventories.readNbt(nbt, this.items, registryLookup);

        this.potionEffects.clear();

        if (ConfigManager.canLockPots()) {
            this.setOwner(nbt.getUuid(OWNER_NBT));
        }

        if (nbt.contains(EFFECTS_NBT)) {
            NbtList nbtList = (NbtList) nbt.get(EFFECTS_NBT);
            this.potionEffects = new ArrayList<>(NbtListUtil.effectInstanceCollectionFromNbtList(nbtList));
            this.dilutePotionEffects();
        }

        this.setRedstoneOutputType(RedstoneOutputType.valueOf(nbt.getString(REDSTONE_OUTPUT)));

        this.markDirty();

        super.readNbt(nbt, registryLookup);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
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

        Inventories.writeNbt(nbt, this.items, registryLookup);

        if (!this.potionEffects.isEmpty()) {
            nbt.put(EFFECTS_NBT, NbtListUtil.nbtListFromStatusEffectInstances(this.potionEffects));
        }

        super.writeNbt(nbt, registryLookup);
    }

    protected void recalculateFoodValues() {
        int portions = this.getPortions();

        if (portions == 0) {
            this.hunger = 0;
            this.saturation = 0;
            return;
        }

        int combinedHunger = 0;

        for (ItemStack itemStack : this.getContents()) {
            Item item = itemStack.getItem();
            FoodComponent foodComponent = item.getComponents().get(DataComponentTypes.FOOD);

            if (foodComponent == null) {
                continue;
            }

            combinedHunger += foodComponent.nutrition();
        }

        this.hunger = combinedHunger;
    }

    protected void dilutePotionEffects() {
        this.dilutedPotionEffects = new ArrayList<>();

        float dilution = Math.max(1f, this.portions * ConfigManager.dilutionModifier());

        for (StatusEffectInstance effect : this.potionEffects) {
            this.dilutedPotionEffects.add(
                    new StatusEffectInstance(
                            effect.getEffectType(),
                            (int) (effect.getDuration() / dilution),
                            effect.getAmplifier()));
        }
    }

    public boolean canAddFood(ItemStack food) {
        if (!this.canAddPotion(food) && !food.getComponents().contains(DataComponentTypes.FOOD)) {
            return false;
        }

        if (!this.canBoil()) {
            return false;
        }

        if (!this.hasEmptySlot() && !this.hasStackOfType(food.getItem())) {
            return false;
        }

        return this.getPortions() < ConfigManager.maxPortionsPerPot();
    }

    public boolean canAddPotion(ItemStack potion) {
        Item potionItem = potion.getItem();

        if (!(potionItem instanceof PotionItem)) {
            return false;
        }

        if (!ConfigManager.canAddPotions()) {
            return false;
        }

        return this.potionEffects.size() < ConfigManager.effectPerPot();
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

        FoodComponent foodComponent = foodItem.getComponents().get(DataComponentTypes.FOOD);
        if (foodComponent == null && foodItem.getComponents().contains(DataComponentTypes.FOOD)) {
            return false;
        }

        this.boilingTime = 0;
        this.bonusLevels = 0;

        if (!this.hasStackOfType(foodItem)) {
            this.items.set(this.getFirstEmptySlot(), new ItemStack(foodItem));
            this.recalculateFoodValues();
        }

        if (ConfigManager.canAddPotions()) {
            if (food.getItem() instanceof PotionItem) {
                Potion potion = food.getComponents().getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT)
                        .potion()
                        .get()
                        .value();
                if (this.potionEffects.size() < ConfigManager.effectPerPot()) {
                    this.addStatusEffects(potion.getEffects());

                    this.markDirty();
                    this.updateNearby();

                    return true;
                }

                return false;
            } else if (foodItem.getComponents().contains(DataComponentTypes.POTION_CONTENTS)) {
                var effects = food.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
                if (effects.hasEffects()) {
                    this.addStatusEffects(effects.customEffects());
                }
            }

            if (food.getOrDefault(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffectsComponent.DEFAULT) != null) {
                List<StatusEffectInstance> effectsFromSuspiciousStew = NbtListUtil.getEffectsFromSuspiciousStew(food);
                this.addStatusEffects(effectsFromSuspiciousStew);
            }
        }

        this.dilutePotionEffects();
        this.recalculateStews();
        this.markDirty();

        if (this.hasWorld()) {
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
                    this.potionEffects.add(effectInstance);
                    countAdded++;
                } else {
                    var oldEffectOptional = this.potionEffects.stream().filter(effect ->
                            effect.getEffectType() == effectInstance.getEffectType()).findFirst();

                    if (oldEffectOptional.isPresent()) {
                        StatusEffectInstance oldEffect = oldEffectOptional.get();
                        if (this.shouldReplaceEffect(oldEffect, effectInstance)) {
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

        if (countAdded > 0) {
            this.dilutePotionEffects();
        }
    }

    protected boolean shouldReplaceEffect(StatusEffectInstance oldEffect, StatusEffectInstance newEffect) {
        return newEffect.getDuration() > oldEffect.getDuration()
                || newEffect.getAmplifier() > oldEffect.getAmplifier();
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
            ItemStack stew = new ItemStack(ItemRegistry.STEW_ITEM);

            if (!ConfigManager.useCursedStew() || this.curseLevel < ConfigManager.stewMinNegativeLevelsEffect()) {

                int foodItems = this.getFoodStackCount();

                if(foodItems == 0)
                {
                    CrockPotMod.LOGGER.error("APPARENTLY NO FOOD ITEMS");
                    return stew;
                }

                int hungerToGo = (this.hunger + (int) (this.bonusLevels * ConfigManager.bonusHungerMagnitude())) / foodItems;
                this.saturation = 0.7f + (ConfigManager.bonusSaturationMagnitude() * this.getBoilingIntensity());

                StewItem.setHunger(stew, hungerToGo);
                StewItem.setSaturation(stew, this.saturation);
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
                    boolean dilute = ConfigManager.diluteEffects();
                    if (dilute && this.potionEffects.size() != this.dilutedPotionEffects.size()) {
                        this.dilutePotionEffects();
                    }
                    for (int i = 0; i < max; i++) {
                        StatusEffectInstance effect = dilute
                                ? this.dilutedPotionEffects.get(i)
                                : this.potionEffects.get(i);
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
                                    Math.min(this.curseLevel - ConfigManager.stewMinNegativeLevelsEffect() + 1, 5)));
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
                if (this.items.stream().anyMatch(itemStack -> itemStack.getItem() instanceof PotionItem)) {
                    statusText.append(Text.translatable("item.crockpot.stew.alchemical"));
                    statusText.append(" ");
                }

                if (this.filledSlotCount() < 4) {
                    List<Text> list = new ArrayList<>();
                    for (int i = 0; i < contents.size(); i++) {
                        ItemStack content = contents.get(i);

                        if (content.getItem() instanceof PotionItem) {
                            continue;
                        }

                        Text text = Text.translatable(
                                content.getItem() instanceof StewItem
                                        ? "item.crockpot.stew_name"
                                        : content.getItem().getTranslationKey());

                        list.add(text);
                        if (i < contents.size() - 2) {
                            list.add(Text.of(", "));
                        } else if (i < contents.size() - 1) {
                            list.add(Text.of(" & "));
                        }
                    }

                    MutableText total = Text.empty();
                    list.forEach(total::append);

                    if (total.getString().length() > ConfigManager.maxStewNameLength()) {
                        statusText.append(Text.translatable("item.crockpot.stew.mixed"));
                    } else {
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
            stew.set(DataComponentTypes.CUSTOM_NAME, statusText);

            return stew;
        }

        return ItemStack.EMPTY;
    }

    protected void addEffectToStew(ItemStack stew) {
        int duration = ConfigManager.basePositiveDuration() * 20 * this.bonusLevels;
        duration = ConfigManager.cappedPositiveDuration()
                ? Math.min(ConfigManager.maxPositiveDuration(), duration)
                : duration;
        StewItem.addStatusEffect(
                stew,
                new StatusEffectInstance(
                        StatusEffects.SATURATION,
                        duration,
                        Math.min(this.bonusLevels - ConfigManager.stewMinPositiveLevelsEffect() + 1, ConfigManager.maxBonusLevels())));
    }

    public DefaultedList<ItemStack> getContents() {
        DefaultedList<ItemStack> contents = DefaultedList.ofSize(ConfigManager.ingredientSlots());
        contents.addAll(this.items.stream().filter(itemStack -> !itemStack.isEmpty() && itemStack.getItem() != ItemRegistry.STEW_ITEM).toList());
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
            if (this.world == null || this.world.isClient) {
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

        if (this.world == null || this.world.isClient) {
            return;
        }

        if (this.redstoneOutputType == RedstoneOutputType.NONE) {
            this.world.setBlockState(this.pos, this.getCachedState().with(CrockPotBlock.EMITS_SIGNAL, false));
        } else {
            this.world.setBlockState(this.pos, this.getCachedState().with(CrockPotBlock.EMITS_SIGNAL, true));
        }

        this.updateNearby();
    }

    protected void updateNearby() {
        if (this.world == null || this.world.isClient) {
            return;
        }
        world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 0);
        world.updateNeighborsAlways(this.pos, this.getCachedState().getBlock());

        /*
        for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) this.world, this.pos)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(this.pos);
            buf.writeNbt(this.createNbt(world.getRegistryManager()));
            ServerPlayNetworking.send(player, new Payload);
        }
         */
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
        if (ConfigManager.diluteEffects()) {
            return new ArrayList<>(this.dilutedPotionEffects);
        }

        return new ArrayList<>(this.potionEffects);
    }

    public int getBonusLevels() {
        return this.bonusLevels;
    }

    public void decrementPortions(int amount) {
        this.portions = Math.max(0, this.portions - amount);

        this.getStack(OUTPUT_SLOT).decrement(amount);
        this.markDirty();

        if (this.portions == 0) {
            this.flush();
        }
    }

    protected void cookRawFood() {
        if (world == null) {
            return;
        }

        if (world instanceof ServerWorld serverWorld) {
            for (ItemStack stack : this.getContents()) {

                ServerRecipeManager.MatchGetter<SingleStackRecipeInput, CampfireCookingRecipe> matchGetter = ServerRecipeManager.createCachedMatchGetter(RecipeType.CAMPFIRE_COOKING);

                var singleStackRecipeInput = new SingleStackRecipeInput(stack);

                var cookedStack = matchGetter
                        .getFirstMatch(singleStackRecipeInput, serverWorld)
                        .map(recipe -> (recipe.value()).craft(singleStackRecipeInput, world.getRegistryManager()))
                        .orElse(stack);

                if(cookedStack == stack)
                {
                    continue;
                }

                int rawCount = stack.getCount();
                int rawSlot = this.getSlotForItem(stack.getItem());

                var possibleStatus = new ArrayList<>(
                        StreamSupport.stream(
                                        stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT)
                                                .getEffects()
                                                .spliterator(),
                                        false)
                                .toList());

                var foodComponent = stack.get(DataComponentTypes.FOOD);
                if (foodComponent != null) {
                    var foodEffects = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT)
                            .customEffects();
                    possibleStatus.addAll(foodEffects);
                }
                for (StatusEffectInstance effect : possibleStatus) {
                    var removeEffects = this.potionEffects.stream()
                            .filter(status ->
                                    status.getEffectType().equals(effect.getEffectType()))
                            .toList();

                    this.potionEffects.removeAll(removeEffects);
                }

                Item cookedItem = cookedStack.getItem();

                int cookedSlot = this.getSlotForItem(cookedItem);

                if (cookedSlot < 0) {
                    this.removeStack(rawSlot);
                    this.setStack(rawSlot, new ItemStack(cookedItem, 1));
                }

                var cookedFoodComponent = cookedItem.getComponents().get(DataComponentTypes.FOOD);
                var cookedFoodEffects = cookedItem.getComponents().getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);

                if (cookedFoodEffects.hasEffects()) {
                    this.addStatusEffects(StreamSupport.stream(cookedFoodEffects.getEffects().spliterator(), false).collect(Collectors.toList()));
                }

                this.recalculateFoodValues();
                this.recalculateStews();
            }
        }
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        if (world == null) {
            return;
        }

        CrockPotBlockEntity crockPotBlockEntity = (CrockPotBlockEntity) blockEntity;

        if (world instanceof ServerWorld serverWorld) {
            serverTick(serverWorld, crockPotBlockEntity);
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

    protected static void serverTick(ServerWorld world, CrockPotBlockEntity blockEntity) {

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

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registry) {
        return this.createNbt(registry);
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public int getFoodStackCount() {
        int foodItems = 0;

        for (ItemStack itemStack : this.getContents()) {
            var foodComponent = itemStack.get(DataComponentTypes.FOOD);
            if (foodComponent != null) {
                foodItems++;
            } else if (ConfigManager.potionsCountAsFood()
                    && itemStack.getItem() instanceof PotionItem) {
                foodItems++;
            }
        }

        return foodItems;
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
