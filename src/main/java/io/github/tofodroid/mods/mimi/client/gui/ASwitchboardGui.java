package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.common.container.ASwitchboardContainer;

import io.github.tofodroid.mods.mimi.common.container.ContainerListener;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.SwitchboardStackUpdatePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class ASwitchboardGui<T extends ASwitchboardContainer> extends BaseContainerGui<T> {
    protected final Player player;
    protected final T container;
	protected ItemStack selectedSwitchboardStack;

    public ASwitchboardGui(T container, Inventory inv, Integer width, Integer height, Integer textureSize, String textureResource, Component textComponent) {
        super(container, inv, width, height, textureSize, textureResource, textComponent);

        this.player = inv.player;
        this.container = container;

        if(ModItems.SWITCHBOARD.equals(container.getSlot(ContainerListener.TARGET_CONTAINER_MIN_SLOT_ID).getItem().getItem())) {
            this.selectedSwitchboardStack = container.getSlot(ContainerListener.TARGET_CONTAINER_MIN_SLOT_ID).getItem();
            loadSelectedSwitchboard();
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if(this.selectedSwitchboardStack == null && container.getSlot(ASwitchboardContainer.TARGET_CONTAINER_MIN_SLOT_ID).getItem().isEmpty()) {
            return;
        } else if(this.selectedSwitchboardStack != container.getSlot(ASwitchboardContainer.TARGET_CONTAINER_MIN_SLOT_ID).getItem()) {
            if(!ModItems.SWITCHBOARD.equals(container.getSlot(ASwitchboardContainer.TARGET_CONTAINER_MIN_SLOT_ID).getItem().getItem())) {
                this.selectedSwitchboardStack = null;
            } else {
                this.selectedSwitchboardStack = container.getSlot(ASwitchboardContainer.TARGET_CONTAINER_MIN_SLOT_ID).getItem();
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
        ItemMidiSwitchboard.setMidiSource(selectedSwitchboardStack, player.getUUID(), player.getName().getString());
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
        ItemMidiSwitchboard.toggleChannel(selectedSwitchboardStack, Integer.valueOf(channelId).byteValue());
        this.syncSwitchboardToServer();
    }
    
    protected void changeVolume(Integer changeAmount) {
        ItemMidiSwitchboard.setInstrumentVolume(selectedSwitchboardStack, Integer.valueOf(ItemMidiSwitchboard.getInstrumentVolume(selectedSwitchboardStack) + changeAmount).byteValue());
        this.syncSwitchboardToServer();
    }
}
