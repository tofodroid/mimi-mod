package io.github.tofodroid.mods.mimi.server.midi.playlist;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.server.level.ServerPlayer;

public class PlayerPlaylistHandler extends APlaylistHandler {
    protected PlaylistData data;

    public PlayerPlaylistHandler(ServerPlayer player) {
        super(player.getUUID());

        // Load from world saved data
        this.data = player.getServer().overworld().getDataStorage().computeIfAbsent(PlaylistData::loadFromTag, PlaylistData::new, MIMIMod.MODID + "-ender-playlist-" + player.getUUID().toString());
        this.data.setDirty();
    }

    @Override
    public ArrayList<UUID> getFavoriteSongs() {
        return this.data.favoriteSongs;
    }

    @Override
    public LoopMode getLoopMode() {
        return this.data.loopMode;
    }

    @Override
    public FavoriteMode getFavoriteMode() {
        return this.data.favoriteMode;
    }

    @Override
    public SourceMode getSourceMode() {
        return this.data.sourceMode;
    }

    @Override
    public Boolean getIsShuffled() {
        return this.data.isShuffled;
    }

    @Override
    protected void setFavoriteSongs(ArrayList<UUID> favorites) {
        this.data.favoriteSongs = favorites;
        this.data.setDirty();
    }

    @Override
    protected void setLoopMode(LoopMode mode) {
        this.data.loopMode = mode;
        this.data.setDirty();
    }

    @Override
    protected void setFavoriteMode(FavoriteMode mode) {
        this.data.favoriteMode = mode;
        this.data.setDirty();
    }

    @Override
    protected void setSourceMode(SourceMode mode) {
        this.data.sourceMode = mode;
        this.data.setDirty();
    }

    @Override
    protected void setIsShuffled(Boolean shuffle) {
        this.data.isShuffled = shuffle;
        
        if(shuffle) {
            data.shuffleSeed = Math.abs(new Random().nextInt());
        } else {
            data.shuffleSeed = 0;
        }

        this.data.setDirty();
    }

    @Override
    public UUID getClientSourceId() {
        return this.musicPlayerId;
    }

    @Override
    protected Integer getShuffleSeed() {
        return this.data.shuffleSeed;
    }
}
