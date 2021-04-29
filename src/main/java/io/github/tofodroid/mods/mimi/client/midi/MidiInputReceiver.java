package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.network.MaestroNoteOffPacket;
import io.github.tofodroid.mods.mimi.common.network.MaestroNoteOnPacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNoteOffPacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNoteOnPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

public abstract class MidiInputReceiver implements Receiver {
    @SuppressWarnings("resource")
    public void send(MidiMessage msg, long timeStamp) {
        PlayerEntity player = Minecraft.getInstance().player;

        if(player != null && msg instanceof ShortMessage) {
            handleMessage((ShortMessage)msg, player);
        }
    }

    public void close() { }

    protected abstract void handleMessage(ShortMessage message, PlayerEntity player);
        
    protected void handleMessageMaestro(ShortMessage message) {
        if(isNoteOnMessage(message)) {
            this.sendMaestroNoteOnPacket(new Integer(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        } else if(isNoteOffMessage(message)) {
            this.sendMaestroNoteOffPacket(new Integer(message.getChannel()).byteValue(), message.getMessage()[1]);
        } else if(isAllNotesOffMessage(message)) {
            this.sendMaestroNoteOffPacket(new Integer(message.getChannel()).byteValue(), MaestroNoteOffPacket.ALL_NOTES_OFF);
        } else {
            // TODO handle unknown message
        }
    }
    
    protected void handleMessageMidi(ShortMessage message, PlayerEntity player, Byte instrument) {
        if(isNoteOnMessage(message)) {
            sendMidiNoteOnPacket(instrument, message.getMessage()[1], message.getMessage()[2], player);
        } else if(isNoteOffMessage(message)) {
            sendMidiNoteOffPacket(instrument, message.getMessage()[1], player);
        } else if(isAllNotesOffMessage(message)) {
            sendMidiNoteOffPacket(instrument, MidiNoteOffPacket.ALL_NOTES_OFF, player);
        } else {
            // TODO handle unknown message
        }
    }
    
    // Message Utils
    protected Boolean isNoteOnMessage(ShortMessage msg) {
        return ShortMessage.NOTE_ON == msg.getCommand() && msg.getData2() > 0;
    }

    protected Boolean isNoteOffMessage(ShortMessage msg) {
        return ShortMessage.NOTE_OFF == msg.getCommand() || (ShortMessage.NOTE_ON == msg.getCommand() && msg.getData2() == 0);
    }

    protected Boolean isAllNotesOffMessage(ShortMessage msg) {
        return ShortMessage.CONTROL_CHANGE == msg.getCommand() && ( msg.getData1() == 120 || msg.getData2() == 123);
    }
    
    // Packets
    public void sendMaestroNoteOnPacket(Byte channel, Byte midiNote, Byte velocity) {
        MaestroNoteOnPacket packet = new MaestroNoteOnPacket(channel, midiNote, velocity);
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
    public void sendMaestroNoteOffPacket(Byte channel, Byte midiNote) {
        MaestroNoteOffPacket packet = new MaestroNoteOffPacket(channel, midiNote);
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
    public void sendMidiNoteOnPacket(Byte instrument, Byte midiNote, Byte velocity, PlayerEntity player) {
        MidiNoteOnPacket packet = new MidiNoteOnPacket(midiNote, velocity, instrument, player.getUniqueID(), player.getPosition());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
    public void sendMidiNoteOffPacket(Byte instrument, Byte midiNote, PlayerEntity player) {
        MidiNoteOffPacket packet = new MidiNoteOffPacket(midiNote, instrument, player.getUniqueID());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
}
