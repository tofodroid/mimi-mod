package io.github.tofodroid.mods.mimi.common.inventory;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class SwitchboardInventoryStackHandler extends ItemStackHandler {
    public SwitchboardInventoryStackHandler(Integer stackSize) {
        super(stackSize);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return slot == 0 && ModItems.SWITCHBOARD.equals(stack.getItem());
    }
}
