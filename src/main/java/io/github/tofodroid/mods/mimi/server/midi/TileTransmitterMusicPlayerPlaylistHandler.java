package io.github.tofodroid.mods.mimi.server.midi;

import java.util.ArrayList;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;

public class TileTransmitterMusicPlayerPlaylistHandler extends AMusicPlayerPlaylistHandler {

    private TileTransmitter tile;

    public TileTransmitterMusicPlayerPlaylistHandler(TileTransmitter tile) {
        super(tile.getUUID());
        this.tile = tile;
    }

    @Override
    public ArrayList<UUID> getFavoriteSongs() {
        return tile.getFavoriteSongs();
    }

    @Override
    public LoopMode getLoopMode() {
        return tile.getLoopMode();
    }

    @Override
    public FavoriteMode getFavoriteMode() {
        return tile.getFavoriteMode();
    }

    @Override
    public SourceMode getSourceMode() {
        return SourceMode.SERVER;
    }

    @Override
    public Boolean getIsShuffled() {
        return tile.getIsShuffled();
    }

    @Override
    protected void setFavoriteSongs(ArrayList<UUID> favorites) {
        tile.setFavoriteSongs(favorites);
    }

    @Override
    protected void setLoopMode(LoopMode mode) {
        tile.setLoopMode(mode);
    }

    @Override
    protected void setFavoriteMode(FavoriteMode mode) {
        tile.setFavoriteMode(mode);
    }

    @Override
    protected void setSourceMode(SourceMode mode) {
        // No-op
    }

    @Override
    protected void setIsShuffled(Boolean shuffle) {
        tile.setIsShuffled(shuffle);
    }

    @Override
    public UUID getClientSourceId() {
        return null;
    }
    
}
