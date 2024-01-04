package io.github.tofodroid.mods.mimi.server.midi;

import java.util.UUID;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.midi.TransmitterNoteEvent;
import io.github.tofodroid.mods.mimi.server.midi.receiver.ServerMusicReceiverManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

public abstract class AServerMidiInputReceiver implements Receiver {
    private volatile boolean open = true;

    public synchronized void send(MidiMessage msg, long timeStamp) {
        if(open && msg instanceof ShortMessage) {
            handleMessage((ShortMessage)msg);
        }
    }
    
    protected abstract void handleMessage(ShortMessage message);
    protected abstract UUID getTransmitterId();
    protected abstract BlockPos getTransmitterPos();
    protected abstract Level getTransmitterLevel();

    public void close() {
        open = false;
    }

    // Packet Utils
    public void sendTransmitterNoteOnPacket(Byte channel, Byte midiNote, Byte velocity) {
        if(!(getTransmitterLevel() instanceof ServerLevel)) {
            return;
        }
        TransmitterNoteEvent packet = TransmitterNoteEvent.createNoteEvent(channel, midiNote, velocity);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            ServerMusicReceiverManager.handlePacket(packet, getTransmitterId(), getTransmitterPos(), (ServerLevel)getTransmitterLevel());
        });        
    }
    
    public void sendTransmitterNoteOffPacket(Byte channel, Byte midiNote) {
        if(!(getTransmitterLevel() instanceof ServerLevel)) {
            return;
        }
        TransmitterNoteEvent packet = TransmitterNoteEvent.createNoteEvent(channel, midiNote, Integer.valueOf(0).byteValue());
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            ServerMusicReceiverManager.handlePacket(packet, getTransmitterId(), getTransmitterPos(), (ServerLevel)getTransmitterLevel());
        });     
    }

    public void sendTransmitterAllNotesOffPacket() {
        this.sendTransmitterAllNotesOffPacket(TransmitterNoteEvent.ALL_CHANNELS);
    }

    public void sendTransmitterAllNotesOffPacket(Byte channel) {
        if(!(getTransmitterLevel() instanceof ServerLevel)) {
            return;
        }
        TransmitterNoteEvent packet = TransmitterNoteEvent.createAllNotesOffEvent(channel);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            ServerMusicReceiverManager.handlePacket(packet, getTransmitterId(), getTransmitterPos(), (ServerLevel)getTransmitterLevel());
        });     
    }

    public void sendTransmitterControllerPacket(Byte channel, Byte controller, Byte value) {
        if(!(getTransmitterLevel() instanceof ServerLevel)) {
            return;
        }
        TransmitterNoteEvent packet = TransmitterNoteEvent.createControllerEvent(channel, controller, value);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.execute(() -> {
            ServerMusicReceiverManager.handlePacket(packet, getTransmitterId(), getTransmitterPos(), (ServerLevel)getTransmitterLevel());
        });     
    }

    // Message Utils
    protected Boolean isNoteOnMessage(ShortMessage msg) {
        return ShortMessage.NOTE_ON == msg.getCommand() && msg.getData2() > 0;
    }

    protected Boolean isNoteOffMessage(ShortMessage msg) {
        return ShortMessage.NOTE_OFF == msg.getCommand() || (ShortMessage.NOTE_ON == msg.getCommand() && msg.getData2() == 0);
    }

    protected Boolean isAllNotesOffMessage(ShortMessage msg) {
        return ShortMessage.CONTROL_CHANGE == msg.getCommand() && ( msg.getData1() == 120 || msg.getData1() == 123);
    }
    
    protected abstract Boolean isSupportedControlMessage(ShortMessage msg);
}
