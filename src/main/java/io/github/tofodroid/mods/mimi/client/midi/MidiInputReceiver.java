package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.network.MaestroNoteOffPacket;
import io.github.tofodroid.mods.mimi.common.network.MaestroNoteOnPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;

// Receiver
public class MidiInputReceiver implements Receiver {
    public void send(MidiMessage msg, long timeStamp) {
        if(msg instanceof ShortMessage ) {
            handleMidiSequenceMessage((ShortMessage)msg);
        }
    }

    public void close() { }

    protected void handleMidiSequenceMessage(ShortMessage message) {
        if(isNoteOnMessage(message)) {
            this.sendRelayNoteOnPacket(new Integer(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
        } else if(isNoteOffMessage(message)) {
            this.sendRelayNoteOffPacket(new Integer(message.getChannel()).byteValue(), message.getMessage()[1]);
        } else if(isAllNotesOffMessage(message)) {
            this.sendRelayNoteOffPacket(new Integer(message.getChannel()).byteValue(), MaestroNoteOffPacket.ALL_NOTES_OFF);
        } else {
            // TODO handle unknown message
        }
    }
    
    // Message Utils
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
    
    // Packets
    public void sendRelayNoteOnPacket(Byte channel, Byte midiNote, Byte velocity) {
        MaestroNoteOnPacket packet = new MaestroNoteOnPacket(channel, midiNote, velocity);
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
    public void sendRelayNoteOffPacket(Byte channel, Byte midiNote) {
        MaestroNoteOffPacket packet = new MaestroNoteOffPacket(channel, midiNote);
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
}
