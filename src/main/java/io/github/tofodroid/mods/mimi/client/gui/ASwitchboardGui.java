package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.common.container.ASwitchboardContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import io.github.tofodroid.mods.mimi.common.container.ContainerListener;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.SwitchboardStackUpdatePacket;

import net.minecraft.item.ItemStack;

public abstract class ASwitchboardGui<T extends ASwitchboardContainer> extends BaseContainerGui<T> {
    protected final PlayerEntity player;
	protected ItemStack selectedSwitchboardStack;

    public ASwitchboardGui(T container, PlayerInventory inv, Integer width, Integer height, Integer textureSize, String textureResource, ITextComponent textComponent) {
        super(container, inv, width, height, textureSize, textureResource, textComponent);

        player = inv.player;

        if(ModItems.SWITCHBOARD.equals(container.getSlot(ContainerListener.TARGET_CONTAINER_MIN_SLOT_ID).getStack().getItem())) {
            this.selectedSwitchboardStack = container.getSlot(ContainerListener.TARGET_CONTAINER_MIN_SLOT_ID).getStack();
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

    protected void loadSelectedSwitchboard() {};
    protected void clearSwitchboard() {};

    protected void syncSwitchboardToServer() {
        SwitchboardStackUpdatePacket packet = null;

        if(selectedSwitchboardStack != null && ModItems.SWITCHBOARD.equals(selectedSwitchboardStack.getItem())) {
            packet = ItemMidiSwitchboard.getSyncPacket(selectedSwitchboardStack);
        }        
        if(packet != null) {
            NetworkManager.NET_CHANNEL.sendToServer(packet);
        }
    }
    
    protected void setSelfSource() {
        ItemMidiSwitchboard.setMidiSource(selectedSwitchboardStack, player.getUniqueID(), player.getName().getString());
        this.syncSwitchboardToServer();
    }
    
    protected void setPublicSource() {
        ItemMidiSwitchboard.setMidiSource(selectedSwitchboardStack, ItemMidiSwitchboard.PUBLIC_SOURCE_ID, "Public");
        this.syncSwitchboardToServer();
    }

    protected void clearSource() {
        ItemMidiSwitchboard.setMidiSource(selectedSwitchboardStack, null, "None");
        this.syncSwitchboardToServer();
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
}
