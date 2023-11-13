package io.github.tofodroid.mods.mimi.server.midi;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;

public class PlayerMusicPlayerPlaylistHandler extends AMusicPlayerPlaylistHandler {
    public PlayerMusicPlayerPlaylistHandler(ServerPlayer player) {
        super(player.getUUID());
    }

    // TODO: Store on world data?
    ArrayList<UUID> favoriteSongs = new ArrayList<>();
    LoopMode loopMode = LoopMode.NONE;
    FavoriteMode favoriteMode = FavoriteMode.ALL;
    SourceMode sourceMode = SourceMode.ALL;
    Boolean isShuffled = false;

    @Override
    public ArrayList<UUID> getFavoriteSongs() {
        return this.favoriteSongs;
    }

    @Override
    public LoopMode getLoopMode() {
        return this.loopMode;
    }

    @Override
    public FavoriteMode getFavoriteMode() {
        return this.favoriteMode;
    }

    @Override
    public SourceMode getSourceMode() {
        return this.sourceMode;
    }

    @Override
    public Boolean getIsShuffled() {
        return this.isShuffled;
    }

    @Override
    protected void setFavoriteSongs(ArrayList<UUID> favorites) {
        this.favoriteSongs = favorites;
    }

    @Override
    protected void setLoopMode(LoopMode mode) {
        this.loopMode = mode;
    }

    @Override
    protected void setFavoriteMode(FavoriteMode mode) {
        this.favoriteMode = mode;
    }

    @Override
    protected void setSourceMode(SourceMode mode) {
        this.sourceMode = mode;
    }

    @Override
    protected void setIsShuffled(Boolean shuffle) {
        this.isShuffled = shuffle;
    }

    @Override
    public UUID getClientSourceId() {
        return this.musicPlayerId;
    }
    
}
