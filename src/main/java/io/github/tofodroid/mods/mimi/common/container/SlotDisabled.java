package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotDisabled extends Slot {
    public SlotDisabled(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean canTakeStack(PlayerEntity playerIn) {
		return false;
	}

	@Override
	public boolean isItemValid(ItemStack stackIn) {
		return false;
	}
}
