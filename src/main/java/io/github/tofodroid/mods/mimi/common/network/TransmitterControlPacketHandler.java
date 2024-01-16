package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.AServerMusicTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class TransmitterControlPacketHandler {
    public static void handlePacket(final TransmitterControlPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final TransmitterControlPacket message, ServerPlayer sender) {
        AServerMusicTransmitter musicPlayer = ServerMusicTransmitterManager.getMusicPlayer(message.transmitterId);
        Boolean shouldRefreshSongs = false;

        if(musicPlayer != null) {
            switch(message.control) {
                case PLAY:
                    musicPlayer.play();
                    break;
                case PAUSE:
                    musicPlayer.pause();
                    break;
                case STOP:
                    musicPlayer.stop();
                    break;
                case RESTART:
                    musicPlayer.stop();
                    musicPlayer.play();
                    break;
                case SEEK:
                    //musicPlayer.seek(message.transmitterId, message.controlData.get());
                    break;
                case PREV:
                    musicPlayer.previous();
                    break;
                case NEXT:
                    musicPlayer.next();
                    break;
                case SHUFFLE:
                    musicPlayer.toggleShuffled();
                    shouldRefreshSongs = true;
                    break;
                case LOOP_M:
                    musicPlayer.cycleLoopMode();
                    break;
                case FAVE_M:
                    musicPlayer.cycleFavoriteMode();
                    shouldRefreshSongs = true;
                    break;
                case SOURCE_M:
                    musicPlayer.cycleSourceMode();
                    shouldRefreshSongs = true;
                    break;
                case MARKFAVE:
                    musicPlayer.toggleSongFavorite();
                    shouldRefreshSongs = true;
                    break;
                default:
                    break;
            }

            sendResponsePackets(shouldRefreshSongs, message.transmitterId, sender, musicPlayer);
        }
    }

    public static void sendResponsePackets(Boolean songListPacket, UUID musicPlayerId, ServerPlayer sender, AServerMusicTransmitter player) {
        NetworkProxy.sendToPlayer(sender, player.getStatus());

        if(songListPacket) {
            NetworkProxy.sendToPlayer(sender, new ServerMusicPlayerSongListPacket(musicPlayerId, player.getCurrentSongsSorted(), player.getCurrentFavoriteIndicies()));
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void handlePacketClient(final TransmitterControlPacket message) {
        MIMIMod.LOGGER.warn("Client received unexpected TransmitterControlPacket!");
    }
}
