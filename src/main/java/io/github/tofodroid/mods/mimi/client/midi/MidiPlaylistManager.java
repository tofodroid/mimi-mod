package io.github.tofodroid.mods.mimi.client.midi;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;

import io.github.tofodroid.com.sun.media.sound.MidiUtils;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.midi.MidiInputSourceManager;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;

public class MidiPlaylistManager extends MidiInputSourceManager {
    // Playlist
    private String playlistFolderPath;
    private List<MidiFileInfo> songList;
    private List<MidiFileInfo> originalList;
    private Long pausedTickPosition;
    private Long pausedMicrosecond;
    private Integer lastTempoBPM;
    private Integer selectedSongIndex;
    private LoopMode currentLoopMode = LoopMode.NONE;
    private TransmitMode currentTransmitMode = TransmitMode.PUBLIC;
    private Boolean shuffled = false;
        
    // MIDI Sequencer
    private Sequencer activeSequencer;
    private MidiSequenceInputReceiver activeReceiver;

    public MidiPlaylistManager() {
        // Create Sequencer
        createSequencer();
        
        // Load from Config
        loadFromFolder(ModConfigs.CLIENT.playlistFolderPath.get());
    }

    // Utils
    protected void resetPlaylist() {
        stop();

        this.playlistFolderPath = "";
        this.songList = new ArrayList<>();
        this.pausedTickPosition = null;
        this.pausedMicrosecond = null;
        this.lastTempoBPM = 120;
        this.selectedSongIndex = null;
        this.shuffled = false;
    }

    protected Boolean createSequencer() {
        try {
            this.activeSequencer = MidiSystem.getSequencer(false);
            this.activeSequencer.open();
            this.activeSequencer.addMetaEventListener(new MetaEventListener(){
                @Override
                public void meta(MetaMessage meta) {
                    if(MidiUtils.isMetaEndOfTrack(meta) && !activeSequencer.isRunning()) {
                        switch(currentLoopMode) {
                            case ALL:
                                stop();
                                shiftSong(true);
                                playFromBeginning();
                                break;
                            case SINGLE:
                                stop();
                                playFromBeginning();
                                break;
                            case NONE:
                            default:
                                stop();
                                break;
                        }
                    } else if(MidiUtils.isMetaTempo(meta) | (meta.getType() == 81 && meta.getData().length == 3)) {
                        byte[] data = meta.getData();
                        int mspq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                        lastTempoBPM = Math.round(60000001f / mspq);
                        activeSequencer.setTempoInBPM(lastTempoBPM);
                    }
                }
                
            });
            return true;
        } catch(Exception e) {
            this.activeSequencer = null;
            this.activeReceiver = null;
            MIMIMod.LOGGER.error("Failed to create MIDI Sequencer: ", e);
            return false;
        }
    }
    
    protected Boolean loadSelectedSong() {
        if(isOpen() && isSongSelected()) {
            try {
                Path activePath = Paths.get(this.playlistFolderPath, this.songList.get(this.selectedSongIndex).fileName);
                activeSequencer.setSequence(MidiSystem.getSequence(activePath.toFile()));
                this.lastTempoBPM = this.songList.get(this.selectedSongIndex).tempo;
                return true;
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to open MIDI file " + Paths.get(this.playlistFolderPath, this.songList.get(this.selectedSongIndex).fileName).toString(), e);
            }
        }

        return false;
    }

    // Getters
    public Boolean isOpen() {
        return this.activeSequencer != null && this.activeSequencer.isOpen();
    }
    
    public Boolean isSongSelected() {
        return this.selectedSongIndex != null;
    }
    
    public Boolean isSongLoaded() {
        return this.isOpen() && isSongSelected() && this.activeSequencer.getSequence() != null;
    }
        
    public Integer getSongLengthSeconds() {
        return isSongLoaded() ? Long.valueOf(this.activeSequencer.getSequence().getMicrosecondLength() / 1000000).intValue() : null;
    }
        
    public Integer getCurrentSongPosSeconds() {
        Long microPos;

        if(isPlaying()) {
            microPos = this.activeSequencer.getMicrosecondPosition();
        } else if(this.pausedMicrosecond != null) {
            microPos = this.pausedMicrosecond;
        } else if(isSongLoaded()) {
            microPos = 0l;
        } else {
            return null;
        }

        return Long.valueOf(microPos / 1000000).intValue();
    }

    public MidiFileInfo getSelectedSongInfo() {
        return isSongSelected() ? this.songList.get(this.selectedSongIndex) : null;
    }

    public Integer getSelectedSongIndex() {
        return selectedSongIndex;
    }

    public String getPlaylistFolderPath() {
        return this.playlistFolderPath;
    }

    public List<MidiFileInfo> getLoadedPlaylist() {
        return songList;
    }

    // Playlist Controls
    public Boolean loadFromFolder(String folderPath) {
        // Clear existing playlist
        resetPlaylist();

        // Validate path
        if(folderPath != null && !folderPath.trim().isEmpty() && Files.isDirectory(Paths.get(folderPath.trim()), LinkOption.NOFOLLOW_LINKS)) {
            this.playlistFolderPath = folderPath.trim();

            // Load songs
            for(File file : new File(this.playlistFolderPath).listFiles()) {
                if(file.isFile() && file.getAbsolutePath().endsWith("midi") || file.getAbsolutePath().endsWith("mid")) {
                    MidiFileInfo info = MidiFileInfo.fromFile(file);
                    if(info != null) songList.add(info);
                }
            }

            // Select first song
            if(!this.songList.isEmpty()) {
                selectSong(0);
            }

            return true;
        }

        return false;
    }

    public void playFromBeginning() {
        if(isSongLoaded()) {
            stop();
            this.lastTempoBPM = this.songList.get(this.selectedSongIndex).tempo;
            this.activeSequencer.start();
        }
    }

    public void playFromLastTickPosition() {
        if(isPlaying()) {
            return;
        }

        if(isSongLoaded() && this.pausedTickPosition != null) {
            this.activeSequencer.stop();
            this.activeSequencer.setTickPosition(this.pausedTickPosition);
            this.activeSequencer.setTempoInBPM(this.lastTempoBPM);
            this.pausedTickPosition = null;
            this.pausedMicrosecond = null;
            this.activeSequencer.start();
        } else if(isSongLoaded()) {
            playFromBeginning();
        }
    }

    public void pause() {
        if(isSongLoaded() && isPlaying()) {
            this.pausedTickPosition = this.activeSequencer.getTickPosition();
            this.pausedMicrosecond = this.activeSequencer.getMicrosecondPosition();
            this.activeSequencer.stop();
        }
    }

    public void stop() {
        this.pausedTickPosition = null;
        this.pausedMicrosecond = null;

        if(isSongLoaded()) {
            this.activeSequencer.stop();
            this.activeSequencer.setTickPosition(0);
        }
    }

    public void selectSong(Integer songNum) {
        Boolean play = false;

        if(isPlaying()) {
            play = true;
        }

        stop();
        this.selectedSongIndex = null;

        if(songNum != null && songNum >= 0 && songNum < this.songList.size()) {
            this.selectedSongIndex = songNum;
            loadSelectedSong();

            if(play) {
                this.playFromBeginning();
            }
        }
    }

    public void shiftSong(Boolean up) {
        if(this.songList == null || this.songList.isEmpty()) {
            return;
        }

        if(!up) {
            if(this.selectedSongIndex > 0) {
                selectSong(this.selectedSongIndex-1);
            } else {
                selectSong(this.songList.size() - 1);
            }
        } else {
            if(this.selectedSongIndex < this.songList.size() - 1) {
                selectSong(this.selectedSongIndex+1);
            } else {
                selectSong(0);
            }
        }
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

    public void shiftTransmitMode() {
        if(currentTransmitMode == TransmitMode.PUBLIC) {
            stop();
            currentTransmitMode = TransmitMode.LINKED;
        } else if(currentTransmitMode == TransmitMode.LINKED) {
            stop();
            currentTransmitMode = TransmitMode.SELF;
        } else {
            stop();
            currentTransmitMode = TransmitMode.PUBLIC;
        }
    }

    public TransmitMode getTransmitMode() {
        return currentTransmitMode;
    }

    public Integer getTransmitModeInt() {
        return currentTransmitMode.ordinal();
    }

    public Integer getShuffleMode() {
        return shuffled ? 1 : 0;
    }
    
    public Boolean toggleShuffle() {
        this.shuffled = !this.shuffled;

        if(this.getSongCount() > 0 && this.shuffled) {
            this.originalList = new ArrayList<>(this.songList);
            MidiFileInfo selectedSong = getSelectedSongInfo();
            Collections.shuffle(this.songList);
            this.selectedSongIndex = this.songList.indexOf(selectedSong);
        } else if(this.getSongCount() > 0) {
            MidiFileInfo selectedSong = getSelectedSongInfo();
            this.songList = this.originalList;
            this.originalList = new ArrayList<>();
            this.selectedSongIndex = this.songList.indexOf(selectedSong);
        }

        return shuffled;
    }

    public Integer getSongCount() {
        return songList.size();
    }

    public Boolean isPlaying() {
        return isSongLoaded() && this.activeSequencer.isRunning();
    }

    // Internal Functions
    @Override
    protected void openTransmitter() {
        if(isOpen()) {
            try {
                this.activeTransmitter = this.activeSequencer.getTransmitter();
                this.activeReceiver = new MidiSequenceInputReceiver();
                this.activeTransmitter.setReceiver(this.activeReceiver);
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Midi Device Error: ", e);
                close();
            }
        }
    }

    @Override
    public void open() {
        if(!isOpen()) {
            try {
                this.activeSequencer = MidiSystem.getSequencer(false);
                this.activeSequencer.open();
            } catch(Exception e) {
                // TODO
            }
        }
        
        if(this.activeTransmitter == null) {
            this.openTransmitter();
        }
    }

    @Override
    public void close() {
        stop();
        super.close();
        this.activeReceiver = null;

        if(this.activeSequencer != null) {
            this.activeSequencer.close();
            this.activeSequencer = null;
        }
    }

    public enum LoopMode {
        ALL,
        SINGLE,
        NONE;
    }
}
