package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNoteOffPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNoteOnPacket;
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
                this.sendTransmitterNoteOffPacket(new Integer(message.getChannel()).byteValue(), TransmitterNoteOffPacket.ALL_NOTES_OFF);
            }
        }
    }
    
    public void sendTransmitterNoteOnPacket(Byte channel, Byte midiNote, Byte velocity) {
        TransmitterNoteOnPacket packet = new TransmitterNoteOnPacket(channel, midiNote, velocity, MIMIMod.proxy.getMidiInput().getTransmitMode());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
    public void sendTransmitterNoteOffPacket(Byte channel, Byte midiNote) {
        TransmitterNoteOffPacket packet = new TransmitterNoteOffPacket(channel, midiNote, MIMIMod.proxy.getMidiInput().getTransmitMode());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
}
