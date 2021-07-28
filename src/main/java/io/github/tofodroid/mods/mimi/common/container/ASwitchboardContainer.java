package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.UUID;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;

public abstract class ASwitchboardContainer extends APlayerInventoryContainer {
    
	public ASwitchboardContainer(ContainerType<?> type, int id, PlayerInventory playerInventory) {
        super(type, id, playerInventory);
    }

    protected Slot buildSwitchboardSlot(int xPos, int yPos) {
        return new SlotItemHandler(targetInventory, 0, xPos, yPos) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return ModItems.SWITCHBOARD.equals(stack.getItem());
            }
        };
    }

    @Override
	protected Slot buildPlayerSlot(PlayerInventory playerInventory, int slot, int xPos, int yPos) {
		if(playerInventory.getStackInSlot(slot).isEmpty() || ModItems.SWITCHBOARD.equals(playerInventory.getStackInSlot(slot).getItem())) {
			return new Slot(playerInventory, slot, xPos, yPos) {
				@Override
				public boolean isItemValid(ItemStack stack) {
					return ModItems.SWITCHBOARD.equals(stack.getItem());
				}
			};
		} else {
			return new SlotDisabled(playerInventory, slot, xPos, yPos);
		}
	}

	public Boolean updateSelectedSwitcboard(ServerPlayerEntity player, UUID newSourceId, String newChannelString, String newNoteString) {
		ItemStack selectedStack = this.getSlot(ContainerInstrument.TARGET_CONTAINER_MIN_SLOT_ID).getStack();

		if(ModItems.SWITCHBOARD.equals(selectedStack.getItem())) {
			ItemMidiSwitchboard.setMidiSource(selectedStack, newSourceId);
			ItemMidiSwitchboard.setEnabledChannelsString(selectedStack, newChannelString);
			ItemMidiSwitchboard.setFilterNoteString(selectedStack, newNoteString);
			this.detectAndSendChanges();
            return true;
		}

        return false;
	}

	public ItemStack getSelectedSwitchboard() {
		return this.getSlot(ContainerInstrument.TARGET_CONTAINER_MIN_SLOT_ID).getStack();
	}
}
