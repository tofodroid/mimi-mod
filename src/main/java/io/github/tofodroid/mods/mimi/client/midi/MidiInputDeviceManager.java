package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MidiInputDeviceManager {
    private List<Object> localInstrumentToPlay = new ArrayList<>();

    protected Receiver activeReceiver = null;
    protected Transmitter activeTransmitter = null;
    protected MidiDevice activeDevice = null;

    private String selectedDeviceName;
    private String midiDeviceError = null;


    public MidiInputDeviceManager() {
        selectedDeviceName = ModConfigs.CLIENT.selectedMidiDevice.get();
    }

    public Boolean isDeviceError() {
        return midiDeviceError != null;
    }

    public Boolean isDeviceSelected() {
        return this.selectedDeviceName != null && !this.selectedDeviceName.trim().isEmpty();
    }

    public Boolean isDeviceAvailable() {
        return this.activeTransmitter != null;
    }

    public String getSelectedDeviceName() {
        return selectedDeviceName;
    }

    public String getSelectedDeviceError() {
        return midiDeviceError;
    }

    public void saveDeviceSelection(MidiDevice device) {
        ModConfigs.CLIENT.selectedMidiDevice.set(device.getDeviceInfo().getName());
        selectedDeviceName = device.getDeviceInfo().getName();
        midiDeviceError = null;

        if(this.activeTransmitter != null) {
            this.close();
        }
        this.openTransmitter();
    }

    public void clearDeviceSelection() {
        ModConfigs.CLIENT.selectedMidiDevice.set("");
        this.close();
    }

    public List<MidiDevice> getAvailableDevices() {
        List<MidiDevice> devices = new ArrayList<>();

        // Devices
        for (int i = 0; i < MidiSystem.getMidiDeviceInfo().length; i++) {
            try {
                devices.add(MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[i]));
            } catch (MidiUnavailableException e) {
                MIMIMod.LOGGER.warn("Midi Device Error. Device will be skipped. Error: ", e);
            }
        }

        if(!devices.isEmpty()) {
            devices = devices.stream()
                .filter(d -> d.getMaxTransmitters() != 0)
                .filter(d -> !d.getClass().getName().contains("com.sun.media.sound.RealTimeSequencer"))
                .collect(Collectors.toList());
        }

        return devices;
    }

    @SubscribeEvent
    public void handleTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT || !event.player.isLocalPlayer()) {
            return;
        }

        this.localInstrumentToPlay = localInstrumentsToPlay(event.player);
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

    protected void openTransmitter() {
        if(isDeviceSelected() && this.activeTransmitter == null) {
            for(MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
                if(info.getName().toLowerCase().equals(this.selectedDeviceName.toLowerCase())) {
                    try {
                        MidiDevice device = MidiSystem.getMidiDevice(info);

                        if(device != null && device.getMaxTransmitters() != 0) {
                            activeDevice = device;
                            activeDevice.open();
                            activeTransmitter = device.getTransmitter();
                            activeReceiver = new MidiDeviceInputReceiver();
                            activeTransmitter.setReceiver(activeReceiver);
                        }
                    } catch(Exception e) {
                        MIMIMod.LOGGER.error("Failed to open MIDI Input Device: '" + this.selectedDeviceName + "'. Error: " + e.getMessage());
                        midiDeviceError = e.getMessage();
                        close();
                    }

                }
            }
        }
    }

    public void open() {
        if(this.activeTransmitter == null) {
            this.openTransmitter();
        }
    }

    public void close() {
        if(activeReceiver != null) {
            activeReceiver.close();
            activeReceiver = null;
        }

        if(activeTransmitter != null) {
            activeTransmitter.setReceiver(null);
            activeTransmitter.close();
            activeTransmitter = null;
        }

        if(activeDevice != null) {
            activeDevice.close();
            activeDevice = null;
        }

        this.selectedDeviceName = null;
    }
}
