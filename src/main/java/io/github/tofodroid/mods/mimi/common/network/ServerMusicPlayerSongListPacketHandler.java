package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.client.gui.GuiTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.AServerMusicTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class ServerMusicPlayerSongListPacketHandler {
    public static void handlePacket(final ServerMusicPlayerSongListPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final ServerMusicPlayerSongListPacket message, ServerPlayer sender) {
        AServerMusicTransmitter player = ServerMusicTransmitterManager.getMusicPlayer(message.musicPlayerId);

        if(player != null) {
            player.refreshSongs();
            NetworkManager.INFO_CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new ServerMusicPlayerSongListPacket(message.musicPlayerId, player.getCurrentSongsSorted(), player.getCurrentFavoriteIndicies()));
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings({"resource", "null"})
    public static void handlePacketClient(final ServerMusicPlayerSongListPacket message) {
        if(Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof GuiTransmitter) {
            ((GuiTransmitter)Minecraft.getInstance().screen).handleMusicplayerSongListPacket(message);
        }
    }
}
