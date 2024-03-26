package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.ServerTransmitterManager;
import io.github.tofodroid.mods.mimi.server.midi.ServerMidiManager;
import net.minecraft.server.level.ServerPlayer;

public class ClientMidiListPacketHandler {
    public static void handlePacketClient(final ClientMidiListPacket message) {
        MIMIMod.getProxy().clientMidiFiles().refresh(true);
        NetworkProxy.sendToServer(new ClientMidiListPacket(MIMIMod.getProxy().clientMidiFiles().getSortedSongInfos()));
    }
    
    public static void handlePacketServer(final ClientMidiListPacket message, ServerPlayer sender) {
        ServerMidiManager.setCacheInfosForSource(sender.getUUID(), message.infos);
        ServerMidiManager.refreshServerSongs(false);
        ServerTransmitterManager.refreshSongs(sender.getUUID());
        ServerMusicPlayerSongListPacket packet = ServerTransmitterManager.createListPacket(sender.getUUID());

        if(packet != null) {
            NetworkProxy.sendToPlayer(sender, packet);
        }
    }
}
