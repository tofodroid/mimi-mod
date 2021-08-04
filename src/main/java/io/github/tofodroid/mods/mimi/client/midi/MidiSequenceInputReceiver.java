package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;

import net.minecraft.entity.player.PlayerEntity;

// Receiver
public class MidiSequenceInputReceiver extends MidiInputReceiver {

    @Override
    protected void handleMessage(ShortMessage message, PlayerEntity player) {
        if(MIMIMod.proxy.getMidiInput().hasTransmitter()) {
            if(isNoteOnMessage(message)) {
                this.sendTransmitterNoteOnPacket(new Integer(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
            } else if(isNoteOffMessage(message)) {
                this.sendTransmitterNoteOffPacket(new Integer(message.getChannel()).byteValue(), message.getMessage()[1]);
            } else if(isAllNotesOffMessage(message)) {
                this.sendTransmitterNoteOffPacket(new Integer(message.getChannel()).byteValue(), TransmitterNotePacket.ALL_NOTES_OFF);
            }
        }
    }
    
    public void sendTransmitterNoteOnPacket(Byte channel, Byte midiNote, Byte velocity) {
        TransmitterNotePacket packet = new TransmitterNotePacket(channel, midiNote, velocity, MIMIMod.proxy.getMidiInput().getTransmitMode());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
    public void sendTransmitterNoteOffPacket(Byte channel, Byte midiNote) {
        TransmitterNotePacket packet = new TransmitterNotePacket(channel, midiNote, Integer.valueOf(0).byteValue(), MIMIMod.proxy.getMidiInput().getTransmitMode());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
}
