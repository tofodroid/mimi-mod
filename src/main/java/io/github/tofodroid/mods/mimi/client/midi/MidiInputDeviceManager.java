package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentHandheld;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.forge.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MidiInputDeviceManager {
    private List<Pair<InteractionHand, ItemStack>> localInstrumentsToPlay = new ArrayList<>();

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

    @SuppressWarnings("resource")
    public void handlePlayerTick(Player player) {
        if(!player.getUUID().equals(Minecraft.getInstance().player.getUUID())) {
            return;
        }

        if(player.isAlive() && player.isAddedToWorld()) {
            this.localInstrumentsToPlay = localInstrumentsToPlay(player);
        } else {
            this.localInstrumentsToPlay.clear();
        }
    }
    
    protected List<Pair<InteractionHand, ItemStack>> localInstrumentsToPlay(Player player) {
        List<Pair<InteractionHand, ItemStack>> result = new ArrayList<>();

        // Check for seated instrument
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(player);
        if(instrumentEntity != null) {
            result.add(Pair.of(null, instrumentEntity.getInstrumentStack()));
        }

        // Check for held instruments
        ItemStack mainHand = ItemInstrumentHandheld.getEntityHeldInstrumentStack(player, InteractionHand.MAIN_HAND);
        if(mainHand != null) {
            result.add(Pair.of(InteractionHand.MAIN_HAND, mainHand));
        }

        ItemStack offHand = ItemInstrumentHandheld.getEntityHeldInstrumentStack(player, InteractionHand.OFF_HAND);
        if(offHand != null) {
            result.add(Pair.of(InteractionHand.OFF_HAND, offHand));
        }

        return result;
    }
    
    public List<Pair<InteractionHand, ItemStack>> getLocalInstrumentsForMidiDevice(Player player, Byte channel) {
        return localInstrumentsToPlay.stream()
            .filter(instrumentStack -> MidiNbtDataUtils.getSysInput(instrumentStack.getRight()) && MidiNbtDataUtils.isChannelEnabled(instrumentStack.getRight(), channel))
            .collect(Collectors.toList());
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
