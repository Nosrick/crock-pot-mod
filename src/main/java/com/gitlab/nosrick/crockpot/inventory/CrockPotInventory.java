package com.gitlab.nosrick.crockpot.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.Collection;

public interface CrockPotInventory extends Inventory {

    /**
     * Retrieves the item list of this inventory.
     * Must return the same instance every time it's called.
     */
    DefaultedList<ItemStack> getItems();

    /**
     * Creates an inventory from the item list.
     */
    static CrockPotInventory of(DefaultedList<ItemStack> items) {
        return () -> items;
    }

    /**
     * Creates a new inventory with the specified size.
     */
    static CrockPotInventory ofSize(int size) {
        return of(DefaultedList.ofSize(size, ItemStack.EMPTY));
    }

    /**
     * Returns the inventory size.
     */
    @Override
    default int size() {
        return getItems().size() - 1;
    }

    /**
     * Checks if the inventory is empty.
     * @return true if this inventory has only empty stacks, false otherwise.
     */
    @Override
    default boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the item in the slot.
     */
    @Override
    default ItemStack getStack(int slot) {
        return getItems().get(slot);
    }

    default boolean hasEmptySlot() {
        for(int i = 0; i < size(); i++){
            if(getStack(i) == ItemStack.EMPTY) {
                return true;
            }
        }

        return false;
    }

    default int getFirstEmptySlot() {
        for(int i = 0; i < size(); i++) {
            if(getStack(i) == ItemStack.EMPTY) {
                return i;
            }
        }

        return -1;
    }

    default int emptySlotCount() {
        int count = 0;
        for(int i = 0; i < size(); i++) {
            if(getStack(i) == ItemStack.EMPTY) {
                count++;
            }
        }

        return count;
    }

    default int filledSlotCount() {
        int count = 0;
        for(int i = 0; i < size(); i++) {
            if(getStack(i) != ItemStack.EMPTY) {
                count++;
            }
        }

        return count;
    }

    /**
     * Removes items from an inventory slot.
     * @param slot  The slot to remove from.
     * @param count How many items to remove. If there are less items in the slot than what are requested,
     *              takes all items in that slot.
     */
    @Override
    default ItemStack removeStack(int slot, int count) {
        ItemStack result = Inventories.splitStack(getItems(), slot, count);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    default boolean hasStackOfType(Item item) {
        for(int i = 0; i < size(); i++) {
            if(getStack(i).getItem() == item) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes all items from an inventory slot.
     * @param slot The slot to remove from.
     */
    @Override
    default ItemStack removeStack(int slot) {
        return Inventories.removeStack(getItems(), slot);
    }

    @Override
    default void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
    }

    /**
     * Clears the inventory.
     */
    @Override
    default void clear() {
        getItems().clear();
    }

    /**
     * Marks the state as dirty.
     * Must be called after changes in the inventory, so that the game can properly save
     * the inventory contents and notify neighboring blocks of inventory changes.
     */
    @Override
    default void markDirty() {
        // Override if you want behavior.
    }

    /**
     * @return true if the player can use the inventory, false otherwise.
     */
    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return true;
    }
}