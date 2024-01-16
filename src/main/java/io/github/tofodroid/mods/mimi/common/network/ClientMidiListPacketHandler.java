package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.midi.ServerMidiManager;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.AServerMusicTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import net.minecraft.server.level.ServerPlayer;

public class ClientMidiListPacketHandler {
    public static void handlePacketClient(final ClientMidiListPacket message) {
        MIMIMod.proxy.clientMidiFiles().refresh();
        NetworkProxy.sendToServer(new ClientMidiListPacket(false, MIMIMod.proxy.clientMidiFiles().getSortedSongInfos()));
    }
    
    public static void handlePacketServer(final ClientMidiListPacket message, ServerPlayer sender) {
        ServerMidiManager.setCacheInfosForSource(sender.getUUID(), message.infos);
        AServerMusicTransmitter player = ServerMusicTransmitterManager.getMusicPlayer(sender.getUUID());

        if(message.doUpdateServerFileList) {
            MIMIMod.proxy.serverMidiFiles().refresh();
        }

        if(player != null) {
            player.refreshSongs();
        } else {
            ServerMusicTransmitterManager.createTransmitter(sender);
            player = ServerMusicTransmitterManager.getMusicPlayer(sender.getUUID());
        }

        NetworkProxy.sendToPlayer(sender, new ServerMusicPlayerSongListPacket(sender.getUUID(), player.getCurrentSongsSorted(), player.getCurrentFavoriteIndicies()));
    }
}
