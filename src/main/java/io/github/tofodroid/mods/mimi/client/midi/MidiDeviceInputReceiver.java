package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.MidiNoteOffPacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNoteOnPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;

import net.minecraft.entity.player.PlayerEntity;

public class MidiDeviceInputReceiver extends MidiInputReceiver {

    @Override
    protected void handleMessage(ShortMessage message, PlayerEntity player) {
        MIMIMod.proxy.getMidiInput().getLocalInstrumentsToPlay(new Integer(message.getChannel()).byteValue()).forEach(instrument -> {
            if(isNoteOnMessage(message)) {
                sendMidiNoteOnPacket(instrument, message.getMessage()[1], message.getMessage()[2], player);
            } else if(isNoteOffMessage(message)) {
                sendMidiNoteOffPacket(instrument, message.getMessage()[1], player);
            } else if(isAllNotesOffMessage(message)) {
                sendMidiNoteOffPacket(instrument, MidiNoteOffPacket.ALL_NOTES_OFF, player);
            }
        });
    }
    
    public void sendMidiNoteOnPacket(Byte instrument, Byte midiNote, Byte velocity, PlayerEntity player) {
        MidiNoteOnPacket packet = new MidiNoteOnPacket(MidiNoteOnPacket.NO_CHANNEL, midiNote, velocity, instrument, player.getUniqueID(), player.getPosition());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
    public void sendMidiNoteOffPacket(Byte instrument, Byte midiNote, PlayerEntity player) {
        MidiNoteOffPacket packet = new MidiNoteOffPacket(MidiNoteOffPacket.NO_CHANNEL, midiNote, instrument, player.getUniqueID());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
}
