package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.handler.codec.DecoderException;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.midi.AMusicPlayerPlaylistHandler.FavoriteMode;
import io.github.tofodroid.mods.mimi.server.midi.AMusicPlayerPlaylistHandler.LoopMode;
import io.github.tofodroid.mods.mimi.server.midi.AMusicPlayerPlaylistHandler.SourceMode;

public class ServerMusicPlayerStatusPacket {
    private static final UUID NO_FILE_ID = new UUID(0,0);

    public final UUID musicPlayerId;
    public final UUID fileId;
    public final Integer fileIndex;
    public final Boolean isFileFavorite;
    public final byte[] channelMapping;
    public final Integer songLengthSeconds;
    public final Integer songPositionSeconds;
    public final Boolean isPlaying;
    public final Boolean isLoadFailed;
    public final Boolean isLoading;
    public final Boolean isShuffled;
    public final LoopMode loopMode;
    public final FavoriteMode favoriteMode;
    public final SourceMode sourceMode;
    
    public ServerMusicPlayerStatusPacket(UUID musicPlayerId) {
        this.musicPlayerId = musicPlayerId;
        this.fileId = null;
        this.fileIndex = null;
        this.isFileFavorite = false;
        this.channelMapping = null;
        this.songLengthSeconds = null;
        this.songPositionSeconds = null;
        this.isPlaying = false;
        this.isLoadFailed = false;
        this.isLoading = false;
        this.isShuffled = false;
        this.loopMode = LoopMode.ALL;
        this.favoriteMode = FavoriteMode.ALL;
        this.sourceMode = SourceMode.ALL;
    }

    public ServerMusicPlayerStatusPacket(UUID musicPlayerId, UUID fileId, Integer fileIndex, Boolean isFileFavorite, byte[] channelMapping, Integer songLengthSeconds, Integer songPositionSeconds, Boolean isPlaying, Boolean isLoadFailed, Boolean isLoading, Boolean isShuffled, LoopMode loopMode, FavoriteMode favoriteMode, SourceMode sourceMode) {
        this.musicPlayerId = musicPlayerId;
        this.fileId = fileId;
        this.fileIndex = fileIndex;
        this.isFileFavorite = isFileFavorite;
        this.channelMapping = channelMapping;
        this.songLengthSeconds = songLengthSeconds;
        this.songPositionSeconds = songPositionSeconds;
        this.isPlaying = isPlaying;
        this.isLoadFailed = isLoadFailed;
        this.isLoading = isLoading;
        this.isShuffled = isShuffled;
        this.loopMode = loopMode;
        this.favoriteMode = favoriteMode;
        this.sourceMode = sourceMode;
    }
    
    public static ServerMusicPlayerStatusPacket decodePacket(FriendlyByteBuf buf) {
        try {       
            UUID musicPlayerId = buf.readUUID();
            UUID fileId = buf.readUUID();
            if(fileId.toString().equals(NO_FILE_ID.toString())) {
                fileId = null;
            }
            Integer fileIndex = Integer.valueOf(buf.readByte());
            if(fileIndex < 0) {
                fileIndex = null;
            }
            Boolean isFileFavorite = buf.readBoolean();
            byte[] channelMapping = buf.readBoolean() ? buf.readByteArray(16) : null;
            Integer songLengthSeconds = buf.readInt();
            if(songLengthSeconds < 0) {
                songLengthSeconds = null;
            }
            Integer songPositionSeconds = buf.readInt();
            if(songPositionSeconds < 0) {
                songPositionSeconds = null;
            }
            Boolean isPlaying = buf.readBoolean();
            Boolean isLoadFailed = buf.readBoolean();
            Boolean isLoading = buf.readBoolean();
            Boolean isShuffled = buf.readBoolean();
            LoopMode loopMode = LoopMode.values()[buf.readByte()];
            FavoriteMode favoritMode = FavoriteMode.values()[buf.readByte()];
            SourceMode sourceMode = SourceMode.values()[buf.readByte()];
            
            return new ServerMusicPlayerStatusPacket(musicPlayerId, fileId, fileIndex, isFileFavorite, channelMapping, songLengthSeconds, songPositionSeconds, isPlaying, isLoadFailed, isLoading, isShuffled, loopMode, favoritMode, sourceMode);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerMusicPlayerStatusPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("ServerMusicPlayerStatusPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerMusicPlayerStatusPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.musicPlayerId);
        buf.writeUUID(pkt.fileId != null ? pkt.fileId : NO_FILE_ID);
        buf.writeByte(pkt.fileIndex != null ? pkt.fileIndex : -1);
        buf.writeBoolean(pkt.isFileFavorite);

        if(pkt.channelMapping != null) {
            buf.writeBoolean(true);
            buf.writeByteArray(pkt.channelMapping);
        } else {
            buf.writeBoolean(false);
        }

        buf.writeInt(pkt.songLengthSeconds != null ? pkt.songLengthSeconds : -1);
        buf.writeInt(pkt.songPositionSeconds != null ? pkt.songPositionSeconds : -1);
        buf.writeBoolean(pkt.isPlaying);
        buf.writeBoolean(pkt.isLoadFailed);
        buf.writeBoolean(pkt.isLoading);
        buf.writeBoolean(pkt.isShuffled);
        buf.writeByte(pkt.loopMode.ordinal());
        buf.writeByte(pkt.favoriteMode.ordinal());
        buf.writeByte( pkt.sourceMode.ordinal());
    }
}
