package com.github.nosrick.crockpot.util;

import net.minecraft.block.SuspiciousStewIngredient;
import net.minecraft.entity.effect.StatusEffect;
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
import java.util.Optional;
import java.util.function.Consumer;

public abstract class NbtListUtil {

    public static NbtList nbtListFromStatusEffectInstances(Collection<StatusEffectInstance> collection) {
        NbtList list = new NbtList();

        if (collection == null || collection.isEmpty()) {
            return list;
        }

        for (StatusEffectInstance effectInstance : collection) {
            list.add(effectInstance.writeNbt(new NbtCompound()));
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

        NbtCompound nbtCompound = stew.getNbt();
        if (nbtCompound != null && nbtCompound.contains(SuspiciousStewItem.EFFECTS_KEY, NbtElement.LIST_TYPE)) {
            NbtList nbtList = nbtCompound.getList("Effects", NbtElement.COMPOUND_TYPE);

            for(int i = 0; i < nbtList.size(); i++) {
                int j = 160;
                NbtCompound nbtCompound2 = nbtList.getCompound(i);
                if (nbtCompound2.contains("EffectDuration", NbtElement.INT_TYPE)) {
                    j = nbtCompound2.getInt("EffectDuration");
                }

                StatusEffect statusEffect = StatusEffect.byRawId(nbtCompound2.getInt("EffectId"));
                if (statusEffect != null) {
                    effectInstances.add(new StatusEffectInstance(statusEffect, j));
                }
            }

            /*var tempList = SuspiciousStewIngredient.StewEffect.LIST_CODEC
                    .parse(NbtOps.INSTANCE,
                            nbtCompound.getList(SuspiciousStewItem.EFFECTS_KEY, NbtElement.COMPOUND_TYPE))
                    .result();

            if (tempList.isPresent()) {
                var effectsList = tempList.get();
                for (var effect : effectsList) {
                    effectInstances.add(effect.createStatusEffectInstance());
                }
            }

             */
        }

        return effectInstances;
    }
}
