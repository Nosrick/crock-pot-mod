package com.github.nosrick.crockpot.util;

import net.minecraft.block.SuspiciousStewIngredient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class NbtListUtil {

    public static NbtList nbtListFromStatusEffectInstances(Collection<StatusEffectInstance> collection) {
        NbtList list = new NbtList();

        if (collection == null || collection.isEmpty()) {
            return list;
        }

        for (StatusEffectInstance effectInstance : collection) {
            list.add(effectInstance.writeNbt());
        }

        return list;
    }

    public static Collection<StatusEffectInstance> effectInstanceCollectionFromNbtList(NbtList nbtList) {
        if (nbtList == null) {
            return new ArrayList<>();
        }

        ArrayList<StatusEffectInstance> effectInstances = new ArrayList<>();

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            StatusEffectInstance effectInstance = StatusEffectInstance.fromNbt(nbtCompound);
            if (effectInstance == null) {
                continue;
            }

            effectInstances.add(effectInstance);
        }

        return effectInstances;
    }

    public static List<StatusEffectInstance> getEffectsFromSuspiciousStew(ItemStack stew) {
        if (!(stew.getItem() instanceof SuspiciousStewItem)) {
            return new ArrayList<>();
        }

        ArrayList<StatusEffectInstance> effectInstances = new ArrayList<>();

        var stewEffects = stew.getOrDefault(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffectsComponent.DEFAULT);
        if(!stewEffects.effects().isEmpty())
        {
            for(var effect : stewEffects.effects()){
                effectInstances.add(effect.createStatusEffectInstance());
            }
        }

        return effectInstances;
    }
}
