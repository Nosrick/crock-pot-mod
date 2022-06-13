package com.github.nosrick.crockpot.util;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.Collection;

public abstract class NbtListUtil {

    public static NbtList nbtListFromStatusEffectInstances(Collection<StatusEffectInstance> collection) {
        NbtList list = new NbtList();

        if(collection == null || collection.isEmpty()) {
            return list;
        }

        for(StatusEffectInstance effectInstance : collection) {
            list.add(effectInstance.writeNbt(new NbtCompound()));
        }

        return list;
    }

    public static Collection<StatusEffectInstance> effectInstanceCollectionFromNbtList(NbtList nbtList) {
        if(nbtList == null) {
            return new ArrayList<>();
        }

        ArrayList<StatusEffectInstance> effectInstances = new ArrayList<>();

        for(int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            StatusEffectInstance effectInstance = StatusEffectInstance.fromNbt(nbtCompound);
            if(effectInstance == null) {
                continue;
            }

            effectInstances.add(effectInstance);
        }

        return effectInstances;
    }
}
