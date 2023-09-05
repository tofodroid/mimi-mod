package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import io.github.tofodroid.mods.mimi.common.midi.ATransmitterManager;
import net.minecraft.client.Minecraft;

public class EnderTransmitterManager extends ATransmitterManager {
    private Boolean shuffled = null;
    private LoopMode loopMode = null;
    private SourceMode sourceMode = null;
    private FavoriteMode favoriteMode = null;
    private UUID playerId = null;

    // TODO - Migrate to SaveData
    private ArrayList<UUID> favoriteSongs = new ArrayList<>();

    @Override
    @SuppressWarnings({"null", "resource"})
    public UUID transmitterId() {
        if(playerId == null) { 
            this.playerId = Minecraft.getInstance().player.getUUID();
        }
        return this.playerId;
    }

    @Override
    public Boolean isShuffled() {
        if(this.shuffled == null) {
            this.shuffled = false;
        }
        return this.shuffled;
    }

    @Override
    public LoopMode loopMode() {
        if(this.loopMode == null) {
            this.loopMode = LoopMode.NONE;
        }
        return this.loopMode;
    }

    @Override
    public void toggleShuffled() {
        this.shuffled = !this.shuffled;
    }

    @Override
    public void setLoopMode(LoopMode mode) {
        this.loopMode = mode;
    }

    @Override
    public SourceMode sourceMode() {
        if(this.sourceMode == null) {
            this.sourceMode = SourceMode.ALL;
        }
        return this.sourceMode;
    }

    @Override
    public void setSourceMode(SourceMode mode) {
        this.sourceMode = mode;
    }

    @Override
    public FavoriteMode favoriteMode() {
        if(this.favoriteMode == null) {
            this.favoriteMode = FavoriteMode.ALL;
        }
        return this.favoriteMode;
    }

    @Override
    public void setFavoriteMode(FavoriteMode mode) {
        this.favoriteMode = mode;
    }

    @Override
    public List<UUID> getFavoriteSongs() {
        return this.favoriteSongs;
    }

    @Override
    public Boolean isSongFavorite(UUID id) {
        return this.favoriteSongs.contains(id);
    }
    
    @Override
    public void toggleFavoriteSong(UUID id) {
        if(this.getFavoriteSongs().contains(id)) {
             this.getFavoriteSongs().remove(id);
        } else {
            this.getFavoriteSongs().add(id);
        }
    }
}
