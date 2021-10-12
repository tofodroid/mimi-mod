package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;

public class MidiInputDeviceManager extends MidiInputSourceManager {
    private String selectedDeviceName;
    private String midiDeviceError;
    private Boolean dirty = false;

    public MidiInputDeviceManager() {
        selectedDeviceName = ModConfigs.CLIENT.selectedMidiDevice.get();
    }

    public Boolean isDeviceSelected() {
        return this.selectedDeviceName != null && !this.selectedDeviceName.trim().isEmpty();
    }

    public Boolean isDeviceAvailable() {
        return this.activeTransmitter != null;
    }

    public Boolean isDirtyStatus() {
        return this.dirty;
    }

    public String getSelectedDeviceName() {
        return selectedDeviceName;
    }

    public String getSelectedDeviceError() {
        return midiDeviceError;
    }

    public void saveDeviceSelection(MidiDevice device) {
        ModConfigs.CLIENT.selectedMidiDevice.set(device.getDeviceInfo().getName());
        dirty = true;
    }

    public void clearDeviceSelection() {
        ModConfigs.CLIENT.selectedMidiDevice.set("");
        dirty = true;
    }

    public List<MidiDevice> getAvailableDevices() {
        List<MidiDevice> devices = new ArrayList<>();

        // Devices
        for (int i = 0; i < MidiSystem.getMidiDeviceInfo().length; i++) {
            try {
                devices.add(MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[i]));
            } catch (MidiUnavailableException e) {
                midiDeviceError = e.getMessage();
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

    protected void openTransmitter() {
        if(isDeviceSelected()) {
            try {
                String oldVal = System.setProperty("javax.sound.midi.Transmitter", "#" + selectedDeviceName);

                activeTransmitter = MidiSystem.getTransmitter();
                activeTransmitter.setReceiver(new MidiDeviceInputReceiver());

                if(oldVal != null) {
                    System.setProperty("javax.sound.midi.Transmitter", oldVal);
                } else {
                    System.clearProperty("javax.sound.midi.Transmitter");
                }
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Midi Device Error: ", e);
                close();
            }
        }
    }

    public void open() {
        if(this.activeTransmitter == null) {
            this.openTransmitter();
        }
    }
}
