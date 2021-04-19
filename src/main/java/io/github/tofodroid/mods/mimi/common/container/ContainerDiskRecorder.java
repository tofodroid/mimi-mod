package io.github.tofodroid.mods.mimi.common.container;

import io.github.tofodroid.mods.mimi.common.item.ModItems;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;

public class ContainerDiskRecorder extends Container {
	public static final int PLAYER_INV_XPOS = 8;
	public static final int PLAYER_INV_YPOS = 79;
	public static final int HOTBAR_XPOS = 8;
	public static final int HOTBAR_YPOS = 137;
    public static final int DISK_IN_XPOS = 44;
    public static final int DISK_OUT_XPOS = 116;
    public static final int DISK_YPOS = 54;

    private final int HOTBAR_COLUMNS = 9;
	private final int PLAYER_INV_ROWS = 3;
	private final int PLAYER_INV_COLUMNS = 9;
    private final int SLOT_X_SPACING = 18;
    private final int SLOT_Y_SPACING = 18;
	
	private final IInventory inventory;

    public static ContainerDiskRecorder createContainerServerSide(int windowID, PlayerInventory playerInventory) {
        return new ContainerDiskRecorder(windowID, playerInventory);
    }
    
    public static ContainerDiskRecorder createContainerClientSide(int windowID, PlayerInventory playerInventory, net.minecraft.network.PacketBuffer extraData) {
        return new ContainerDiskRecorder(windowID, playerInventory);
    }

	public ContainerDiskRecorder(int windowId, PlayerInventory playerInventory) {
        this(windowId, playerInventory, IWorldPosCallable.DUMMY);
    }
	
	public ContainerDiskRecorder(int windowId, PlayerInventory playerInventory, IWorldPosCallable worldPosCallable) {
        super(ModContainers.CONTAINER_TYPE_CONTAINER_DISK_RECORDER, windowId);

        // Hotbar
        for (int column = 0; column < HOTBAR_COLUMNS; column++) {
            this.addSlot(new Slot(playerInventory, column, HOTBAR_XPOS + SLOT_X_SPACING * column, HOTBAR_YPOS));
		}

        // Inventory
        for (int column = 0; column < PLAYER_INV_COLUMNS; column++) {
            for(int row = 0; row < PLAYER_INV_ROWS; row++) {
                this.addSlot(new Slot(
                    playerInventory, 
                    HOTBAR_COLUMNS + row * PLAYER_INV_COLUMNS + column, 
                    PLAYER_INV_XPOS + SLOT_X_SPACING * column, 
                    PLAYER_INV_YPOS + row * SLOT_Y_SPACING
                ));
            }
		}

        // Slots
        this.inventory = new Inventory(2);
        this.addSlot(new FilteredSlot(inventory, 0, DISK_IN_XPOS, DISK_YPOS, Arrays.asList(ModItems.DISK), 1, ModItems.DISK::isEmptyDisk));
        this.addSlot(new FilteredSlot(inventory, 1, DISK_OUT_XPOS, DISK_YPOS, new ArrayList<>()));
	}
	
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerEntity, int sourceSlotIndex) {
        // TODO
        return ItemStack.EMPTY;
    }

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
        // TODO
		return true;
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
	}

	public boolean writeDisk(String title, String url, String playerName) {
        if(canWriteDisk()) {
            inventory.setInventorySlotContents(1, ModItems.DISK.writeDiskData(inventory.getStackInSlot(0), title, url, playerName));
            inventory.removeStackFromSlot(0);
            this.detectAndSendChanges();
            return true;
        }

        return false;
    }

    public boolean canWriteDisk() {
        return ModItems.DISK.equals(inventory.getStackInSlot(0).getItem()) && ItemStack.EMPTY.equals(inventory.getStackInSlot(1));
    }
}
