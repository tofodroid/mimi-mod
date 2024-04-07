package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.forge.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;

public class MidiDeviceInputReceiver implements Receiver {
    private volatile boolean open = true;

    public void send(MidiMessage msg, long timeStamp) {
        if(open && msg instanceof ShortMessage) {
            LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT).execute(() -> {
                handleMessage((ShortMessage)msg);
            });
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
        if(isNoteOnMessage(message)) {
            printDebugMessage("Recieved MIDI Input Device Note On - Channel: " + (message.getChannel() + 1) + ", Note: " + message.getMessage()[1] + ", Velocity: " + message.getMessage()[2]);
        } else if(isNoteOffMessage(message)) {
            printDebugMessage("Recieved MIDI Input Device Note Off - Channel: " + (message.getChannel() + 1) + ", Note: " + message.getMessage()[1]);
        } else if(isAllNotesOffMessage(message)) {
            printDebugMessage("Recieved MIDI Input Device All Notes Off - Channel: " + (message.getChannel() + 1));
        } else if(isSupportedControlMessage(message)) {
            printDebugMessage("Recieved MIDI Input Device Control Change - Channel: " + (message.getChannel() + 1) + ", Controller: " + message.getMessage()[1] + ", Value: " + message.getMessage()[2]);
        } else {
            printDebugMessage("Recieved Unsupported MIDI Input Device Message - Channel: " + (message.getChannel() + 1) + ", Data: " + message.getMessage().toString());
        }

        Player player = Minecraft.getInstance().player;

        if(player != null && MIMIMod.getProxy().isClient()) {
            ((ClientProxy)MIMIMod.getProxy()).getMidiData().inputDeviceManager.getLocalInstrumentsForMidiDevice(player, Integer.valueOf(message.getChannel()).byteValue()).forEach(instrumentStack -> {
                Byte instrumentId = MidiNbtDataUtils.getInstrumentId(instrumentStack.getRight());
                if(isNoteOnMessage(message)) {
                    handleMidiNoteOn(Integer.valueOf(message.getChannel()).byteValue(), instrumentId, message.getMessage()[1], MidiNbtDataUtils.applyVolume(instrumentStack.getRight(), message.getMessage()[2]), player, instrumentStack.getLeft());
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

    protected void printDebugMessage(String message) {
        if(ModConfigs.CLIENT.printDeDebugMessages.get()) {
            MIMIMod.LOGGER.info("Device Debug: " + message);
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
