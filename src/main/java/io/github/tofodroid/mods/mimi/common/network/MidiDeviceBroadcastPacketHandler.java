package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter.ATransmitterBroadcastProducer;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter.ServerTransmitterManager;
import net.minecraft.server.level.ServerPlayer;

public class MidiDeviceBroadcastPacketHandler {
    public static void handlePacketServer(final MidiDeviceBroadcastPacket message, ServerPlayer sender) {
        if(message != null) {
            ATransmitterBroadcastProducer musicPlayer = ServerTransmitterManager.getTransmitter(message.player);

            if(musicPlayer != null) {
                musicPlayer.broadcast(new BroadcastEvent(message.type, message.channel, message.note, message.velocity, message.player, sender.getLevel().dimension(), message.pos, 16, message.noteServerTime));
            }
        }
    }

    public static void handlePacketClient(final MidiDeviceBroadcastPacket message) {
        MIMIMod.LOGGER.warn("Client received unexpected MidiDeviceBroadcastPacket!");
    }
}
