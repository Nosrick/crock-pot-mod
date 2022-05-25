package com.github.nosrick.crockpot.compat.early_game_buckets;

import dev.satyrn.early_buckets.item.Bucket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class EarlyGameBucketsCompat {

    protected static boolean Loaded = false;

    public static boolean isLoaded()
    {
        return Loaded;
    }

    public static void markAsLoaded(){
        Loaded = true;
    }

    public static boolean isEarlyGameBucket(ItemStack probableBucket)
    {
        return probableBucket.getItem() instanceof Bucket;
    }

    public static ItemStack getEmptyItem(ItemStack probableBucket, PlayerEntity player)
    {
        if(probableBucket.getItem() instanceof Bucket bucket)
        {
            return bucket.getEmptyItemStack(probableBucket, player );
        }

        return null;
    }
}
