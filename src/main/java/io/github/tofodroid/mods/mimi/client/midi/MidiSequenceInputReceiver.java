package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.MaestroNoteOffPacket;
import io.github.tofodroid.mods.mimi.common.network.MaestroNoteOnPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;

import net.minecraft.entity.player.PlayerEntity;

// Receiver
public class MidiSequenceInputReceiver extends MidiInputReceiver {

    @Override
    protected void handleMessage(ShortMessage message, PlayerEntity player) {
        if(MIMIMod.proxy.getMidiInput().hasTransmitter()) {
            if(isNoteOnMessage(message)) {
                this.sendMaestroNoteOnPacket(new Integer(message.getChannel()).byteValue(), message.getMessage()[1], message.getMessage()[2]);
            } else if(isNoteOffMessage(message)) {
                this.sendMaestroNoteOffPacket(new Integer(message.getChannel()).byteValue(), message.getMessage()[1]);
            } else if(isAllNotesOffMessage(message)) {
                this.sendMaestroNoteOffPacket(new Integer(message.getChannel()).byteValue(), MaestroNoteOffPacket.ALL_NOTES_OFF);
            }
        }
    }
    
    public void sendMaestroNoteOnPacket(Byte channel, Byte midiNote, Byte velocity) {
        MaestroNoteOnPacket packet = new MaestroNoteOnPacket(channel, midiNote, velocity, MIMIMod.proxy.getMidiInput().getTransmitMode());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
    public void sendMaestroNoteOffPacket(Byte channel, Byte midiNote) {
        MaestroNoteOffPacket packet = new MaestroNoteOffPacket(channel, midiNote, MIMIMod.proxy.getMidiInput().getTransmitMode());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
}
