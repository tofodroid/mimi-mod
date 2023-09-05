package io.github.tofodroid.mods.mimi.common.midi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.client.network.MidiUploadManager;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiListPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiUploadPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacket.CONTROL;

public abstract class ATransmitterManager {
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
        LOCAL,
        SERVER;
    }

    protected Map<UUID, MidiFileInfo> songMap = new HashMap<>();
    protected ArrayList<UUID> defaultSongOrder = new ArrayList<>();
    protected ArrayList<UUID> shuffleSongOrder = null;
    protected ArrayList<MidiFileInfo> displaySongs = new ArrayList<>();
    protected Boolean initialized = false;
    protected Integer selectedDisplayIndex = -1;
    protected UUID selectedSong = null;
    protected Boolean selectedSongHasLoaded = false;
    protected Boolean selectedSongIsLoading = false;
    protected Boolean selectedSongFavorite = false;

    protected ServerMusicPlayerStatusPacket lastMediaStatus = null;

    public void init() {
        if(!this.initialized) {
            refreshSongs();
            this.initialized = true;
        }
    }

    public void refreshSongs() {
        stop();
        clearSongs();
        this.startLoadServerSongsFromRemote();
    }

    public Boolean isSelectedSongFavorite() {
        return this.selectedSongFavorite;
    }

    private void refreshSelectedSongFavorite() {
        if(this.selectedSong != null) {
            this.selectedSongFavorite = this.isSongFavorite(this.selectedSong);
        }
    }

    // Media
    public void startRefreshMediaStatus() {
        if(this.songLoaded()) {
            NetworkManager.INFO_CHANNEL.sendToServer(ServerMusicPlayerStatusPacket.requestPacket());
        }
    }

    public void finishRefreshMediaStatus(ServerMusicPlayerStatusPacket response) {
        if(this.songLoaded()) {
            if(response.complete && (this.lastMediaStatus == null || !this.lastMediaStatus.complete)) {
                switch(loopMode()) {
                    case ALL:
                        this.selectNextSong();
                        break;
                    case SINGLE:
                        this.sendControlPacket(CONTROL.PLAY);
                        break;
                    default:
                    case NONE:
                        this.lastMediaStatus = response;
                        break;
                    
                }
            }

            this.lastMediaStatus = response;
        }
    }

    public ServerMusicPlayerStatusPacket mediaStatus() {
        if(this.lastMediaStatus != null && this.songLoaded()) {
            return this.lastMediaStatus;
        }
        return null;
    }

    public Boolean isPlaying() {
        if(this.mediaStatus() != null) {
            return this.mediaStatus().running;
        }
        return false;
    }

    public Integer getSongPositionSeconds() {
        if(this.mediaStatus() != null) {
            return this.mediaStatus().songPositionSeconds;
        }
        return 0;
    }

    public void sendControlPacket(CONTROL control) {
        NetworkManager.INFO_CHANNEL.sendToServer(new TransmitterControlPacket(transmitterId(), control));
    }

    public void sendControlPacket(CONTROL control, UUID songId) {
        NetworkManager.INFO_CHANNEL.sendToServer(new TransmitterControlPacket(transmitterId(), control, songId));
    }

    public void sendControlPacket(CONTROL control, Integer controlData) {
        NetworkManager.INFO_CHANNEL.sendToServer(new TransmitterControlPacket(transmitterId(), control, controlData));
    }

    // Song List
    public List<MidiFileInfo> getDisplaySongList() {
        return this.displaySongs;
    }

    public Integer getSelectedSongLength() {
        MidiFileInfo info = getSelectedSongInfo();

        if(info != null) {
            return info.songLength;
        } else {
            return 1;
        }
    }

    protected Integer getLoadedSongCount() {
        return this.songMap.size();
    }

    public Integer getDisplaySongCount() {
        return this.displaySongs.size();
    }

    public Integer getSelectedDisplayIndex() {
        return this.selectedDisplayIndex;
    }

    protected UUID getSelectedSong() {
        return this.selectedSong;
    }

    public MidiFileInfo getSelectedSongInfo() {
        UUID selectedSong = getSelectedSong();

        if(selectedSong != null) {
            return this.songMap.get(selectedSong);
        }

        return null;
    }

    public Boolean songLoading() {
        return this.selectedSongIsLoading;
    }

    public Boolean songLoaded() {
        return this.selectedSongHasLoaded;
    }

    public Boolean selectedSongIsLocal() {
        MidiFileInfo selectedInfo = this.getSelectedSongInfo();
        return selectedInfo != null && selectedInfo.local;
    }
    
    public Boolean getDisplayHasSongs() {
        return getDisplaySongCount() > 0;
    }

    public Integer selectNextSong() {
        if(getDisplayHasSongs()) {
            Integer newSongIndex = 0;

            if(getSelectedDisplayIndex() < (getDisplaySongCount() - 1)) {
                newSongIndex = getSelectedDisplayIndex() + 1;
            }
            
            selectDisplaySong(newSongIndex);
            return newSongIndex;
        }
        return -1;
    }

    public Integer selectPreviousSong() {
        if(getDisplayHasSongs()) {
            Integer newSongIndex = getDisplaySongCount() - 1;

            if(getSelectedDisplayIndex() > 0) {
                newSongIndex = getSelectedDisplayIndex() - 1;
            }
            
            selectDisplaySong(newSongIndex);
            return newSongIndex;
        }
        return -1;
    }

    public Boolean selectDisplaySong(Integer displaySongIndex) {
        if(displaySongIndex >= 0 && displaySongIndex < getDisplaySongCount()) {
            Boolean wasPlaying = this.isPlaying();
            this.stop();
            this.selectedDisplayIndex = displaySongIndex;
            this.selectedSongHasLoaded = false;
            this.selectedSong = this.displaySongs.get(displaySongIndex).toUUID();
            this.refreshSelectedSongFavorite();

            if(this.selectedSongIsLocal()) {
                if(wasPlaying || this.selectedSongIsLoading) {
                    this.startUploadSelectedLocalSongToServer();
                }
            } else {
                if(wasPlaying || this.selectedSongIsLoading) {
                    this.sendControlPacket(CONTROL.PLAY, this.selectedSong);
                    this.selectedSongHasLoaded = true;
                }
            }
            return true;
        }
        return false;
    }
    
    public void play() {
        if(!this.isPlaying() && this.getLoadedSongCount() > 0) {
            if(this.songLoaded()) {
                this.sendControlPacket(CONTROL.PLAY);
            } else {
                if(this.selectedSongIsLocal()) {
                    this.startUploadSelectedLocalSongToServer();
                } else {
                    this.sendControlPacket(CONTROL.PLAY, this.selectedSong);
                    this.selectedSongHasLoaded = true;
                }
            }
        }
    }

    public void pause() {
        this.sendControlPacket(CONTROL.PAUSE);
    }

    public void stop() {
        this.sendControlPacket(CONTROL.STOP);
    }

    public void seek(Integer seekToSeconds) {
        if(this.songLoaded()) {
            this.sendControlPacket(CONTROL.SEEK, seekToSeconds);
        }
    }

    public void clearSongs() {
        this.stop();

        if(this.isShuffled()) {
            this.toggleShuffle();
        }

        this.selectedSongHasLoaded = false;
        this.selectedSongIsLoading = false;
        this.selectedSong = null;
        this.selectedSongFavorite = false;
        this.songMap = new HashMap<>();
        this.displaySongs = new ArrayList<>();
        this.defaultSongOrder = new ArrayList<>();
    }
    
    public void startUploadSelectedLocalSongToServer() {
        if(this.selectedSong != null) {
            this.selectedSongIsLoading = true;
            MidiUploadManager.startUploadSequenceToServer(this.getSelectedSongInfo().getSequence());
        }
    }

    public void finishUploadSelectedLocalSongToServer(ServerMidiUploadPacket response) {
        this.selectedSongIsLoading = false;

        if(response.getResponseStatus() == ServerMidiUploadPacket.UPLOAD_SUCCESS) {
            this.sendControlPacket(CONTROL.PLAY);
            this.selectedSongHasLoaded = true;
        } else {
            MIMIMod.LOGGER.error("Selected song failed to upload to server.");
            this.selectedSongHasLoaded = false;
        }
    }

    public void startLoadServerSongsFromRemote() {
        NetworkManager.INFO_CHANNEL.sendToServer(new ServerMidiListPacket());
    }

    public Boolean finishLoadServerSongsFromRemote(ServerMidiListPacket response) {
        // TODO - Change
        MIMIMod.proxy.customMidiFiles().initFromAbsolutePath("D:/midi");
        
        // Merge local and server songs and sort by name
        ArrayList<MidiFileInfo> allFiles = new ArrayList<>(response.midiList);
        allFiles.addAll(MIMIMod.proxy.customMidiFiles().getAllSongs());
        allFiles.sort((midiA, midiB) -> {
            return midiA.file.getName().toLowerCase().trim().compareTo(midiB.file.getName().toLowerCase().trim());
        });

        // Build song map
        allFiles.forEach(midiFile -> {
            this.songMap.put(midiFile.toUUID(), midiFile);
            this.defaultSongOrder.add(midiFile.toUUID());
        });

        // Build display songs
        if(!this.songMap.isEmpty()) {
            this.buildDisplaySongList();

            // Select first song
            if(!this.displaySongs.isEmpty()) {
                this.selectDisplaySong(0);
                return true;
            }
        }

        return false;
    }

    protected void buildDisplaySongList() {
        this.displaySongs = new ArrayList<>();

        List<UUID> orderedSongs = this.isShuffled() ? this.shuffleSongOrder : this.defaultSongOrder;
        Integer newSelectedIndex = -1;
        
        for(UUID songId : orderedSongs) {
            MidiFileInfo fileInfo = this.songMap.get(songId);

            // Source Filter
            if(sourceMode() == SourceMode.LOCAL && !fileInfo.local) {
                continue;
            } else if(sourceMode() == SourceMode.SERVER && fileInfo.local) {
                continue;
            }

            // Favorite Filter
            Boolean isFavorite = this.isSongFavorite(songId);
            if(favoriteMode() == FavoriteMode.FAVORITE && !isFavorite) {
                continue;
            } else if(favoriteMode() == FavoriteMode.NOT_FAVORITE && isFavorite) {
                continue;
            }

            // Selected Song
            if(songId.equals(this.selectedSong)) {
                newSelectedIndex = this.displaySongs.size();
            }

            this.displaySongs.add(fileInfo);
        }

        this.selectedDisplayIndex = newSelectedIndex;
    }
    
    public void shiftLoopMode() {
        if(loopMode() == LoopMode.ALL) {
            setLoopMode(LoopMode.SINGLE);
        } else if(loopMode() == LoopMode.SINGLE) {
            setLoopMode(LoopMode.NONE);
        } else {
            setLoopMode(LoopMode.ALL);
        }
    }

    public Integer getLoopMode() {
        return loopMode().ordinal();
    }
    
    public void shiftSourceMode() {
        if(sourceMode() == SourceMode.ALL) {
            setSourceMode(SourceMode.LOCAL);
        } else if(sourceMode() == SourceMode.LOCAL) {
            setSourceMode(SourceMode.SERVER);
        } else {
            setSourceMode(SourceMode.ALL);
        }
        this.buildDisplaySongList();
    }

    public Integer getSourceMode() {
        return sourceMode().ordinal();
    }

    public void shiftFavoriteMode() {
        if(favoriteMode() == FavoriteMode.ALL) {
            setFavoriteMode(FavoriteMode.FAVORITE);
        } else if(favoriteMode() == FavoriteMode.FAVORITE) {
            setFavoriteMode(FavoriteMode.NOT_FAVORITE);
        } else {
            setFavoriteMode(FavoriteMode.ALL);
        }
        this.buildDisplaySongList();
    }

    public Integer getFavoriteMode() {
        return favoriteMode().ordinal();
    }

    public Integer getShuffleMode() {
        return isShuffled() ? 1 : 0;
    }
    
    public Boolean toggleShuffle() {
        this.toggleShuffled();

        // Local
        if(this.isShuffled()) {
            this.shuffleSongOrder = new ArrayList<>(this.defaultSongOrder);
            Collections.shuffle(this.shuffleSongOrder);
        } else {
            this.shuffleSongOrder = null;
        }

        this.buildDisplaySongList();

        return isShuffled();
    }

    public void toggleSelectedSongFavorite() {
        if(this.selectedSong != null) {
            this.toggleFavoriteSong(this.selectedSong);
            this.refreshSelectedSongFavorite();
            this.buildDisplaySongList();
        }
    }
    
    public abstract UUID transmitterId();
    public abstract LoopMode loopMode();
    public abstract void setLoopMode(LoopMode mode);
    public abstract SourceMode sourceMode();
    public abstract void setSourceMode(SourceMode mode);
    public abstract FavoriteMode favoriteMode();
    public abstract void setFavoriteMode(FavoriteMode mode);
    public abstract Boolean isShuffled();
    public abstract void toggleShuffled();
    public abstract List<UUID> getFavoriteSongs();
    public abstract void toggleFavoriteSong(UUID id);
    public abstract Boolean isSongFavorite(UUID id);
}
