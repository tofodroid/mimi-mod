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
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MidiDevice.Info;

import io.github.tofodroid.mods.mimi.client.gui.GuiInstrument;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.instruments.EntityInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.instruments.ItemInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemTransmitter;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.MidiNoteOffPacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNoteOnPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.SpeakerNoteOffPacket;
import io.github.tofodroid.mods.mimi.common.network.SpeakerNoteOnPacket;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

@SuppressWarnings("rawtypes")
public class MidiInputManager implements AutoCloseable {
    private Integer selectedDeviceId = null;
    private Transmitter activeTransmitter = null;
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
                // Init new connection
                if(!device.isOpen()) {
                    device.open();
                }

                activeTransmitter = device.getTransmitter();
                activeTransmitter.setReceiver(new MidiInputReceiver(this));
            } catch(MidiUnavailableException e) {
                MIMIMod.LOGGER.error("Midi Device Error: ", e);
                closeActiveMidiTransmitter();
                closeActiveMidiDevice();
            }
        }
    }

    protected void closeActiveMidiDevice() {
        this.closeActiveMidiTransmitter();

        if(selectedDeviceId != null && selectedDeviceId >= 0 && midiDevices != null && selectedDeviceId < midiDevices.size() &&  midiDevices.get(selectedDeviceId) != null) {
            if(midiDevices.get(selectedDeviceId).isOpen()) {
                midiDevices.get(selectedDeviceId).close();
            }
        }
    }

    protected void closeActiveMidiTransmitter() {
        if(activeTransmitter != null) {
            activeTransmitter.setReceiver(null);
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
        } else {
            this.handleSingleNote(message, player);
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

    protected void handleSingleNote(ShortMessage message, PlayerEntity player) {
        if(this.instrumentGui != null) {
            this.handleMidiMessageGui(message);
        } else {
            this.handleMidiMessageHeldInstruments(message, player);
            this.handleMidimessageSeatedInstrument(message, player);
        }
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

    protected void handleMidiMessageHeldInstruments(ShortMessage midiMessage, PlayerEntity player) {
        ItemStack mainStack = ItemInstrument.getEntityHeldInstrumentStack(player, Hand.MAIN_HAND);
        ItemStack offStack = ItemInstrument.getEntityHeldInstrumentStack(player, Hand.OFF_HAND);

        if(isNoteOnMessage(midiMessage)) {
            if(mainStack != null) this.instrumentStackMidiNoteOn(mainStack, getChannel(midiMessage), getData1(midiMessage), getData2(midiMessage), player);
            if(offStack != null)  this.instrumentStackMidiNoteOn(offStack, getChannel(midiMessage), getData1(midiMessage), getData2(midiMessage), player);
        } else if(isNoteOffMessage(midiMessage)) {
            if(mainStack != null) this.instrumentStackMidiNoteOff(mainStack, getChannel(midiMessage), getData1(midiMessage), player);
            if(offStack != null) this.instrumentStackMidiNoteOff(offStack, getChannel(midiMessage), getData1(midiMessage), player);
        } else if(isAllNotesOffMessage(midiMessage)) {
            if(mainStack != null) this.instrumentStackMidiNoteOff(mainStack, getChannel(midiMessage), MidiNoteOffPacket.ALL_NOTES_OFF, player);
            if(offStack != null) this.instrumentStackMidiNoteOff(offStack, getChannel(midiMessage), MidiNoteOffPacket.ALL_NOTES_OFF, player);
        }
    }
    
    protected void handleMidimessageSeatedInstrument(ShortMessage midiMessage, PlayerEntity player) {
        if(BlockInstrument.isEntitySittingAtInstrument(player)) {
            TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(player);

            if(instrumentEntity != null && isNoteOnMessage(midiMessage)) {
                instrumentEntityMidiNoteOn(instrumentEntity, getChannel(midiMessage), getData1(midiMessage), getData2(midiMessage), player);
            } else if(instrumentEntity != null && isNoteOffMessage(midiMessage)) {
                instrumentEntityMidiNoteOff(instrumentEntity, getChannel(midiMessage), getData1(midiMessage), player);
            } else if(instrumentEntity != null && isAllNotesOffMessage(midiMessage)) {
                instrumentEntityMidiNoteOff(instrumentEntity, getChannel(midiMessage), MidiNoteOffPacket.ALL_NOTES_OFF, player);
            }
        }
        
    }

    // Packets
    protected void sendNoteOnPacket(Byte instrumentId, Byte note, Byte velocity, PlayerEntity player) {
        MidiNoteOnPacket packet = new MidiNoteOnPacket(note, velocity, instrumentId, player.getUniqueID(), player.getPosition());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
    protected void sendNoteOffPacket(Byte instrumentId, Byte midiNote, PlayerEntity player) {
        MidiNoteOffPacket packet = new MidiNoteOffPacket(midiNote, instrumentId, player.getUniqueID());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
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

    // Item Stack Functions
    protected Boolean instrumentStackShouldHandleMessage(ItemStack instrumentStack, Byte channel) {
        return ItemInstrumentDataUtil.INSTANCE.midiInputSelected(instrumentStack) && ItemInstrumentDataUtil.INSTANCE.doesAcceptChannel(instrumentStack, channel);
    }

    protected void instrumentStackMidiNoteOn(ItemStack instrumentStack, Byte channel, Byte note, Byte velocity, PlayerEntity player) {
        Byte instrumentId = ItemInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(instrumentStack);
        if(instrumentId != null && this.instrumentStackShouldHandleMessage(instrumentStack, channel)) {
            this.sendNoteOnPacket(instrumentId, note, velocity, player);
        }
    }

    protected void instrumentStackMidiNoteOff(ItemStack instrumentStack, Byte channel, Byte note, PlayerEntity player) {
        Byte instrumentId = ItemInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(instrumentStack);
        if(instrumentId != null && this.instrumentStackShouldHandleMessage(instrumentStack, channel)) {
            this.sendNoteOffPacket(instrumentId, note, player);
        }
    }

    // Entity Functions
    protected Boolean instrumentEntityShouldHandleMessage(TileInstrument instrumentEntity, Byte channel) {
        return EntityInstrumentDataUtil.INSTANCE.midiInputSelected(instrumentEntity) && EntityInstrumentDataUtil.INSTANCE.doesAcceptChannel(instrumentEntity, channel);
    }

    protected void instrumentEntityMidiNoteOn(TileInstrument instrumentEntity, Byte channel, Byte note, Byte velocity, PlayerEntity player) {
        Byte instrumentId = EntityInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(instrumentEntity);
        if(instrumentId != null && this.instrumentEntityShouldHandleMessage(instrumentEntity, channel)) {
            this.sendNoteOnPacket(instrumentId, note, velocity, player);
        }
    }

    protected void instrumentEntityMidiNoteOff(TileInstrument instrumentEntity, Byte channel, Byte note, PlayerEntity player) {
        Byte instrumentId = EntityInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(instrumentEntity);
        if(instrumentId != null && this.instrumentEntityShouldHandleMessage(instrumentEntity, channel)) {
            this.sendNoteOffPacket(instrumentId, note, player);
        }
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
