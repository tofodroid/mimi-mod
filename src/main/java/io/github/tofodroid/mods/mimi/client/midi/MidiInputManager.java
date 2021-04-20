package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MidiDevice.Info;

import io.github.tofodroid.mods.mimi.client.gui.GuiInstrument;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.item.ItemTransmitter;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.SpeakerNoteOffPacket;
import io.github.tofodroid.mods.mimi.common.network.SpeakerNoteOnPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

@SuppressWarnings("rawtypes")
public class MidiInputManager implements AutoCloseable {
    private Integer selectedDeviceId = null;
    private Transmitter activeTransmitter = null;
    private Receiver activeReceiver = null;
    private Sequencer activeSequencer = null;
    private Transmitter seqTransmitter = null;
    private Receiver seqReceiver = null;
    private List<MidiDevice> midiDevices = new ArrayList<>();
    private GuiInstrument instrumentGui = null;

    public MidiInputManager() {
        Integer configDeviceId = ModConfigs.CLIENT.defaultMidiInputDevice.get();
        this.loadMidiDevices();
        
        if(devicesAvailable() && configDeviceId != null && configDeviceId >= 0 && configDeviceId < this.midiDevices.size()) {
            this.selectDeviceById(configDeviceId);
        }
    }

    public Boolean devicesAvailable() {
        return this.midiDevices != null && !this.midiDevices.isEmpty();
    }

    public Boolean sequencerAvailable() {
        return this.activeSequencer != null && this.activeSequencer.isOpen();
    }

    public Sequencer getSequencer() {
        return this.activeSequencer;
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

    public void setGuiInstance(GuiInstrument inst) {
        this.instrumentGui = inst;
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
        this.closeActiveMidiDevice();
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
            this.closeActiveMidiDevice();
            this.selectedDeviceId = null;
        } else if(this.devicesAvailable() && deviceId < this.midiDevices.size()) {
            // Valid selection --> Close current device and select requested device
            this.closeActiveMidiDevice();
            this.selectedDeviceId = deviceId;
            this.openMidiTransmitterWithDevice(this.midiDevices.get(deviceId));
        } else {
            // Invalid selection --> Print warning and do nothing
            MIMIMod.LOGGER.error("Requested invalid device id: " + deviceId);
        }
    }

    public void loadMidiDevices() {
        List<MidiDevice> devices = new ArrayList<>();

        // Sequencer
        try {
            this.activeSequencer = MidiSystem.getSequencer(false);
            this.seqReceiver = new MidiInputReceiver(this);
            this.seqTransmitter = this.activeSequencer.getTransmitter();
            this.seqTransmitter.setReceiver(seqReceiver);
            this.activeSequencer.open();
        } catch(Exception e) {
            this.activeSequencer = null;
            this.seqReceiver = null;
            this.seqTransmitter = null;
            MIMIMod.LOGGER.error("Midi Sequencer Error. Will not setup sequencer. Error: ", e);
        }

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

    protected void openMidiTransmitterWithDevice(MidiDevice device) {
        if(device != null) {
            try {
                String oldVal = System.setProperty("javax.sound.midi.Transmitter", device.getDeviceInfo().getName());

                activeTransmitter = MidiSystem.getTransmitter();
                activeReceiver = new MidiInputReceiver(this);
                activeTransmitter.setReceiver(activeReceiver);

                if(oldVal != null) {
                    System.setProperty("javax.sound.midi.Transmitter", oldVal);
                } else {
                    System.clearProperty("javax.sound.midi.Transmitter");
                }
            } catch(MidiUnavailableException e) {
                MIMIMod.LOGGER.error("Midi Device Error: ", e);
                closeActiveMidiDevice();
            }
        }
    }

    protected void closeActiveMidiDevice() {
        if(activeReceiver != null) {
            activeReceiver.close();
            activeReceiver = null;
        }

        if(activeTransmitter != null) {
            activeTransmitter.close();
            activeTransmitter = null;
        }
    }

    // Message Handling
    @SuppressWarnings("resource")
    protected void handleMidiMessage(ShortMessage message) {
        PlayerEntity player = Minecraft.getInstance().player;

        // Ignore irrelevant messages and non-player states
        if(player == null || !isInterestingMessage(message)) {
            return;
        }

        if(hotbarHasEnabledTransmitter(player)) {
            if(isNoteOnMessage(message)) {
                this.sendRelayNoteOnPacket(new Integer(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2], player.getUniqueID());
            } else if(isNoteOffMessage(message)) {
                this.sendRelayNoteOffPacket(new Integer(message.getChannel()).byteValue(), message.getMessage()[1], player.getUniqueID());
            } else if(isAllNotesOffMessage(message)) {
                this.sendRelayNoteOffPacket(new Integer(message.getChannel()).byteValue(), SpeakerNoteOffPacket.ALL_NOTES_OFF, player.getUniqueID());
            }
        }
    }

    protected Boolean hotbarHasEnabledTransmitter(PlayerEntity player) {
        if(player != null && player.inventory != null) {
            for(int i = 0; i < 9; i++) {
                if(ModItems.TRANSMITTER.equals(player.inventory.getStackInSlot(i).getItem())) {
                    return player.inventory.getStackInSlot(i).getOrCreateTag().contains(ItemTransmitter.ENABLED_TAG);
                }
            }
        }
        return false;
    }

    protected void handleMidiMessageGui(ShortMessage midiMessage) {        
        if(isNoteOnMessage(midiMessage)) {
            this.instrumentGui.onMidiNoteOn(getChannel(midiMessage), getData1(midiMessage), getData2(midiMessage));
        } else if(isNoteOffMessage(midiMessage)) {
            this.instrumentGui.onMidiNoteOff(getChannel(midiMessage), getData1(midiMessage));
        } else if(isAllNotesOffMessage(midiMessage)) {
            this.instrumentGui.onMidiAllNotesOff(getChannel(midiMessage));
        }
    }

    // Packets
    public void sendRelayNoteOnPacket(Byte channel, Byte midiNote, Byte velocity, UUID playerId) {
        SpeakerNoteOnPacket packet = new SpeakerNoteOnPacket(channel, midiNote, velocity, playerId);
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
    public void sendRelayNoteOffPacket(Byte channel, Byte midiNote, UUID playerId) {
        SpeakerNoteOffPacket packet = new SpeakerNoteOffPacket(channel, midiNote, playerId);
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
    // Message Utils
    protected Byte getChannel(ShortMessage msg) {
        return new Integer(msg.getChannel()).byteValue();
    }

    protected Byte getData1(ShortMessage msg) {
        return msg.getLength() > 0 ? msg.getMessage()[1] : null;
    }

    protected Byte getData2(ShortMessage msg) {
        return msg.getLength() > 1 ? msg.getMessage()[2] : null;
    }

    protected Boolean isInterestingMessage(ShortMessage msg) {
        return isNoteOnMessage(msg) || isNoteOffMessage(msg) || isAllNotesOffMessage(msg);
    }

    protected Boolean isNoteOnMessage(ShortMessage msg) {
        return ShortMessage.NOTE_ON == msg.getCommand() && msg.getData2() > 0;
    }

    protected Boolean isNoteOffMessage(ShortMessage msg) {
        return ShortMessage.NOTE_OFF == msg.getCommand() || (ShortMessage.NOTE_ON == msg.getCommand() && msg.getData2() == 0);
    }

    protected Boolean isAllNotesOffMessage(ShortMessage msg) {
        return ShortMessage.CONTROL_CHANGE == msg.getCommand() && ( msg.getData1() == 120 || msg.getData2() == 123);
    }

    // Receiver    
    public class MidiInputReceiver implements Receiver {
        private MidiInputManager manager;

        public MidiInputReceiver(MidiInputManager manager) {
            this.manager = manager;
        }

        public void send(MidiMessage msg, long timeStamp) {
            if(msg instanceof ShortMessage && manager != null) {
                manager.handleMidiMessage((ShortMessage)msg);
            }
        }

        public void close() { }
    }

    @Override
    public void close() {
        this.closeActiveMidiDevice();
    }
}
