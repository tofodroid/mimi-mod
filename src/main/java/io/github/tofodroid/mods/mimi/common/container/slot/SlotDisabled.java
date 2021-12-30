package io.github.tofodroid.mods.mimi.common.container.slot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotDisabled extends Slot {
    public SlotDisabled(Inventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stackIn) {
		return false;
	}

	@Override
	public boolean mayPickup(Player player) {
		return false;
	}
}
