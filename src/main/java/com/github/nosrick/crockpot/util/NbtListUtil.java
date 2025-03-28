package com.github.nosrick.crockpot.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class NbtListUtil {

    /*
    public static NbtList nbtListFromStatusEffectInstances(Collection<StatusEffectInstance> collection) {
        NbtList list = new NbtList();

        if (collection == null || collection.isEmpty()) {
            return list;
        }

        for (StatusEffectInstance effectInstance : collection) {
            list.add(effectInstance.);
        }

        return list;
    }

    public static Collection<StatusEffectInstance> effectInstanceCollectionFromNbtList(NbtList nbtList) {
        if (nbtList == null) {
            return new ArrayList<>();
        }

        ArrayList<StatusEffectInstance> effectInstances = new ArrayList<>();

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i).get();
            StatusEffectInstance effectInstance = StatusEffectInstance.fromNbt(nbtCompound);
            if (effectInstance == null) {
                continue;
            }

            effectInstances.add(effectInstance);
        }

        return effectInstances;
    }

     */

    public static List<StatusEffectInstance> getEffectsFromSuspiciousStew(ItemStack stew) {
        if (stew.getOrDefault(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffectsComponent.DEFAULT) == null) {
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
