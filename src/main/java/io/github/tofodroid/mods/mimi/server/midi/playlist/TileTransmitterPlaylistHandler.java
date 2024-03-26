package io.github.tofodroid.mods.mimi.server.midi.playlist;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;
import net.minecraft.world.item.ItemStack;

public class TileTransmitterPlaylistHandler extends APlaylistHandler {
    private TileTransmitter tile;
    private PlaylistData data;

    public TileTransmitterPlaylistHandler(TileTransmitter tile) {
        super(tile.getUUID());
        this.tile = tile;
        this.refreshData();
    }

    @Override
    public ArrayList<UUID> getFavoriteSongs() {
        return data.favoriteSongs;
    }

    @Override
    public LoopMode getLoopMode() {
        return data.loopMode;
    }

    @Override
    public FavoriteMode getFavoriteMode() {
        return data.favoriteMode;
    }

    @Override
    public SourceMode getSourceMode() {
        return SourceMode.SERVER;
    }

    @Override
    public Boolean getIsShuffled() {
        return data.isShuffled;
    }

    @Override
    protected void setFavoriteSongs(ArrayList<UUID> favorites) {
        data.favoriteSongs = favorites;
        this.saveData();
    }

    @Override
    protected void setLoopMode(LoopMode mode) {
        data.loopMode = mode;
        this.saveData();
    }

    @Override
    protected void setFavoriteMode(FavoriteMode mode) {
        data.favoriteMode = mode;
        this.saveData();
    }

    @Override
    protected void setSourceMode(SourceMode mode) {
        // No-op
    }

    @Override
    protected void setIsShuffled(Boolean shuffle) {
        data.isShuffled = shuffle;

        if(shuffle) {
            data.shuffleSeed = Math.abs(new Random().nextInt());
        } else {
            data.shuffleSeed = 0;
        }

        this.saveData();
    }

    @Override
    public UUID getClientSourceId() {
        return null;
    }

    @SuppressWarnings("null")
    protected void saveData() {
        ItemStack sourceStack = tile.getSourceStack();
        PlaylistData.writeToTag(data, sourceStack.getOrCreateTag());
        tile.setSourceStack(sourceStack);
        tile.getLevel().sendBlockUpdated(tile.getBlockPos(), tile.getBlockState(), tile.getBlockState(), 2);
        this.refreshData();
    }
    
    protected void refreshData() {
        this.data = PlaylistData.loadFromTag(tile.getSourceStack().getOrCreateTag());
    }

    @Override
    protected Integer getShuffleSeed() {
        return this.data.shuffleSeed;
    }
}
