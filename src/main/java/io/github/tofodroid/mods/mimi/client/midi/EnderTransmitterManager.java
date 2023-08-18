package io.github.tofodroid.mods.mimi.client.midi;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileInfo;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiListPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiUploadPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiUploadPacketHandler;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacket.CONTROL;
import net.minecraft.client.Minecraft;

public class EnderTransmitterManager {
    public enum LoopMode {
        ALL,
        SINGLE,
        NONE;
    }

    private Boolean localMode = true;
    private LoopMode currentLoopMode = LoopMode.NONE;
    private Boolean shuffled = false;

    private List<MidiFileInfo> localSongList = new ArrayList<>();
    private List<MidiFileInfo> serverSongList = new ArrayList<>();
    private List<MidiFileInfo> originalLocalSongList = new ArrayList<>();
    private List<MidiFileInfo> originalServerSongList = new ArrayList<>();
    
    private Integer selectedLocalSong = -1;
    private Integer selectedServerSong = -1;
    private Boolean selectedSongLoaded = false;
    private Boolean selectedSongLoadingInProgress = false;
    private Boolean disableStatusRefresh = true;

    private ServerMusicPlayerStatusPacket lastMediaStatus = null;

    public EnderTransmitterManager() {}

    // Media
    public void startRefreshMediaStatus() {
        if(!this.disableStatusRefresh && this.songLoaded()) {
            NetworkManager.INFO_CHANNEL.sendToServer(ServerMusicPlayerStatusPacket.requestPacket());
        }
    }

    public void finishRefreshMediaStatus(ServerMusicPlayerStatusPacket response) {
        if(!this.disableStatusRefresh) {
            this.selectedSongLoaded = true;

            if(response.complete && (this.lastMediaStatus == null || !this.lastMediaStatus.complete)) {
                switch(currentLoopMode) {
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

    public void play() {
        if(!this.isPlaying() && this.getCurrentModeHasSongs()) {
            if(this.songLoaded()) {
                this.sendControlPacket(CONTROL.PLAY);
            } else {
                if(this.isLocalMode()) {
                    this.startUploadSelectedLocalSongToServer();
                } else {
                    this.sendControlPacket(CONTROL.PLAY, selectedServerSong);
                    this.disableStatusRefresh = false;
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
        if(this.getCurrentModeHasSongs() && this.selectedSongLoaded) {
            this.sendControlPacket(CONTROL.SEEK, seekToSeconds);
        }
    }

    public void sendControlPacket(CONTROL control) {
        NetworkManager.INFO_CHANNEL.sendToServer(new TransmitterControlPacket(control));
    }

    public void sendControlPacket(CONTROL control, Integer data) {
        NetworkManager.INFO_CHANNEL.sendToServer(new TransmitterControlPacket(control, data));
    }

    // Mode
    public Boolean isLocalMode() {
        return this.localMode;
    }

    public void clearMediaState() {
        this.stop();
        this.disableStatusRefresh = true;
        this.selectedSongLoaded = false;
        this.selectedSongLoadingInProgress = false;
    }

    public void toggleLocalMode() {
        clearMediaState();
        this.localMode = !this.localMode;
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

    public Boolean songLoaded() {
        return this.selectedSongLoaded;
    }

    public Boolean songLoading() {
        return this.selectedSongLoadingInProgress;
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
            if(localMode) {
                this.selectedLocalSong = songIndex;

                if(this.isPlaying()) {
                    this.stop();
                    this.startUploadSelectedLocalSongToServer();
                }
            } else {
                this.selectedServerSong = songIndex;

                if(this.isPlaying()) {
                    this.selectedSongLoadingInProgress = true;
                    this.sendControlPacket(CONTROL.SELECT, selectedServerSong);
                }
            }
            this.selectedSongLoaded = false;  
            return true;
        }
        return false;
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
        if(this.selectedLocalSong >= 0 && !selectedSongLoadingInProgress) {
            selectedSongLoadingInProgress = true;
            ServerMidiUploadPacketHandler.startUploadMidiSequence(Minecraft.getInstance().player.getUUID(), this.localSongList.get(this.selectedLocalSong).getSequence());
        }
    }

    public Boolean finishUploadSelectedLocalSongToServer(ServerMidiUploadPacket response) {
        selectedSongLoadingInProgress = false;

        if(response.totalParts == ServerMidiUploadPacket.UPLOAD_SUCCESS) {
            disableStatusRefresh = false;
            this.sendControlPacket(CONTROL.PLAY);
            return true;
        } else {
            disableStatusRefresh = true;
        }

        return false;
    }

    public void startLoadServerSongsFromRemote() {
        clearServerSongs();
        NetworkManager.INFO_CHANNEL.sendToServer(new ServerMidiListPacket());
    }

    public Boolean finishLoadServerSongsFromRemote(ServerMidiListPacket response) {
        if(response.midiList.isPresent()) {
            this.serverSongList = new ArrayList<>(response.midiList.get());
            
            // Select first song
            if(!this.serverSongList.isEmpty()) {
                this.selectedServerSong = 0;
            }

            return true;
        }
        return false;
    }

    public Boolean loadLocalSongsFromFolder(String folderPath) {
        clearLocalSongs();

        // Validate path
        if(folderPath != null && !folderPath.trim().isEmpty() && Files.isDirectory(Paths.get(folderPath.trim()), LinkOption.NOFOLLOW_LINKS)) {
            // Load songs
            for(File file : new File(folderPath).listFiles()) {
                if(file.isFile() && file.getAbsolutePath().endsWith("midi") || file.getAbsolutePath().endsWith("mid")) {
                    MidiFileInfo info = MidiFileInfo.fromFile(file);
                    if(info != null) localSongList.add(info);
                }
            }

            // Select first song
            if(!this.localSongList.isEmpty()) {
                this.selectedLocalSong = 0;
            }

            return true;
        }

        return false;
    }
    
    public void shiftLoopMode() {
        if(currentLoopMode == LoopMode.ALL) {
            currentLoopMode = LoopMode.SINGLE;
        } else if(currentLoopMode == LoopMode.SINGLE) {
            currentLoopMode = LoopMode.NONE;
        } else {
            currentLoopMode = LoopMode.ALL;
        }
    }

    public Integer getLoopMode() {
        return currentLoopMode.ordinal();
    }

    public Integer getShuffleMode() {
        return shuffled ? 1 : 0;
    }
    
    public Boolean toggleShuffle() {
        MidiFileInfo selectedSong = getCurrentModeSelectedSongInfo();
        this.shuffled = !this.shuffled;

        // Local
        if(this.shuffled) {
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

        return shuffled;
    }
}
