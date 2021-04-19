package io.github.tofodroid.mods.mimi.common.container;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class FilteredSlot extends Slot {
    private final List<Item> allowedItems;
    private final Integer maxSize;
    private final Predicate<ItemStack> validStack;


    public FilteredSlot (IInventory inventory, int slot, int x, int y, Integer maxSize) {
        this(inventory, slot, x, y, null, maxSize);
    }

    public FilteredSlot (IInventory inventory, int slot, int x, int y, List<Item> allowedItems) {
        this(inventory, slot, x, y, allowedItems, inventory.getInventoryStackLimit());
    }

    public FilteredSlot (IInventory inventory, int slot, int x, int y, List<Item> allowedItems, Integer maxSize) {
        this(inventory, slot, x, y, allowedItems, inventory.getInventoryStackLimit(), stack -> {return true;});
    }

    public FilteredSlot (IInventory inventory, int slot, int x, int y, List<Item> allowedItems, Integer maxSize, Predicate<ItemStack> validStack) {
        super(inventory, slot, x, y);
        this.allowedItems = allowedItems;
        this.maxSize = maxSize;
        this.validStack = validStack;
    }

    @Override
    public boolean isItemValid (@Nonnull ItemStack stack) {
        if(allowedItems != null) {
            return allowedItems.contains(stack.getItem()) && validStack.test(stack);
        }

        return true;
    }

    @Override
    public int getSlotStackLimit() {
        return this.maxSize != null && this.maxSize > 0 ? this.maxSize : 0;
    }
}