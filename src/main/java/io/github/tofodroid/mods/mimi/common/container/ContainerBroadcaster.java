package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import org.jetbrains.annotations.NotNull;

import io.github.tofodroid.mods.mimi.common.item.ItemFloppyDisk;
import io.github.tofodroid.mods.mimi.common.tile.TileBroadcaster;

public class ContainerBroadcaster extends APlayerInventoryContainer {
	private static final int FLOPPY_SLOT_X = 8;
	private static final int FLOPPY_SLOT_Y = 189;

	protected IItemHandler targetInventory;
	private final BlockPos tilePos;

	public ContainerBroadcaster(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
		super(ModContainers.BROADCASTER, id, playerInventory);
		tilePos = extraData.readBlockPos();
		this.targetInventory =  playerInventory.player.level.getBlockEntity(tilePos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		this.addSlot(buildFloppySlot());
	}

	public ContainerBroadcaster(int id, Inventory playerInventory, BlockPos pos) {
		super(ModContainers.BROADCASTER, id, playerInventory);
		tilePos = pos;
		this.targetInventory =  playerInventory.player.level.getBlockEntity(tilePos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		this.addSlot(buildFloppySlot());
	}

	public Boolean hasActiveFloppy() {
		ItemStack stack = this.targetInventory.getStackInSlot(0);
		return stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemFloppyDisk;
	}

	public ItemStack getActiveFloppyStack() {
		if(hasActiveFloppy()) {
			return this.targetInventory.getStackInSlot(0);
		}
		return null;
	}

	public TileBroadcaster getBroadcasterTile() {
		BlockEntity ent = playerInventory.player.level.getBlockEntity(tilePos);

		if(ent != null && ent instanceof TileBroadcaster) {
			return (TileBroadcaster)ent;
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
    protected Integer getPlayerInventoryX() {
        return 183;
    }

    @Override
    protected Integer getPlayerInventoryY() {
        return 149;
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
				if (!this.moveItemStackTo(itemstack1, TARGET_CONTAINER_MIN_SLOT_ID, TARGET_CONTAINER_MIN_SLOT_ID, false)) {
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
