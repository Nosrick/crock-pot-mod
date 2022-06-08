package com.github.nosrick.crockpot.compat.wthit;

import com.github.nosrick.crockpot.block.CrockPotBlock;
import com.github.nosrick.crockpot.blockentity.CrockPotBlockEntity;
import mcp.mobius.waila.api.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

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
            Inventories.writeNbt(data, crockPotBlock.getContents());
        }
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof CrockPotBlockEntity crockPotBlockEntity) {
            NbtCompound nbt = accessor.getServerData();
            int portions = nbt.getInt(CrockPotBlockEntity.PORTIONS_NBT);
            int bonusLevels = nbt.getInt(CrockPotBlockEntity.BONUS_LEVELS);
            DefaultedList<ItemStack> items = DefaultedList.ofSize(8, ItemStack.EMPTY);
            Inventories.readNbt(nbt, items);

            List<Item> contents = items.stream().takeWhile(itemStack -> !itemStack.isEmpty()).map(ItemStack::getItem).toList();
            tooltip.addLine(Text.translatable("tooltip.crockpot.portions", portions));
            tooltip.addLine(Text.translatable("tooltip.crockpot.bonus_levels", bonusLevels));

            tooltip.addLine(Text.translatable("values.crockpot.redstone_output.type",
                    crockPotBlockEntity.getRedstoneOutputType().localName));

            if (!contents.isEmpty()) {
                tooltip.addLine(new ItemRenderTooltipComponent(contents));
            }
        }
    }
}
