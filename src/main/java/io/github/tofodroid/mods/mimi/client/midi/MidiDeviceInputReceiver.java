package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiInputReceiver;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class MidiDeviceInputReceiver extends MidiInputReceiver {
    @Override
    @SuppressWarnings("resource")
    protected void handleMessage(ShortMessage message) {
        Player player = Minecraft.getInstance().player;

        if(player != null && MIMIMod.proxy.isClient()) {
            ((ClientProxy)MIMIMod.proxy).getMidiInput().inputDeviceManager.getLocalInstrumentsForMidiDevice(player, Integer.valueOf(message.getChannel()).byteValue()).forEach(instrumentStack -> {
                Byte instrumentId = InstrumentDataUtils.getInstrumentId(instrumentStack);
                if(isNoteOnMessage(message)) {
                    handleMidiNoteOn(Integer.valueOf(message.getChannel()).byteValue(), instrumentId, message.getMessage()[1], InstrumentDataUtils.applyVolume(instrumentStack, message.getMessage()[2]), player);
                } else if(isNoteOffMessage(message)) {
                    handleMidiNoteOff(Integer.valueOf(message.getChannel()).byteValue(), instrumentId, message.getMessage()[1], player);
                } else if(isAllNotesOffMessage(message)) {
                    handleAllNotesOff(Integer.valueOf(message.getChannel()).byteValue(), instrumentId, player);
                } else if(isSupportedControlMessage(message)) {
                    handleControlMessage(Integer.valueOf(message.getChannel()).byteValue(), instrumentId, message.getMessage()[1], message.getMessage()[2], player);
                }
            });
        }
    }
    
    public void handleMidiNoteOn(Byte channel, Byte instrument, Byte midiNote, Byte velocity, Player player) {
        if(MIMIMod.proxy.isClient()) {
            MidiNotePacket packet = MidiNotePacket.createNotePacket(midiNote, velocity, instrument, player.getUUID(), player.getOnPos());
            NetworkManager.NOTE_CHANNEL.sendToServer(packet);
            ((ClientProxy)MIMIMod.proxy).getMidiSynth().handleLocalPacketInstant(packet);
        }
    }
    
    public void handleMidiNoteOff(Byte channel, Byte instrument, Byte midiNote, Player player) {
        if(MIMIMod.proxy.isClient()) {
            MidiNotePacket packet = MidiNotePacket.createNotePacket(midiNote, Integer.valueOf(0).byteValue(), instrument, player.getUUID(), player.getOnPos());
            NetworkManager.NOTE_CHANNEL.sendToServer(packet);
            ((ClientProxy)MIMIMod.proxy).getMidiSynth().handleLocalPacketInstant(packet);
        }
    }

    public void handleAllNotesOff(Byte channel, Byte instrument, Player player) {
        if(MIMIMod.proxy.isClient()) {
            MidiNotePacket packet =MidiNotePacket.createAllNotesOffPacket(instrument, player.getUUID(), player.getOnPos());
            NetworkManager.NOTE_CHANNEL.sendToServer(packet);
            ((ClientProxy)MIMIMod.proxy).getMidiSynth().handleLocalPacketInstant(packet);
        }
    }
    
    public void handleControlMessage(Byte channel, Byte instrument, Byte controller, Byte value, Player player) {
        if(MIMIMod.proxy.isClient()) {
            MidiNotePacket packet =MidiNotePacket.createControlPacket(controller, value, instrument, player.getUUID(), player.getOnPos());
            NetworkManager.NOTE_CHANNEL.sendToServer(packet);
            ((ClientProxy)MIMIMod.proxy).getMidiSynth().handleLocalPacketInstant(packet);
        }
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
