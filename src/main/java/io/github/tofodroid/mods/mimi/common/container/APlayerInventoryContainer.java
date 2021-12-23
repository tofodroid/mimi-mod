package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public abstract class APlayerInventoryContainer extends AbstractContainerMenu {
	public static final int PLAYER_INVENTORY_MIN_SLOT_ID = 0;
	public static final int PLAYER_HOTBAR_MIN_SLOT_ID = 27;
	public static final int TARGET_CONTAINER_MIN_SLOT_ID = 36;
    protected static final int SLOT_SPACING = 18;
	protected static final int INVENTORY_PLAYER_ROW_COUNT = 3;
	protected static final int INVENTORY_PLAYER_COLUMN_COUNT = 9;
	protected static final int HOTBAR_SLOT_COUNT = 9;
	protected static final int HOTBAR_Y_OFFSET = 40;

	protected Inventory playerInventory;
			
	public APlayerInventoryContainer(MenuType<?> menuType, int id, Inventory playerInventory) {
		super(menuType,id);
		this.playerInventory = playerInventory;
        this.buildPlayerSlots(playerInventory);
	}	

	protected abstract Integer getPlayerInventoryX();
	protected abstract Integer getPlayerInventoryY();
	
    protected void buildPlayerSlots(Inventory playerInventory) {
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

	protected Slot buildHotbarSlot(Inventory playerInventory, int slot, int xPos, int yPos) {
		return buildPlayerSlot(playerInventory, slot, xPos, yPos);
	}

	protected Slot buildPlayerSlot(Inventory playerInventory, int slot, int xPos, int yPos) {
		return new Slot(playerInventory, slot, xPos, yPos);
	}
	
	@Override
	public boolean stillValid(Player p_38874_) {
		return p_38874_.isAlive();
	}
}
