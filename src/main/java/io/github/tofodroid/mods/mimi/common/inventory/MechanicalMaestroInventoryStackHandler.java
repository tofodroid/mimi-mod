package io.github.tofodroid.mods.mimi.common.inventory;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentBlock;
import io.github.tofodroid.mods.mimi.common.item.ModItems;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class MechanicalMaestroInventoryStackHandler extends ItemStackHandler {
    public MechanicalMaestroInventoryStackHandler(Integer stackSize) {
        super(stackSize);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return (slot == 0 && ModItems.SWITCHBOARD.equals(stack.getItem())) || (slot == 1 && (stack.getItem() instanceof ItemInstrument || stack.getItem() instanceof ItemInstrumentBlock));
    }
}
