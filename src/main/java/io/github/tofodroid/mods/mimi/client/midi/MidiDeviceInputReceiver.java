package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class MidiDeviceInputReceiver implements Receiver {
    private volatile boolean open = true;

    public synchronized void send(MidiMessage msg, long timeStamp) {
        if(open && msg instanceof ShortMessage) {
            handleMessage((ShortMessage)msg);
        }
    }

    public void close() {
        open = false;
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

    @SuppressWarnings("resource")
    protected void handleMessage(ShortMessage message) {
        Player player = Minecraft.getInstance().player;

        if(player != null && MIMIMod.getProxy().isClient()) {
            ((ClientProxy)MIMIMod.getProxy()).getMidiData().inputDeviceManager.getLocalInstrumentsForMidiDevice(player, Integer.valueOf(message.getChannel()).byteValue()).forEach(instrumentStack -> {
                Byte instrumentId = InstrumentDataUtils.getInstrumentId(instrumentStack.getRight());
                if(isNoteOnMessage(message)) {
                    handleMidiNoteOn(Integer.valueOf(message.getChannel()).byteValue(), instrumentId, message.getMessage()[1], InstrumentDataUtils.applyVolume(instrumentStack.getRight(), message.getMessage()[2]), player, instrumentStack.getLeft());
                } else if(isNoteOffMessage(message)) {
                    handleMidiNoteOff(Integer.valueOf(message.getChannel()).byteValue(), instrumentId, message.getMessage()[1], player, instrumentStack.getLeft());
                } else if(isAllNotesOffMessage(message)) {
                    handleAllNotesOff(Integer.valueOf(message.getChannel()).byteValue(), instrumentId, player, instrumentStack.getLeft());
                } else if(isSupportedControlMessage(message)) {
                    handleControlMessage(Integer.valueOf(message.getChannel()).byteValue(), instrumentId, message.getMessage()[1], message.getMessage()[2], player, instrumentStack.getLeft());
                }
            });
        }
    }

    public void handleMidiNoteOn(Byte channel, Byte instrument, Byte midiNote, Byte velocity, Player player, InteractionHand handIn) {
        if(MIMIMod.getProxy().isClient()) {
            MidiNotePacket packet = MidiNotePacket.createNotePacket(midiNote, velocity, instrument, player.getUUID(), player.getOnPos(), handIn);
            NetworkProxy.sendToServer(packet);
            ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().handleLocalPacketInstant(packet);
        }
    }

    public void handleMidiNoteOff(Byte channel, Byte instrument, Byte midiNote, Player player, InteractionHand handIn) {
        if(MIMIMod.getProxy().isClient()) {
            MidiNotePacket packet = MidiNotePacket.createNotePacket(midiNote, Integer.valueOf(0).byteValue(), instrument, player.getUUID(), player.getOnPos(), handIn);
            NetworkProxy.sendToServer(packet);
            ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().handleLocalPacketInstant(packet);
        }
    }

    public void handleAllNotesOff(Byte channel, Byte instrument, Player player, InteractionHand handIn) {
        if(MIMIMod.getProxy().isClient()) {
            MidiNotePacket packet = MidiNotePacket.createAllNotesOffPacket(instrument, player.getUUID(), player.getOnPos(), handIn);
            NetworkProxy.sendToServer(packet);
            ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().handleLocalPacketInstant(packet);
        }
    }

    public void handleControlMessage(Byte channel, Byte instrument, Byte controller, Byte value, Player player, InteractionHand handIn) {
        if(MIMIMod.getProxy().isClient()) {
            MidiNotePacket packet =MidiNotePacket.createControlPacket(controller, value, instrument, player.getUUID(), player.getOnPos(), handIn);
            NetworkProxy.sendToServer(packet);
            ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().handleLocalPacketInstant(packet);
        }
    }

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
