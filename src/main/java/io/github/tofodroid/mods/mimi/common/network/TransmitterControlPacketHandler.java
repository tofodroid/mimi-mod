package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import net.minecraft.server.level.ServerPlayer;

public class TransmitterControlPacketHandler {    
    public static void handlePacketServer(final TransmitterControlPacket message, ServerPlayer sender) {
        sendResponsePackets(ServerMusicTransmitterManager.handleCommand(message), message.transmitterId, sender);
    }

    public static void sendResponsePackets(Boolean songListPacket, UUID musicPlayerId, ServerPlayer sender) {
        ServerMusicPlayerStatusPacket response = ServerMusicTransmitterManager.createStatusPacket(musicPlayerId);

        if(response != null) {
            NetworkProxy.sendToPlayer(sender, response);

            if(songListPacket) {
                NetworkProxy.sendToPlayer(sender, ServerMusicTransmitterManager.createListPacket(musicPlayerId));
            }
        }
    }

    public static void handlePacketClient(final TransmitterControlPacket message) {
        MIMIMod.LOGGER.warn("Client received unexpected TransmitterControlPacket!");
    }
}
