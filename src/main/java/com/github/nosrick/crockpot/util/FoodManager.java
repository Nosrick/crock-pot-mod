package com.github.nosrick.crockpot.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FoodManager {

    protected Map<UUID, FoodComponent> playerMap;

    public FoodManager() {
        this.playerMap = new HashMap<>();
    }

    public void PlayerBeginsEating(PlayerEntity player, FoodComponent foodComponent) {
        this.playerMap.put(player.getUuid(), foodComponent);
    }

    public void PlayerFinishesEating(PlayerEntity player) {
        if(this.playerMap.containsKey(player.getUuid())) {
            this.playerMap.remove(player.getUuid());
        }
    }

    public FoodComponent GetFoodForPlayer(PlayerEntity player) {
        if(this.playerMap.containsKey(player.getUuid())) {
            return this.playerMap.get(player.getUuid());
        }

        return new FoodComponent.Builder().build();
    }
}
