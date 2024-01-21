package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.server.midi.ServerMidiManager;

public abstract class APlaylistHandler {
    public enum LoopMode {
        ALL,
        SINGLE,
        NONE;
    }
    public enum FavoriteMode {
        ALL,
        FAVORITE,
        NOT_FAVORITE;
    }
    public enum SourceMode {
        ALL,
        CLIENT,
        SERVER;
    }
    
    protected BasicMidiInfo selectedSongInfo = null;
    protected Integer selectedDisplayIndex = -1;
    protected ArrayList<BasicMidiInfo> filteredSongs = null;
    protected final UUID musicPlayerId;

    public abstract ArrayList<UUID> getFavoriteSongs();
    public abstract LoopMode getLoopMode();
    public abstract FavoriteMode getFavoriteMode();
    public abstract SourceMode getSourceMode();
    public abstract Boolean getIsShuffled();
    public abstract UUID getClientSourceId();
    protected abstract Integer getShuffleSeed();
    protected abstract void setFavoriteSongs(ArrayList<UUID> songId);
    protected abstract void setLoopMode(LoopMode mode);
    protected abstract void setFavoriteMode(FavoriteMode mode);
    protected abstract void setSourceMode(SourceMode mode);
    protected abstract void setIsShuffled(Boolean shuffle);

    public APlaylistHandler(UUID musicPlayerId) {
        this.musicPlayerId = musicPlayerId;
    }

    protected ArrayList<BasicMidiInfo> getSourceSongsSorted(SourceMode source) {
        ArrayList<BasicMidiInfo> result = new ArrayList<>();

        if(source != SourceMode.SERVER && this.getClientSourceId() != null) {
            result.addAll(ServerMidiManager.getSortedMidiInfosForSourceId(this.getClientSourceId()));
        }
        
        if(source != SourceMode.CLIENT) {
            result.addAll(MIMIMod.getProxy().serverMidiFiles().getSortedSongInfos());
        }

        return result;
    }

    public LoopMode cycleLoopMode() {
        LoopMode mode = this.getLoopMode();

        if(mode == LoopMode.NONE) {
            this.setLoopMode(LoopMode.SINGLE);
        } else if(mode == LoopMode.SINGLE) {
            this.setLoopMode(LoopMode.ALL);
        } else if(mode == LoopMode.ALL) {
            this.setLoopMode(LoopMode.NONE);
        }

        return this.getLoopMode();
    }

    public FavoriteMode cycleFavoriteMode() {
        FavoriteMode mode = this.getFavoriteMode();

        if(mode == FavoriteMode.ALL) {
            this.setFavoriteMode(FavoriteMode.FAVORITE);
        } else if(mode == FavoriteMode.FAVORITE) {
            this.setFavoriteMode(FavoriteMode.NOT_FAVORITE);
        } else if(mode == FavoriteMode.NOT_FAVORITE) {
            this.setFavoriteMode(FavoriteMode.ALL);
        }

        this.refreshFilteredSongs();
        return this.getFavoriteMode();
    }

    public SourceMode cycleSourceMode() {
        SourceMode mode = this.getSourceMode();

        if(mode == SourceMode.ALL) {
            this.setSourceMode(SourceMode.SERVER);
        } else if(mode == SourceMode.SERVER) {
            this.setSourceMode(SourceMode.CLIENT);
        } else if(mode == SourceMode.CLIENT) {
            this.setSourceMode(SourceMode.ALL);
        }

        this.refreshFilteredSongs();
        return this.getSourceMode();
    }

    public Boolean toggleShuffled() {
        this.setIsShuffled(!getIsShuffled());
        this.refreshFilteredSongs();
        return this.getIsShuffled();
    }

    public Boolean toggleSongFavorite() {
        ArrayList<UUID> newFaves = this.getFavoriteSongs();
        Boolean isNowFavorite = false;

        if(this.getSelectedSongId() != null) {
            if(this.getFavoriteSongs().contains(this.getSelectedSongId())) {
                newFaves.remove(this.getSelectedSongId());
            } else {
                newFaves.add(this.getSelectedSongId());
                isNowFavorite = true;
            }

            this.setFavoriteSongs(newFaves);
            this.refreshFilteredSongs();
        }

        return isNowFavorite;
    }

    public void refreshFilteredSongs() {
        ArrayList<BasicMidiInfo> allSongsSorted = this.getSourceSongsSorted(this.getSourceMode());
        ArrayList<UUID> favoriteSongs = this.getFavoriteSongs();
        if(this.getFavoriteMode() != FavoriteMode.ALL) {
            this.filteredSongs = new ArrayList<BasicMidiInfo>(allSongsSorted.stream()
                .filter((songInfo) -> favoriteSongs.contains(songInfo.fileId) == (this.getFavoriteMode() == FavoriteMode.FAVORITE)).collect(Collectors.toList())
            );
        } else {
            this.filteredSongs = new ArrayList<>(allSongsSorted);
        }

        if(this.getIsShuffled()) {
            Collections.shuffle(this.filteredSongs, new Random(this.getShuffleSeed()));
        }

        // Get new selected index
        Integer newSelectedIndex = null;

        if(this.getSelectedSongId() != null) {
            for(int i = 0; i < this.filteredSongs.size(); i++) {
                if(this.filteredSongs.get(i).fileId.toString().equals(this.getSelectedSongId().toString())) {
                    newSelectedIndex = i;
                }
            }
        }
        
        if(newSelectedIndex != null) {
            this.selectedDisplayIndex = newSelectedIndex;
        } else {
            this.selectDisplaySong(0);
        }
    }

    public Integer getFilteredSongCount() {
        return this.filteredSongs.size();
    }

    public Integer getSelectedDisplayIndex() {
        return this.selectedDisplayIndex != null ? this.selectedDisplayIndex : -1;
    }

    public UUID getSelectedSongId() {
        return this.selectedSongInfo != null ? this.selectedSongInfo.fileId : null;
    }

    public BasicMidiInfo getSelectedSongInfo() {
        return this.selectedSongInfo;
    }

    public Boolean getFilterHasSongs() {
        return getFilteredSongCount() > 0;
    }

    public ArrayList<BasicMidiInfo> getSortedFilteredSongs() {
        return this.filteredSongs;
    }

    public void selectNextSong() {
        if(getFilterHasSongs()) {
            Integer newSongIndex = 0;

            if(getSelectedDisplayIndex() < (getFilteredSongCount() - 1)) {
                newSongIndex = getSelectedDisplayIndex() + 1;
            }
            
            selectDisplaySong(newSongIndex);
        }
    }

    public void selectPreviousSong() {
        if(getFilterHasSongs()) {
            Integer newSongIndex = getFilteredSongCount() - 1;

            if(getSelectedDisplayIndex() > 0) {
                newSongIndex = getSelectedDisplayIndex() - 1;
            }
            
            selectDisplaySong(newSongIndex);
        }
    }

    public void selectDisplaySong(Integer displaySongIndex) {
        if(displaySongIndex >= 0 && displaySongIndex < getFilteredSongCount()) {
            this.selectedDisplayIndex = displaySongIndex;
            this.selectedSongInfo = this.filteredSongs.get(displaySongIndex);
        } else {
            this.selectedDisplayIndex = null;
            this.selectedSongInfo = null;
        }
        ServerMusicTransmitterManager.onSelectedSongChange(this.musicPlayerId, this.selectedSongInfo);
    }
}
