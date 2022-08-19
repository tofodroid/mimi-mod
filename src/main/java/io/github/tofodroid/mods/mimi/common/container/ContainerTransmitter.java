package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import org.jetbrains.annotations.NotNull;

import io.github.tofodroid.mods.mimi.common.container.slot.SlotDisabled;
import io.github.tofodroid.mods.mimi.common.item.ItemFloppyDisk;
import io.github.tofodroid.mods.mimi.common.item.ItemTransmitter;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;

public class ContainerTransmitter extends APlayerInventoryContainer {
	private static final int FLOPPY_SLOT_X = 8;
	private static final int FLOPPY_SLOT_Y = 189;

	protected IItemHandler targetInventory;
	public final Integer playerInvSlot;

	public ContainerTransmitter(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
		super(ModContainers.TRANSMITTER, id, playerInventory);
		this.playerInvSlot = extraData.readInt();
		this.targetInventory =  ItemTransmitter.getInventoryHandler(this.playerInventory.getItem(playerInvSlot));
		this.addSlot(buildFloppySlot());
		this.addSlotListener(new ContainerListener() {
			@Override
			public void slotChanged(AbstractContainerMenu p_39315_, int p_39316_, ItemStack p_39317_) {
				saveToInventory(((ContainerTransmitter)p_39315_).playerInventory.player);
			}

			@Override
			public void dataChanged(AbstractContainerMenu p_150524_, int p_150525_, int p_150526_) {
				saveToInventory(((ContainerTransmitter)p_150524_).playerInventory.player);
			}
		});
	}

	public ContainerTransmitter(int id, Inventory playerInventory, Integer playerInvSlot) {
		super(ModContainers.TRANSMITTER, id, playerInventory);
		this.playerInvSlot = playerInvSlot;
		this.targetInventory = ItemTransmitter.getInventoryHandler(this.playerInventory.getItem(this.playerInvSlot));
		this.addSlot(buildFloppySlot());
		this.addSlotListener(new ContainerListener() {
			@Override
			public void slotChanged(AbstractContainerMenu p_39315_, int p_39316_, ItemStack p_39317_) {
				saveToInventory(((ContainerTransmitter)p_39315_).playerInventory.player);
			}

			@Override
			public void dataChanged(AbstractContainerMenu p_150524_, int p_150525_, int p_150526_) {
				saveToInventory(((ContainerTransmitter)p_150524_).playerInventory.player);
			}
		});
	}

	public Boolean hasActiveFloppy() {
		ItemStack stack = this.targetInventory.getStackInSlot(0);
		return stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemFloppyDisk;
	}
    
	public void saveToInventory(Player player) {
		if(playerInvSlot != null && targetInventory instanceof ItemStackHandler) {
			ItemStack newStack = this.playerInventory.getItem(this.playerInvSlot);
			CompoundTag tag = newStack.getOrCreateTag();
			tag.put(ItemTransmitter.INVENTORY_TAG, ((ItemStackHandler)targetInventory).serializeNBT());
			this.playerInventory.setItem(this.playerInvSlot, newStack);
			this.playerInventory.setChanged();
		}
	}

	public ItemStack getTransmitterStack() {
		return this.playerInventory.getItem(this.playerInvSlot);
	}

	public TransmitMode getTransmitMode() {
		return ItemTransmitter.getTransmitMode(this.playerInventory.getItem(this.playerInvSlot));
	}

	public TransmitMode toggleTransmitMode() {
		TransmitMode oldMode = ItemTransmitter.getTransmitMode(this.playerInventory.getItem(this.playerInvSlot));
		TransmitMode newMode;

		if(TransmitMode.SELF.equals(oldMode)) {
			newMode = TransmitMode.LINKED;
		} else if(TransmitMode.LINKED.equals(oldMode)) {
			newMode = TransmitMode.PUBLIC;
		} else {
			newMode = TransmitMode.SELF;
		}

		ItemTransmitter.setTransmitMode(this.playerInventory.getItem(this.playerInvSlot), newMode);
		return newMode;
	}

	public ItemStack getActiveFloppyStack() {
		if(hasActiveFloppy()) {
			return this.targetInventory.getStackInSlot(0);
		}
		return null;
	}
	
	protected Slot buildFloppySlot() {
		return new SlotItemHandler(targetInventory, 0, FLOPPY_SLOT_X, FLOPPY_SLOT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.getItem() instanceof ItemFloppyDisk && ItemFloppyDisk.isWritten(stack);
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
			
			@Override
			public int getMaxStackSize(@NotNull ItemStack stack) {
				return 1;
			}
		};
	}

    @Override
	protected Slot buildPlayerSlot(Inventory playerInventory, int slot, int xPos, int yPos) {
		if(playerInventory.getItem(slot).getItem() instanceof ItemTransmitter) {
			return new SlotDisabled(playerInventory, slot, xPos, yPos);
		} else {
			return new Slot(playerInventory, slot, xPos, yPos);
		}
	}

    @Override
    protected Integer getPlayerInventoryX() {
        return 183;
    }

    @Override
    protected Integer getPlayerInventoryY() {
        return 149;
    }
    
	@Override
	public void removed(Player player) {
		this.saveToInventory(player);
		super.removed(player);
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		// Below code taken from Vanilla Chest Container
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem().copy();
			itemstack = itemstack1.copy();

			// Return Empty Stack if Cannot Merge
			if (index >= TARGET_CONTAINER_MIN_SLOT_ID) {
				// Target --> Player
				if (!this.moveItemStackTo(itemstack1, 0, TARGET_CONTAINER_MIN_SLOT_ID-1, false)) {
					return ItemStack.EMPTY;
				}
			} else {
				// Player --> Target
				if (!this.moveItemStackTo(itemstack1, TARGET_CONTAINER_MIN_SLOT_ID, TARGET_CONTAINER_MIN_SLOT_ID + 1, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.set(itemstack1);
			}
		}

	  return itemstack;
	}
	
	public IItemHandler getTargetInventory() {
		return targetInventory;
	}
}
