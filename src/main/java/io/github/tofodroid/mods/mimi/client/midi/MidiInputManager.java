package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.instruments.EntityInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.instruments.InstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.instruments.ItemInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
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
    
    public List<Byte> getLocalInstrumentsToPlay(Byte channel) {
        return localInstrumentToPlay.stream().filter(data -> {
            if(data instanceof ItemStack) {
                return ItemInstrumentDataUtil.INSTANCE.doesAcceptChannel((ItemStack)data, channel);
            } else if(data instanceof TileInstrument) {
                return EntityInstrumentDataUtil.INSTANCE.doesAcceptChannel((TileInstrument)data, channel);
            } else {
                return false;
            }
        }).map(data -> {
            if(data instanceof ItemStack) {
                return ItemInstrumentDataUtil.INSTANCE.getInstrumentIdFromData((ItemStack)data);
            } else {
                return EntityInstrumentDataUtil.INSTANCE.getInstrumentIdFromData((TileInstrument)data);
            }
        }).collect(Collectors.toList());
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
        if(instrumentEntity != null && InstrumentDataUtil.MIDI_MAESTRO_ID.equals(EntityInstrumentDataUtil.INSTANCE.getLinkedMaestro(instrumentEntity))) {
            result.add(instrumentEntity);
        }

        // Check for held instruments
        ItemStack mainHand = ItemInstrument.getEntityHeldInstrumentStack(player, Hand.MAIN_HAND);
        if(mainHand != null && InstrumentDataUtil.MIDI_MAESTRO_ID.equals(ItemInstrumentDataUtil.INSTANCE.getLinkedMaestro(mainHand))) {
            result.add(mainHand);
        }

        ItemStack offHand = ItemInstrument.getEntityHeldInstrumentStack(player, Hand.OFF_HAND);
        if(offHand != null && InstrumentDataUtil.MIDI_MAESTRO_ID.equals(ItemInstrumentDataUtil.INSTANCE.getLinkedMaestro(mainHand))) {
            result.add(offHand);
        }

        return result;
    }
}
