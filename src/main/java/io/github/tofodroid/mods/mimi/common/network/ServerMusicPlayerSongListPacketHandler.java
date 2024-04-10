package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.client.gui.GuiTransmitterBlock;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.ServerTransmitterManager;
import io.github.tofodroid.mods.mimi.server.midi.ServerMidiManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public class ServerMusicPlayerSongListPacketHandler {   
    public static void handlePacketServer(final ServerMusicPlayerSongListPacket message, ServerPlayer sender) {
        ServerMidiManager.refreshServerSongs(false);
        ServerTransmitterManager.refreshSongs(message.musicPlayerId);
        ServerMusicPlayerSongListPacket packet = ServerTransmitterManager.createListPacket(message.musicPlayerId);

        if(packet != null) {
            NetworkProxy.sendToPlayer(sender, packet);
        }
    }

    @SuppressWarnings({"resource"})
    public static void handlePacketClient(final ServerMusicPlayerSongListPacket message) {
        if(Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof GuiTransmitterBlock) {
            ((GuiTransmitterBlock)Minecraft.getInstance().screen).handleMusicplayerSongListPacket(message);
        }
    }
}
