package io.github.tofodroid.mods.mimi.client.midi;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import io.github.tofodroid.mods.mimi.common.item.ModItems;

public class MidiInputManager {
    public final MidiInputDeviceManager inputDeviceManager;
    public final MidiPlaylistManager playlistManager;

    private Boolean midiModeActive = null;

    public MidiInputManager() {
        this.inputDeviceManager = new MidiInputDeviceManager();
        this.playlistManager = new MidiPlaylistManager();
    }
    
    @SubscribeEvent
    public void handleTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT || !event.player.isUser()) {
            return;
        }

        Boolean lastMode = midiModeActive;
        updatePlayerData(event.player);

        if(lastMode != midiModeActive) {
            updateManagers();
        }
    }

    private void updateManagers() {
        if(midiModeActive == null) {
            // 1. No active transmitter found, close everything
            this.inputDeviceManager.close();
            this.playlistManager.close();
        } else if(midiModeActive) {
            // 2. Found device transmitter
            this.playlistManager.close();
            this.inputDeviceManager.open();
        } else {
            // 3. Found sequence transmitter
            this.inputDeviceManager.close();
            this.playlistManager.open();
        }
    }

    private void updatePlayerData(PlayerEntity player) {
        if(player != null && player.inventory != null) {
            ItemStack transmitterStack = null;

            // If an active transmitter is on cursor then don't update
            if(player.inventory.getItemStack() != null && ModItems.TRANSMITTER.equals(player.inventory.getItemStack().getItem()) && ModItems.TRANSMITTER.isEnabled(player.inventory.getItemStack())) {
                return;
            }

            // Off-hand isn't part of hotbar, so check it explicitly
            if(ModItems.TRANSMITTER.equals(player.getHeldItemOffhand().getItem()) && ModItems.TRANSMITTER.isEnabled(player.getHeldItemOffhand())) {
                transmitterStack = player.getHeldItemOffhand();
            }

            // check hotbar
            for(int i = 0; i < 9; i++) {
                ItemStack invStack = player.inventory.getStackInSlot(i);
                if(transmitterStack == null && ModItems.TRANSMITTER.equals(invStack.getItem()) && ModItems.TRANSMITTER.isEnabled(invStack)) {
                    transmitterStack = player.inventory.getStackInSlot(i);
                }
            }

            midiModeActive = transmitterStack != null ? ModItems.TRANSMITTER.isMidiMode(transmitterStack) : null;
        }
    }
}
