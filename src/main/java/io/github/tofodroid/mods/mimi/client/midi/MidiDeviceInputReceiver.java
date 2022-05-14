package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.midi.MidiInputReceiver;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
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
                handleAllNotesOff(Integer.valueOf(message.getChannel()).byteValue(), pair.getLeft(), player);
            } else if(isSupportedControlMessage(message)) {
                handleControlMessage(Integer.valueOf(message.getChannel()).byteValue(), pair.getLeft(), message.getMessage()[1], message.getMessage()[2], player);
            }
        });
    }
    
    public void handleMidiNoteOn(Byte channel, Byte instrument, Byte midiNote, Byte velocity, Player player) {
        MidiNotePacket packet = new MidiNotePacket(midiNote, velocity, instrument, player.getUUID(), player.getOnPos());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
        MIMIMod.proxy.getMidiSynth().handleLocalPacket(packet);
    }
    
    public void handleMidiNoteOff(Byte channel, Byte instrument, Byte midiNote, Player player) {
        MidiNotePacket packet = new MidiNotePacket(midiNote, Integer.valueOf(0).byteValue(), instrument, player.getUUID(), player.getOnPos());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
        MIMIMod.proxy.getMidiSynth().handleLocalPacket(packet);
    }

    public void handleAllNotesOff(Byte channel, Byte instrument, Player player) {
        MidiNotePacket packet =MidiNotePacket.createAllNotesOffPacket(instrument, player.getUUID(), player.getOnPos());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
        MIMIMod.proxy.getMidiSynth().handleLocalPacket(packet);
    }
    
    public void handleControlMessage(Byte channel, Byte instrument, Byte controller, Byte value, Player player) {
        MidiNotePacket packet =MidiNotePacket.createControlPacket(controller, value, instrument, player.getUUID(), player.getOnPos());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
        MIMIMod.proxy.getMidiSynth().handleLocalPacket(packet);
    }

    @Override
    protected Boolean isSupportedControlMessage(ShortMessage msg) {
        return ShortMessage.CONTROL_CHANGE == msg.getCommand() && (
            msg.getData1() == 1
            || msg.getData1() == 4
            || (msg.getData1() >= 64 && msg.getData1() <= 69)
            || msg.getData1() == 84
            || (msg.getData1() >= 91 && msg.getData1() <= 95)
        );
    }
}
