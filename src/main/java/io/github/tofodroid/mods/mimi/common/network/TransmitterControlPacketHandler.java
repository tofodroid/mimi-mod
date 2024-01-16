package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.AServerMusicTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import net.minecraft.server.level.ServerPlayer;

public class TransmitterControlPacketHandler {    
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

    public static void handlePacketClient(final TransmitterControlPacket message) {
        MIMIMod.LOGGER.warn("Client received unexpected TransmitterControlPacket!");
    }
}
