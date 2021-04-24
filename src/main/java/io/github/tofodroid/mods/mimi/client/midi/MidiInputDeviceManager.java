package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;

public class MidiInputDeviceManager extends MidiInputSourceManager {
    private Integer selectedDeviceId = null;
    private List<MidiDevice> midiDevices = new ArrayList<>();
    
    public MidiInputDeviceManager() {
        Integer configDeviceId = ModConfigs.CLIENT.defaultMidiInputDevice.get();
        this.loadMidiDevices();
        
        if(devicesAvailable() && configDeviceId != null && configDeviceId >= 0 && configDeviceId < this.midiDevices.size()) {
            this.selectDeviceById(configDeviceId);
        }
    }

    public Boolean devicesAvailable() {
        return this.midiDevices != null && !this.midiDevices.isEmpty();
    }

    public Boolean isSelectedDeviceAvailable() {
        return this.selectedDeviceId != null && this.activeTransmitter != null && this.activeTransmitter.getReceiver() != null;
    }

    public Integer getSelectedDeviceId() {
        return this.selectedDeviceId;
    }

    public String getSelectedDeviceName() {
        return this.devicesAvailable() && this.selectedDeviceId != null ? this.midiDevices.get(this.selectedDeviceId).getDeviceInfo().getName() : "None";
    }
    
    public Info getSelectedDeviceInfo() {
        return this.devicesAvailable() && this.selectedDeviceId != null ? this.midiDevices.get(this.selectedDeviceId).getDeviceInfo() : null;
    }

    public Integer getNumDevices() {
        return this.midiDevices != null ? this.midiDevices.size() : 0;
    }

    // MIDI Devices
    public Boolean shiftMidiDevice(Boolean up) {
        // If no devices are available then don't do anything
        if(this.midiDevices == null || this.midiDevices.isEmpty()) {
            return false;
        }

        // If trying to shift up beyond midi device count then don't do anything
        if(up && (selectedDeviceId != null && selectedDeviceId >= midiDevices.size() - 1)) {
            return false;
        }
        
        if(up) {
            this.selectDeviceById(this.selectedDeviceId == null ? 0 : this.selectedDeviceId + 1);
        } else {
            this.selectDeviceById(this.selectedDeviceId != null && this.selectedDeviceId > 0 ? this.selectedDeviceId - 1 : null);
        }

        return true;
    }

    public void clearDeviceSelection() {
        if(this.selectedDeviceId == null) {
            return;
        }
        this.close();
        selectedDeviceId = null;
    }

    public void selectFirstDevice() {
        this.selectDeviceById(0);
    }

    private void selectDeviceById(Integer deviceId) {
        if(deviceId != null && deviceId.equals(this.selectedDeviceId)) {
            // Requested device already selected --> do nothing
            return;
        } else if(deviceId == null || deviceId < 0) {
            // Null or negative --> Close current device and select none
            this.close();
            this.selectedDeviceId = null;
        } else if(this.devicesAvailable() && deviceId < this.midiDevices.size()) {
            // Valid selection --> Close current device and select requested device
            this.close();
            this.selectedDeviceId = deviceId;
            this.openTransmitter();
        } else {
            // Invalid selection --> Print warning and do nothing
            MIMIMod.LOGGER.error("Requested invalid device id: " + deviceId);
        }
    }

    public void loadMidiDevices() {
        List<MidiDevice> devices = new ArrayList<>();

        // Devices
        for (int i = 0; i < MidiSystem.getMidiDeviceInfo().length; i++) {
            try {
                devices.add(MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[i]));
            } catch (MidiUnavailableException e) {
                MIMIMod.LOGGER.error("Midi Device Error. Device will be skipped. Error: ", e);
            }
        }

        if(!devices.isEmpty()) {
            MidiDevice selectedDevice = null;

            if(this.devicesAvailable() && this.selectedDeviceId != null) {
                selectedDevice = this.midiDevices.get(this.selectedDeviceId);
            }

            this.midiDevices = devices.stream()
                .filter(d -> d.getMaxTransmitters() != 0)
                .filter(d -> !d.getClass().getName().contains("com.sun.media.sound.RealTimeSequencer"))
                .collect(Collectors.toList());

            this.midiDevices.forEach(device -> {
                MIMIMod.LOGGER.info("Available MIDI Device: " + device.getDeviceInfo().toString());
            });

            if(selectedDevice != null && (this.midiDevices == null || !this.midiDevices.contains(selectedDevice))) { 
                MIMIMod.LOGGER.warn("Previously selected midi device no longer available. Clearing.");
                this.clearDeviceSelection();
            }
        } else if(this.selectedDeviceId != null) {
            MIMIMod.LOGGER.warn("Previously selected midi device no longer available. Clearing.");
            this.clearDeviceSelection();
        }
    }

    protected void openTransmitter() {
        if(this.selectedDeviceId != null) {
            MidiDevice device = this.midiDevices.get(this.selectedDeviceId);
            try {
                String oldVal = System.setProperty("javax.sound.midi.Transmitter", device.getDeviceInfo().getName());

                activeTransmitter = MidiSystem.getTransmitter();
                activeTransmitter.setReceiver(new MidiInputReceiver());

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
        this.selectDeviceById(this.selectedDeviceId);

        if(this.activeTransmitter == null) {
            this.openTransmitter();
        }
    }
}
