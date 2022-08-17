package com.github.nosrick.crockpot.compat.wthit;

import com.github.nosrick.crockpot.block.CrockPotBlock;
import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import com.github.nosrick.crockpot.config.ConfigManager;
import mcp.mobius.waila.api.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.*;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public class WthitPlugin implements IWailaPlugin, IBlockComponentProvider, IServerDataProvider<BlockEntity> {
    @Override
    public void register(IRegistrar registrar) {
        registrar.addBlockData(this, CrockPotBlockEntity.class);
        registrar.addComponent(this, TooltipPosition.BODY, CrockPotBlock.class);
    }

    @Override
    public void appendServerData(NbtCompound data, IServerAccessor<BlockEntity> accessor, IPluginConfig config) {
        if (accessor.getTarget() instanceof CrockPotBlockEntity crockPotBlock) {
            data.putInt(CrockPotBlockEntity.PORTIONS_NBT, crockPotBlock.getPortions());
            data.putInt(CrockPotBlockEntity.BONUS_LEVELS, crockPotBlock.getBonusLevels());

            List<StatusEffectInstance> effectInstances = crockPotBlock.getPotionEffects();
            if (!effectInstances.isEmpty()) {
                NbtList effectsList = new NbtList();
                for (StatusEffectInstance effectInstance : effectInstances) {
                    effectsList.add(effectInstance.writeNbt(new NbtCompound()));
                }
                data.put(CrockPotBlockEntity.EFFECTS_NBT, effectsList);
            }
            Inventories.writeNbt(data, crockPotBlock.getContents());
        }
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof CrockPotBlockEntity crockPotBlockEntity) {
            NbtCompound nbt = accessor.getServerData();
            int portions = nbt.getInt(CrockPotBlockEntity.PORTIONS_NBT);
            int bonusLevels = nbt.getInt(CrockPotBlockEntity.BONUS_LEVELS);

            List<MutableText> effectsNames = new ArrayList<>();
            if (nbt.contains(CrockPotBlockEntity.EFFECTS_NBT)) {
                NbtList effectsList = (NbtList) nbt.get(CrockPotBlockEntity.EFFECTS_NBT);
                for (int i = 0; i < effectsList.size(); i++) {
                    NbtCompound effectNbt = effectsList.getCompound(i);
                    StatusEffectInstance effect = StatusEffectInstance.fromNbt(effectNbt);
                    effectsNames.add(new TranslatableText(effect.getTranslationKey())
                            .append(" " + (effect.getAmplifier() + 1))
                            .append(" - " + (effect.getDuration() / 20))
                            .append(new TranslatableText("tooltip.crockpot.seconds")));
                }
            }

            DefaultedList<ItemStack> items = DefaultedList.ofSize(8, ItemStack.EMPTY);
            Inventories.readNbt(nbt, items);

            List<Item> contents = items.stream().takeWhile(itemStack -> !itemStack.isEmpty()).map(ItemStack::getItem).toList();
            tooltip.addLine(new TranslatableText("tooltip.crockpot.portions", portions));
            tooltip.addLine(new TranslatableText("tooltip.crockpot.bonus_levels", bonusLevels));
            if (!effectsNames.isEmpty()) {
                tooltip.addLine(new TranslatableText("tooltip.crockpot.effects"));
                if(!ConfigManager.hideStewEffects()) {
                    for (MutableText text : effectsNames) {
                        tooltip.addLine(text);
                    }
                }
                else {
                    if(ConfigManager.useObfuscatedText()) {
                        tooltip.addLine(new LiteralText("THIS DOES STUFF").setStyle(Style.EMPTY.withObfuscated(true)));
                    }
                    else {
                        tooltip.addLine(new TranslatableText("tooltip.crockpot.hidden_effects"));
                    }
                }
            }

            tooltip.addLine(new TranslatableText("values.crockpot.redstone_output.type",
                    crockPotBlockEntity.getRedstoneOutputType().localName));

            if (!contents.isEmpty()) {
                tooltip.addLine(new ItemRenderTooltipComponent(contents));
            }
        }
    }
}
