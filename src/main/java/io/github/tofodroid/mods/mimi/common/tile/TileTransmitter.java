package io.github.tofodroid.mods.mimi.common.tile;

import java.util.ArrayList;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.server.midi.AMusicPlayerPlaylistHandler.FavoriteMode;
import io.github.tofodroid.mods.mimi.server.midi.AMusicPlayerPlaylistHandler.LoopMode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class TileTransmitter extends AConfigurableMidiTile {
    public static final String LOOP_MODE_TAG = "loopMode";
    public static final String SHUFFLE_TAG = "shuffled";
    public static final String SONG_ORDER_TAG = "songOrder";

    private LoopMode loopMode;
    private FavoriteMode favoriteMode;
    private Boolean isShuffled;
    private ArrayList<UUID> favoriteSongs;

    public TileTransmitter(BlockPos pos, BlockState state) {
        super(ModTiles.INSTRUMENT, pos, state, 1);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
    }
    
    public UUID getUUID() {
        String idString = "tile-transmitter-" + this.getBlockPos().getX() + "-" + this.getBlockPos().getY() + "-" + this.getBlockPos().getZ();
        return UUID.nameUUIDFromBytes(idString.getBytes());
    }

    public LoopMode getLoopMode() {
        return this.loopMode;
    }

    public void setLoopMode(LoopMode mode) {
        this.loopMode = mode;
    }

    public FavoriteMode getFavoriteMode() {
        return this.favoriteMode;
    }

    public void setFavoriteMode(FavoriteMode mode) {
        this.favoriteMode = mode;
    }

    public Boolean getIsShuffled() {
        return this.isShuffled;
    }

    public void setIsShuffled(Boolean shuffled) {
        this.isShuffled = shuffled;
    }

    public ArrayList<UUID> getFavoriteSongs() {
        return this.favoriteSongs;
    }
    
    public void setFavoriteSongs(ArrayList<UUID> favoriteSongs) {
        this.favoriteSongs = favoriteSongs;
    }
}
