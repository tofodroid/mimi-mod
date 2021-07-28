package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public abstract class APlayerInventoryContainer extends Container {
	public static final int PLAYER_INVENTORY_MIN_SLOT_ID = 0;
	public static final int PLAYER_HOTBAR_MIN_SLOT_ID = 27;
	public static final int TARGET_CONTAINER_MIN_SLOT_ID = 36;
    protected static final int SLOT_SPACING = 18;
	protected static final int INVENTORY_PLAYER_ROW_COUNT = 3;
	protected static final int INVENTORY_PLAYER_COLUMN_COUNT = 9;
	protected static final int HOTBAR_SLOT_COUNT = 9;
	protected static final int HOTBAR_Y_OFFSET = 40;

	protected PlayerInventory playerInventory;
	protected ItemStackHandler targetInventory;
			
	public APlayerInventoryContainer(ContainerType<?> type, int id, PlayerInventory playerInventory) {
		super(type, id);
		this.playerInventory = playerInventory;
        this.buildPlayerSlots(playerInventory);
	}	

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		// Below code taken from Vanilla Chest Container
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index == this.inventorySlots.size()-1) {
				if (!this.mergeItemStack(itemstack1, 0, this.inventorySlots.size()-1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, this.inventorySlots.size()-1, this.inventorySlots.size(), false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

      return itemstack;
	}
	
	protected abstract Integer getPlayerInventoryX();
	protected abstract Integer getPlayerInventoryY();
	
    protected void buildPlayerSlots(PlayerInventory playerInventory) {
 		// Build player inventory
 	    for (int row = 0; row < INVENTORY_PLAYER_ROW_COUNT; row++) {
 	    	for(int col = 0; col < INVENTORY_PLAYER_COLUMN_COUNT; col++) {
 	    		int slot = HOTBAR_SLOT_COUNT + row * INVENTORY_PLAYER_COLUMN_COUNT + col;
 	    		int xPos = getPlayerInventoryX() + col * SLOT_SPACING;
 	    		int yPos = getPlayerInventoryY() + (row - 1) * SLOT_SPACING;
 	    		
 	    		this.addSlot(buildPlayerSlot(playerInventory, slot, xPos, yPos));
 	        }
 	    }

 	    // Build player hotbar
 	    for (int slot = 0; slot < HOTBAR_SLOT_COUNT; slot++) {
 	    	int xPos = getPlayerInventoryX() + slot * SLOT_SPACING;
 	    	this.addSlot(buildHotbarSlot(playerInventory, slot, xPos, getPlayerInventoryY() + HOTBAR_Y_OFFSET));
 	    }
    }

	protected Slot buildHotbarSlot(PlayerInventory playerInventory, int slot, int xPos, int yPos) {
		return buildPlayerSlot(playerInventory, slot, xPos, yPos);
	}

	protected Slot buildPlayerSlot(PlayerInventory playerInventory, int slot, int xPos, int yPos) {
		return new Slot(playerInventory, slot, xPos, yPos);
	}
	
	public ItemStackHandler getTargetInventory() {
		return targetInventory;
	}
}
