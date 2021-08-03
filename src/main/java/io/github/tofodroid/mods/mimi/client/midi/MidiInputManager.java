package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNoteOnPacket.TransmitMode;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class MidiInputManager {
    public final MidiInputDeviceManager inputDeviceManager;
    public final MidiPlaylistManager playlistManager;

    private Boolean hasTransmitter = false;
    private List<Object> localInstrumentToPlay = new ArrayList<>();

    public MidiInputManager() {
        this.inputDeviceManager = new MidiInputDeviceManager();
        this.playlistManager = new MidiPlaylistManager();
        this.playlistManager.open();
        this.inputDeviceManager.open();
    }

    public Boolean hasTransmitter() {
        return hasTransmitter;
    }

    public TransmitMode getTransmitMode() {
        return playlistManager.getTransmitMode();
    }
    
    public List<Byte> getLocalInstrumentsForMidiDevice(PlayerEntity player, Byte channel) {
        return localInstrumentToPlay.stream().map(data -> {
            ItemStack switchStack = ItemStack.EMPTY;
            Byte instrumentId = null;

            if(data instanceof ItemStack) {
                switchStack = ItemInstrument.getSwitchboardStack((ItemStack)data);
                instrumentId = ItemInstrument.getInstrumentId((ItemStack)data);
            } else if(data instanceof TileInstrument) {
                switchStack = ((TileInstrument)data).getSwitchboardStack();
                instrumentId = ((TileInstrument)data).getInstrumentId();
            }

            if(ModItems.SWITCHBOARD.equals(switchStack.getItem()) && ItemMidiSwitchboard.getSysInput(switchStack) && ItemMidiSwitchboard.isChannelEnabled(switchStack, channel)) {
                return instrumentId;
            }

            return null;
        })
        .filter(b -> b != null).collect(Collectors.toList());
    }
    
    @SubscribeEvent
    public void handleTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT || !event.player.isUser()) {
            return;
        }

        this.hasTransmitter = hasTransmitter(event.player);
        this.localInstrumentToPlay = localInstrumentsToPlay(event.player);
    }
    
    @SubscribeEvent
    public void handleSelfLogOut(LoggedOutEvent event) {
        if(event.getPlayer() != null && event.getPlayer().isUser()) {
            this.playlistManager.stop();
        }
    }
    
    protected Boolean hasTransmitter(PlayerEntity player) {
        if(player.inventory != null) {

            // Off-hand isn't part of hotbar, so check it explicitly
            if(ModItems.TRANSMITTER.equals(player.getHeldItemOffhand().getItem())) {
                return true;
            }

            // check hotbar
            for(int i = 0; i < 9; i++) {
                ItemStack invStack = player.inventory.getStackInSlot(i);
                if(invStack != null && ModItems.TRANSMITTER.equals(invStack.getItem())) {
                    return true;
                }
            }

            // check mouse item
            if(player.inventory.getItemStack() != null && ModItems.TRANSMITTER.equals(player.inventory.getItemStack().getItem())) {
                return hasTransmitter;
            }
        }

        return false;
    }

    protected List<Object> localInstrumentsToPlay(PlayerEntity player) {
        List<Object> result = new ArrayList<>();

        // Check for seated instrument
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(player);
        if(instrumentEntity != null && instrumentEntity.hasSwitchboard()) {
            result.add(instrumentEntity);
        }

        // Check for held instruments
        ItemStack mainHand = ItemInstrument.getEntityHeldInstrumentStack(player, Hand.MAIN_HAND);
        if(mainHand != null && ItemInstrument.hasSwitchboard(mainHand)) {
            result.add(mainHand);
        }

        ItemStack offHand = ItemInstrument.getEntityHeldInstrumentStack(player, Hand.OFF_HAND);
        if(offHand != null &&  ItemInstrument.hasSwitchboard(offHand)) {
            result.add(offHand);
        }

        return result;
    }
}
