package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.client.gui.GuiTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.ServerMidiManager;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.AServerMusicTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public class ServerMusicPlayerSongListPacketHandler {   
    public static void handlePacketServer(final ServerMusicPlayerSongListPacket message, ServerPlayer sender) {
        AServerMusicTransmitter player = ServerMusicTransmitterManager.getMusicPlayer(message.musicPlayerId);

        if(player != null) {
            ServerMidiManager.refreshServerSongs(false);
            player.refreshSongs();
            NetworkProxy.sendToPlayer(sender, new ServerMusicPlayerSongListPacket(message.musicPlayerId, player.getCurrentSongsSorted(), player.getCurrentFavoriteIndicies()));
        }
    }

    @SuppressWarnings({"resource"})
    public static void handlePacketClient(final ServerMusicPlayerSongListPacket message) {
        if(Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof GuiTransmitter) {
            ((GuiTransmitter)Minecraft.getInstance().screen).handleMusicplayerSongListPacket(message);
        }
    }
}
