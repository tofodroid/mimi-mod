package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiEvent;
import io.github.tofodroid.mods.mimi.common.midi.MidiEventType;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter.ATransmitterBroadcastProducer;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter.ServerTransmitterManager;
import net.minecraft.server.level.ServerPlayer;

public class MidiDeviceBroadcastPacketHandler {
    public static void handlePacketServer(final MidiDeviceBroadcastPacket message, ServerPlayer sender) {
        if(message != null) {
            ATransmitterBroadcastProducer musicPlayer = ServerTransmitterManager.getTransmitter(message.player);

            if(musicPlayer != null) {
                if(message.isNoteOnPacket()) {
                    musicPlayer.broadcast(new BasicMidiEvent(MidiEventType.NOTE_ON, message.channel, message.note, message.velocity, message.noteServerTime));
                } else if(message.isNoteOffPacket()) {
                    musicPlayer.broadcast(new BasicMidiEvent(MidiEventType.NOTE_OFF, message.channel, message.note, message.velocity, message.noteServerTime));                    
                } else if(message.isAllNotesOffPacket()) {
                    musicPlayer.broadcast(new BasicMidiEvent(MidiEventType.ALL_NOTES_OFF, message.channel, message.note, message.velocity, message.noteServerTime));
                } else if(message.isControlPacket()) {
                    // Not yet supported
                }
            }
        }
    }

    public static void handlePacketClient(final MidiDeviceBroadcastPacket message) {
        MIMIMod.LOGGER.warn("Client received unexpected MidiDeviceBroadcastPacket!");
    }
}
