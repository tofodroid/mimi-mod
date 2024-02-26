package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.midi.ServerMidiManager;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import net.minecraft.server.level.ServerPlayer;

public class ClientMidiListPacketHandler {
    public static void handlePacketClient(final ClientMidiListPacket message) {
        MIMIMod.getProxy().clientMidiFiles().refresh(true);
        NetworkProxy.sendToServer(new ClientMidiListPacket(MIMIMod.getProxy().clientMidiFiles().getSortedSongInfos()));
    }
    
    public static void handlePacketServer(final ClientMidiListPacket message, ServerPlayer sender) {
        ServerMidiManager.setCacheInfosForSource(sender.getUUID(), message.infos);
        ServerMidiManager.refreshServerSongs(false);
        ServerMusicTransmitterManager.refreshSongs(sender.getUUID());
        ServerMusicPlayerSongListPacket packet = ServerMusicTransmitterManager.createListPacket(sender.getUUID());

        if(packet != null) {
            NetworkProxy.sendToPlayer(sender, packet);
        }
    }
}
