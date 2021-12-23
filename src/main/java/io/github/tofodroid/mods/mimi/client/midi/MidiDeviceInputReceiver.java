package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.util.DebugUtils;
import net.minecraft.world.entity.player.Player;

public class MidiDeviceInputReceiver extends MidiInputReceiver {
    @Override
    protected void handleMessage(ShortMessage message, Player player) {
        MIMIMod.proxy.getMidiInput().getLocalInstrumentsForMidiDevice(player, Integer.valueOf(message.getChannel()).byteValue()).forEach(pair -> {
            if(isNoteOnMessage(message)) {
                handleMidiNoteOn(Integer.valueOf(message.getChannel()).byteValue(), pair.getLeft(), message.getMessage()[1], ItemMidiSwitchboard.applyVolume(pair.getRight(), message.getMessage()[2]), player);
            } else if(isNoteOffMessage(message)) {
                handleMidiNoteOff(Integer.valueOf(message.getChannel()).byteValue(), pair.getLeft(), message.getMessage()[1], player);
            } else if(isAllNotesOffMessage(message)) {
                handleMidiNoteOff(Integer.valueOf(message.getChannel()).byteValue(), pair.getLeft(), MidiNotePacket.ALL_NOTES_OFF, player);
            }
        });
    }
    
    public void handleMidiNoteOn(Byte channel, Byte instrument, Byte midiNote, Byte velocity, Player player) {
        MidiNotePacket packet = new MidiNotePacket(midiNote, velocity, instrument, player.getUUID(), false, player.getOnPos());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
        DebugUtils.logNoteTimingInfo(this.getClass(), true, instrument, midiNote, velocity, player.getOnPos());
        MIMIMod.proxy.getMidiSynth().handlePacket(packet);
    }
    
    public void handleMidiNoteOff(Byte channel, Byte instrument, Byte midiNote, Player player) {
        MidiNotePacket packet = new MidiNotePacket(midiNote, Integer.valueOf(0).byteValue(), instrument, player.getUUID(), false, player.getOnPos());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
        DebugUtils.logNoteTimingInfo(this.getClass(), false, instrument, midiNote, null, null);
        MIMIMod.proxy.getMidiSynth().handlePacket(packet);
    }
}
