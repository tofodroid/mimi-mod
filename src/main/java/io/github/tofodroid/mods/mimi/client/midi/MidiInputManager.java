package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.midi.AMidiInputManager;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class MidiInputManager extends AMidiInputManager {
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
    
    public List<Pair<Byte,ItemStack>> getLocalInstrumentsForMidiDevice(Player player, Byte channel) {
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
                return new ImmutablePair<>(instrumentId,switchStack);
            }

            return null;
        })
        .filter(b -> b != null).collect(Collectors.toList());
    }
    
    @SubscribeEvent
    public void handleTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT || !event.player.isLocalPlayer()) {
            return;
        }

        if(hasTransmitter(event.player)) {
            this.hasTransmitter = true;
        } else {
            if(this.hasTransmitter) {
                this.playlistManager.stop();
            }
            this.hasTransmitter = false;
        }
        this.localInstrumentToPlay = localInstrumentsToPlay(event.player);
    }
    
    @SubscribeEvent
    public void handleSelfLogOut(LoggingOut event) {
        if(event.getPlayer() != null && event.getPlayer().isLocalPlayer()) {
            this.playlistManager.stop();
        }
    }

    @SubscribeEvent
    public void onDeathDevent(LivingDeathEvent event) {
        if(EntityType.PLAYER.equals(event.getEntity().getType()) && ((Player)event.getEntity()).isLocalPlayer()) {
            this.playlistManager.stop();
        }
    }
        
    protected Boolean hasTransmitter(Player player) {
        if(player.getInventory() != null) {

            // Off-hand isn't part of hotbar, so check it explicitly
            if(ModItems.TRANSMITTER.equals(player.getItemInHand(InteractionHand.OFF_HAND).getItem())) {
                return true;
            }

            // check hotbar
            for(int i = 0; i < 9; i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if(invStack != null && ModItems.TRANSMITTER.equals(invStack.getItem())) {
                    return true;
                }
            }

            // check mouse item
            if(player.getInventory().getSelected() != null && ModItems.TRANSMITTER.equals(player.getInventory().getSelected().getItem())) {
                return hasTransmitter;
            }
        }

        return false;
    }

    protected List<Object> localInstrumentsToPlay(Player player) {
        List<Object> result = new ArrayList<>();

        // Check for seated instrument
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(player);
        if(instrumentEntity != null && instrumentEntity.hasSwitchboard()) {
            result.add(instrumentEntity);
        }

        // Check for held instruments
        ItemStack mainHand = ItemInstrument.getEntityHeldInstrumentStack(player, InteractionHand.MAIN_HAND);
        if(mainHand != null && ItemInstrument.hasSwitchboard(mainHand)) {
            result.add(mainHand);
        }

        ItemStack offHand = ItemInstrument.getEntityHeldInstrumentStack(player, InteractionHand.OFF_HAND);
        if(offHand != null &&  ItemInstrument.hasSwitchboard(offHand)) {
            result.add(offHand);
        }

        return result;
    }
}
