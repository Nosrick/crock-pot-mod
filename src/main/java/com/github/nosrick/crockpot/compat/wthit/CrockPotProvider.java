package com.github.nosrick.crockpot.compat.wthit;

import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.github.nosrick.crockpot.config.ConfigManager;
import mcp.mobius.waila.api.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public class CrockPotProvider implements IBlockComponentProvider, IDataProvider<CrockPotBlockEntity> {

    public static CrockPotProvider INSTANCE = new CrockPotProvider();

    private CrockPotProvider() {}

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof CrockPotBlockEntity) {

            NbtCompound nbt = accessor.getData().raw();
            int portions = nbt.getInt(CrockPotBlockEntity.PORTIONS_NBT);
            int bonusLevels = nbt.getInt(CrockPotBlockEntity.BONUS_LEVELS);

            tooltip.addLine(Text.translatable("tooltip.crockpot.portions", portions));
            tooltip.addLine(Text.translatable("tooltip.crockpot.bonus_levels", bonusLevels));
            tooltip.addLine(Text.translatable("values.crockpot.redstone_output.type",
                    nbt.getString(CrockPotBlockEntity.REDSTONE_OUTPUT)));

            if (nbt.contains(CrockPotBlockEntity.EFFECTS_NBT)) {
                NbtList effectsList = (NbtList) nbt.get(CrockPotBlockEntity.EFFECTS_NBT);
                for (int i = 0; i < effectsList.size(); i++) {
                    NbtCompound effectNbt = effectsList.getCompound(i);
                    StatusEffectInstance effect = StatusEffectInstance.fromNbt(effectNbt);
                    tooltip.addLine(Text.translatable("tooltip.crockpot.effects"));

                    if (!ConfigManager.hideStewEffects()) {
                        tooltip.addLine(Text.translatable(effect.getTranslationKey())
                                .append(" " + (effect.getAmplifier() + 1))
                                .append(" - " + (effect.getDuration() / 20))
                                .append(Text.translatable("tooltip.crockpot.seconds")));
                    } else {
                        if (ConfigManager.useObfuscatedText()) {
                            tooltip.addLine(Text.literal("THIS DOES STUFF").setStyle(Style.EMPTY.withObfuscated(true)));
                        } else {
                            tooltip.addLine(Text.translatable("tooltip.crockpot.hidden_effects"));
                        }
                    }
                }
            }

            DefaultedList<ItemStack> items = DefaultedList.ofSize(ConfigManager.ingredientSlots(), ItemStack.EMPTY);
            Inventories.readNbt(nbt, items);
            if (!items.isEmpty()) {
                tooltip.addLine(new ItemRenderTooltipComponent(
                        items.stream()
                                .takeWhile(itemStack -> !itemStack.isEmpty())
                                .map(ItemStack::getItem)
                                .toList()));
            }
        }
    }

    @Override
    public void appendData(IDataWriter data, IServerAccessor<CrockPotBlockEntity> accessor, IPluginConfig config) {
        CrockPotBlockEntity crockPotBlockEntity = accessor.getTarget();
        NbtCompound nbt = data.raw();

        nbt.putInt(CrockPotBlockEntity.PORTIONS_NBT, crockPotBlockEntity.getPortions());
        nbt.putInt(CrockPotBlockEntity.BONUS_LEVELS, crockPotBlockEntity.getBonusLevels());
        nbt.putString(CrockPotBlockEntity.REDSTONE_OUTPUT, crockPotBlockEntity.getRedstoneOutputType().localName.getString());

        if (!crockPotBlockEntity.getPotionEffects().isEmpty()) {
            NbtList effectList = new NbtList();
            for (StatusEffectInstance effectInstance : crockPotBlockEntity.getPotionEffects()) {
                effectList.add(effectInstance.writeNbt(new NbtCompound()));
            }
            nbt.put(CrockPotBlockEntity.EFFECTS_NBT, effectList);
        }

        Inventories.writeNbt(nbt, crockPotBlockEntity.getContents());
    }
}
