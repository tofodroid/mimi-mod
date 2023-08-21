package io.github.tofodroid.mods.mimi.common.midi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private Boolean initialized = false;
    private Boolean localMode = this.supportsLocal();
    private List<MidiFileInfo> localSongList = new ArrayList<>();
    private List<MidiFileInfo> serverSongList = new ArrayList<>();
    private List<MidiFileInfo> originalLocalSongList = new ArrayList<>();
    private List<MidiFileInfo> originalServerSongList = new ArrayList<>();
    private Integer selectedLocalSong = -1;
    private Integer selectedServerSong = -1;
    private Boolean selectedSongHasLoaded = false;
    private Boolean selectedSongIsLoading = false;

    private ServerMusicPlayerStatusPacket lastMediaStatus = null;

    public void init() {
        if(!this.initialized) {
            if(supportsLocal()) {
                this.localSongList = MIMIMod.proxy.customMidiFiles().getAllSongs();

                if(this.localSongList.size() > 0) {
                    this.selectSong(0);
                }
            }

            if(!this.localMode) {
                this.startLoadServerSongsFromRemote();
            }

            this.initialized = true;
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

    public void sendControlPacket(CONTROL control, Integer data) {
        NetworkManager.INFO_CHANNEL.sendToServer(new TransmitterControlPacket(transmitterId(), control, data));
    }

    // Mode
    public Boolean isLocalMode() {
        return this.localMode;
    }

    public void clearMediaState() {
        this.stop();
        this.selectedSongHasLoaded = false;
        this.selectedSongIsLoading = false;
    }

    public void toggleLocalMode() {
        if(supportsLocal()) {
            clearMediaState();
            this.localMode = !this.localMode;

            if(!this.localMode) {
                this.startLoadServerSongsFromRemote();
            }
        }

    }

    // Song List
    public List<MidiFileInfo> getCurrentModeSongList() {
        return localMode ? localSongList : serverSongList;
    }

    public Integer getCurrentSongLength() {
        MidiFileInfo info = getCurrentModeSelectedSongInfo();

        if(info != null) {
            return info.songLength;
        } else {
            return 1;
        }
    }

    public Integer getCurrentModeSongCount() {
        return getCurrentModeSongList().size();
    }

    public Integer getCurrentModeSelectedSong() {
        return localMode ? selectedLocalSong : selectedServerSong;
    }

    public Boolean songLoading() {
        return this.selectedSongIsLoading;
    }

    public Boolean songLoaded() {
        return this.selectedSongHasLoaded;
    }

    public MidiFileInfo getCurrentModeSelectedSongInfo() {
        Integer currentSong = getCurrentModeSelectedSong();

        if(currentSong >= 0) {
            return getCurrentModeSongList().get(currentSong);
        }

        return null;
    }
    
    public Boolean getCurrentModeHasSongs() {
        return getCurrentModeSongCount() > 0;
    }

    public Integer selectNextSong() {
        if(getCurrentModeHasSongs()) {
            Integer newSongIndex = 0;

            if(getCurrentModeSelectedSong() < (getCurrentModeSongCount() - 1)) {
                newSongIndex = getCurrentModeSelectedSong() + 1;
            }
            
            selectSong(newSongIndex);
            return newSongIndex;
        }
        return -1;
    }

    public Integer selectPreviousSong() {
        if(getCurrentModeHasSongs()) {
            Integer newSongIndex = getCurrentModeSongCount() - 1;

            if(getCurrentModeSelectedSong() > 0) {
                newSongIndex = getCurrentModeSelectedSong() - 1;
            }
            
            selectSong(newSongIndex);
            return newSongIndex;
        }
        return -1;
    }

    public Boolean selectSong(Integer songIndex) {
        if(songIndex < getCurrentModeSongCount()) {
            Boolean wasPlaying = this.isPlaying();
            this.stop();
            this.selectedSongHasLoaded = false;

            if(localMode) {
                this.selectedLocalSong = songIndex;

                if(wasPlaying || this.selectedSongIsLoading) {
                    this.startUploadSelectedLocalSongToServer();
                }
            } else {
                this.selectedServerSong = songIndex;

                if(wasPlaying) {
                    this.sendControlPacket(CONTROL.PLAY, selectedServerSong);
                    this.selectedSongHasLoaded = true;
                }
            }
            return true;
        }
        return false;
    }
    
    public void play() {
        if(!this.isPlaying() && this.getCurrentModeHasSongs()) {
            if(this.songLoaded()) {
                this.sendControlPacket(CONTROL.PLAY);
            } else {
                if(this.isLocalMode()) {
                    this.startUploadSelectedLocalSongToServer();
                } else {
                    this.sendControlPacket(CONTROL.PLAY, selectedServerSong);
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
        if(this.getCurrentModeHasSongs() && this.songLoaded()) {
            this.sendControlPacket(CONTROL.SEEK, seekToSeconds);
        }
    }

    public void clearServerSongs() {
        this.clearMediaState();
        this.selectedServerSong = -1;
        this.serverSongList = new ArrayList<>();
        this.originalServerSongList = new ArrayList<>();
    }

    public void clearLocalSongs() {
        this.clearMediaState();
        this.selectedLocalSong = -1;
        this.localSongList = new ArrayList<>();
        this.originalLocalSongList = new ArrayList<>();
    }
    
    public void startUploadSelectedLocalSongToServer() {
        if(this.selectedLocalSong >= 0) {
            this.selectedSongIsLoading = true;
            MidiUploadManager.startUploadSequenceToServer(this.localSongList.get(this.selectedLocalSong).getSequence());
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
        clearServerSongs();
        NetworkManager.INFO_CHANNEL.sendToServer(new ServerMidiListPacket());
    }

    public Boolean finishLoadServerSongsFromRemote(ServerMidiListPacket response) {
        this.serverSongList = new ArrayList<>(response.midiList);
        
        // Select first song
        if(!this.serverSongList.isEmpty()) {
            this.selectSong(0);
            return true;
        }

        return false;
    }

    public Boolean loadLocalSongsFromFolder(String folderPath) {
        clearLocalSongs();
        MIMIMod.proxy.customMidiFiles().initFromAbsolutePath(folderPath);
        this.localSongList = MIMIMod.proxy.customMidiFiles().getAllSongs();

        // Select first song
        if(!this.localSongList.isEmpty()) {
            this.selectSong(0);
            return true;
        }

        return false;
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

    public Integer getShuffleMode() {
        return isShuffled() ? 1 : 0;
    }
    
    public Boolean toggleShuffle() {
        MidiFileInfo selectedSong = getCurrentModeSelectedSongInfo();
        this.toggleShuffled();

        // Local
        if(this.isShuffled()) {
            this.originalLocalSongList = new ArrayList<>(this.localSongList);
            this.originalServerSongList = new ArrayList<>(this.serverSongList);
            Collections.shuffle(this.localSongList);
            Collections.shuffle(this.serverSongList);
        } else {
            this.localSongList = this.originalLocalSongList;
            this.serverSongList = this.originalServerSongList;
            this.originalLocalSongList = new ArrayList<>();
            this.originalServerSongList = new ArrayList<>();
        }
    
        if(selectedSong != null) {
            Integer selectedIndex = getCurrentModeSongList().indexOf(selectedSong);

            if(isLocalMode()) {
                this.selectedLocalSong = selectedIndex;
            } else {
                this.selectedServerSong = selectedIndex;
            }
        }

        return isShuffled();
    }
    
    public abstract Boolean supportsLocal();
    public abstract UUID transmitterId();
    public abstract Boolean isShuffled();
    public abstract LoopMode loopMode();
    public abstract void toggleShuffled();
    public abstract void setLoopMode(LoopMode mode);
}
