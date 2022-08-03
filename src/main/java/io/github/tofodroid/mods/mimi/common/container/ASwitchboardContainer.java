package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;

public abstract class ASwitchboardContainer extends APlayerInventoryContainer {
	private static final int INVENTORY_PLAYER_START_X = 165;
	private static final int INVENTORY_PLAYER_START_Y = 182;
	private static final int SWITCHBOARD_SLOT_POS_X = 127;
	private static final int SWITCHBOARD_SLOT_POS_Y = 193;

	protected IItemHandler targetInventory;

	public ASwitchboardContainer(MenuType<?> type, int id, Inventory playerInventory) {
		super(type, id, playerInventory);
	}

	protected Slot buildSwitchboardSlot() {
		return new SlotItemHandler(targetInventory, 0, getSwitchboardSlotX(), getSwitchboardSlotY()) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.getItem() instanceof ItemMidiSwitchboard;
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
			
			@Override
			public int getMaxStackSize(@NotNull ItemStack stack)
			{
				return 1;
			}
		};
	}

	@Override
	protected Slot buildPlayerSlot(Inventory playerInventory, int slot, int xPos, int yPos) {
		return new Slot(playerInventory, slot, xPos, yPos);
	}
	
	@Override
	protected Integer getPlayerInventoryX() {
		return INVENTORY_PLAYER_START_X;
	}

	@Override
	protected Integer getPlayerInventoryY() {
		return INVENTORY_PLAYER_START_Y;
	}

	protected Integer getSwitchboardSlotX() {
		return SWITCHBOARD_SLOT_POS_X;
	}

	protected Integer getSwitchboardSlotY() {
		return SWITCHBOARD_SLOT_POS_Y;
	}

	public Boolean updateSelectedSwitchboard(ServerPlayer player, UUID newSourceId, String newSourceName, Byte newFilterOct, Byte newFilterNote, Boolean newInvertNoteOct, String newChannelString, Byte newInstrumentId, Boolean newInvertInstrument, Boolean newSysInput, Boolean newPublicBroadcast, Byte newBroadcastNote, Byte newVolume) {
		ItemStack selectedStack = this.getSlot(APlayerInventoryContainer.TARGET_CONTAINER_MIN_SLOT_ID).getItem();

		if(ModItems.SWITCHBOARD.equals(selectedStack.getItem())) {
			ItemMidiSwitchboard.setMidiSource(selectedStack, newSourceId, newSourceName);
			ItemMidiSwitchboard.setFilterOct(selectedStack, newFilterOct);
			ItemMidiSwitchboard.setFilterNote(selectedStack, newFilterNote);
			ItemMidiSwitchboard.setInvertNoteOct(selectedStack, newInvertNoteOct);
			ItemMidiSwitchboard.setEnabledChannelsString(selectedStack, newChannelString);
			ItemMidiSwitchboard.setInstrument(selectedStack, newInstrumentId);
			ItemMidiSwitchboard.setInvertInstrument(selectedStack, newInvertInstrument);
			ItemMidiSwitchboard.setSysInput(selectedStack, newSysInput);
			ItemMidiSwitchboard.setPublicBroadcast(selectedStack, newPublicBroadcast);
			ItemMidiSwitchboard.setBroadcastNote(selectedStack, newBroadcastNote);
			ItemMidiSwitchboard.setInstrumentVolume(selectedStack, newVolume);
			this.setItem(APlayerInventoryContainer.TARGET_CONTAINER_MIN_SLOT_ID, this.getStateId()+1, selectedStack);
			this.setRemoteSlot(APlayerInventoryContainer.TARGET_CONTAINER_MIN_SLOT_ID, selectedStack);
			this.sendAllDataToRemote();
			return true;
		}

		return false;
	}

	public ItemStack getSelectedSwitchboard() {
		return this.getSlot(APlayerInventoryContainer.TARGET_CONTAINER_MIN_SLOT_ID).getItem();
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
				if (!this.moveItemStackTo(itemstack1, TARGET_CONTAINER_MIN_SLOT_ID, TARGET_CONTAINER_MIN_SLOT_ID+targetInventory.getSlots(), false)) {
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
