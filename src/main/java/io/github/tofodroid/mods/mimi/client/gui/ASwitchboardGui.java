package io.github.tofodroid.mods.mimi.client.gui;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.container.ASwitchboardContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import io.github.tofodroid.mods.mimi.common.container.ContainerAdvListener;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.SwitchboardStackUpdatePacket;
import io.github.tofodroid.mods.mimi.util.PlayerNameUtils;

import net.minecraft.item.ItemStack;

public abstract class ASwitchboardGui<T extends ASwitchboardContainer> extends BaseContainerGui<T> {
    protected final PlayerEntity player;

    protected Integer filterNoteOctave = -1;
    protected Integer filterNoteLetter = -1;
    protected String filterNoteString = "";
    protected String selectedSourceName = "None";
	protected ItemStack selectedSwitchboardStack;

    public ASwitchboardGui(T container, PlayerInventory inv, Integer width, Integer height, Integer textureSize, String textureResource, ITextComponent textComponent) {
        super(container, inv, width, height, textureSize, textureResource, textComponent);

        player = inv.player;

        if(ModItems.SWITCHBOARD.equals(container.getSlot(ContainerAdvListener.TARGET_CONTAINER_MIN_SLOT_ID).getStack().getItem())) {
            this.selectedSwitchboardStack = container.getSlot(ContainerAdvListener.TARGET_CONTAINER_MIN_SLOT_ID).getStack();
            loadSelectedSwitchboard();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if(this.selectedSwitchboardStack == null && container.getSlot(ASwitchboardContainer.TARGET_CONTAINER_MIN_SLOT_ID).getStack().isEmpty()) {
            return;
        } else if(this.selectedSwitchboardStack != container.getSlot(ASwitchboardContainer.TARGET_CONTAINER_MIN_SLOT_ID).getStack()) {
            if(!ModItems.SWITCHBOARD.equals(container.getSlot(ASwitchboardContainer.TARGET_CONTAINER_MIN_SLOT_ID).getStack().getItem())) {
                this.selectedSwitchboardStack = null;
            } else {
                this.selectedSwitchboardStack = container.getSlot(ASwitchboardContainer.TARGET_CONTAINER_MIN_SLOT_ID).getStack();
            }
            this.onSelectedSwitchboardChange();
        }
    }
    
    protected Boolean onSelectedSwitchboardChange() {
        if(selectedSwitchboardStack != null) {
            this.loadSelectedSwitchboard();
            return true;
        } else {
            this.clearSwitchboard();
            return false;
        }
    }

    protected void clearSwitchboard() {
        this.filterNoteLetter = -1;
        this.filterNoteOctave = -1;
        this.filterNoteString = "";
        this.selectedSourceName = "None";
    }

    protected void loadSelectedSwitchboard() {
        this.refreshSourceName();
        this.loadLetterAndOctave();
    }

    protected void loadLetterAndOctave() {
		if(this.selectedSwitchboardStack != null) {
			filterNoteLetter = ItemMidiSwitchboard.getFilterNote(selectedSwitchboardStack).intValue();
			filterNoteOctave = ItemMidiSwitchboard.getFilterOct(selectedSwitchboardStack).intValue();
			filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
		} else {
			filterNoteOctave = -1;
			filterNoteLetter = -1;
			filterNoteString = "";
		}       
    }
    
    protected void syncSwitchboardToServer() {
        SwitchboardStackUpdatePacket packet = null;

        if(selectedSwitchboardStack != null && ModItems.SWITCHBOARD.equals(selectedSwitchboardStack.getItem())) {
            packet = ItemMidiSwitchboard.getSyncPacket(selectedSwitchboardStack);
        }        
        if(packet != null) {
            NetworkManager.NET_CHANNEL.sendToServer(packet);
        }
    }

    protected void shiftFilterNoteLetter() {
        if(filterNoteLetter < 11) {
            filterNoteLetter++;
        } else {
            filterNoteLetter = -1;
        }

        ItemMidiSwitchboard.setFilterNote(selectedSwitchboardStack, filterNoteLetter.byteValue());
        this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
        this.syncSwitchboardToServer();
    }
    
    protected void shiftFilterNoteOctave() {
        if(filterNoteOctave < 10) {
            filterNoteOctave++;
        } else {
            filterNoteOctave = -1;
        }
        
        ItemMidiSwitchboard.setFilterOct(selectedSwitchboardStack, filterNoteOctave.byteValue());
        this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
        this.syncSwitchboardToServer();
    }

    protected void toggleInvertFilterNote() {
        ItemMidiSwitchboard.setInvertNoteOct(selectedSwitchboardStack, !ItemMidiSwitchboard.getInvertNoteOct(selectedSwitchboardStack));
        this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
        this.syncSwitchboardToServer();
    }
    
    protected void refreshSourceName() {
		if(this.selectedSwitchboardStack != null) {
			UUID sourceId = ItemMidiSwitchboard.getMidiSource(selectedSwitchboardStack);
			if(sourceId != null) {
				if(sourceId.equals(player.getUniqueID())) {
					this.selectedSourceName = player.getName().getString();
				} else if(sourceId.equals(ItemMidiSwitchboard.PUBLIC_SOURCE_ID)) {
					this.selectedSourceName = "Public Transmitters";
				} else if(this.minecraft != null && this.minecraft.world != null) {
					this.selectedSourceName = PlayerNameUtils.getPlayerNameFromUUID(sourceId, this.minecraft.world);
				} else {
					this.selectedSourceName = "Unknown";
				}
			} else {
				this.selectedSourceName = "None";
			}
		} else {
			this.selectedSourceName = "";
		}
    }

    protected void setSelfSource() {
        ItemMidiSwitchboard.setMidiSource(selectedSwitchboardStack, player.getUniqueID());
        this.syncSwitchboardToServer();
        this.refreshSourceName();
    }
    
    protected void setPublicSource() {
        ItemMidiSwitchboard.setMidiSource(selectedSwitchboardStack, ItemMidiSwitchboard.PUBLIC_SOURCE_ID);
        this.syncSwitchboardToServer();
        this.refreshSourceName();
    }

    protected void clearSource() {
        ItemMidiSwitchboard.setMidiSource(selectedSwitchboardStack, null);
        this.syncSwitchboardToServer();
        this.refreshSourceName();
    }

    protected void enableAllChannels() {
        ItemMidiSwitchboard.setEnableAllChannels(selectedSwitchboardStack);
        this.syncSwitchboardToServer();
    }

    protected void clearChannels() {
        ItemMidiSwitchboard.clearEnabledChannels(selectedSwitchboardStack);
        this.syncSwitchboardToServer();
    }

    protected void toggleChannel(Integer channelId) {
        ItemMidiSwitchboard.toggleChannel(selectedSwitchboardStack, new Integer(channelId).byteValue());
        this.syncSwitchboardToServer();
    }
    
	protected Boolean invalidFilterNote() {
		return new Integer(filterNoteOctave*12+filterNoteLetter) <= Byte.MAX_VALUE;
	}
}
