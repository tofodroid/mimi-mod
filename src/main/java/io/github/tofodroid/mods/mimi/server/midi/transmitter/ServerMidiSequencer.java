package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import java.util.UUID;
import java.util.function.Consumer;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequence;

import io.github.tofodroid.com.sun.media.sound.MidiUtils;
import io.github.tofodroid.com.sun.media.sound.SimpleThreadSequencer;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiEvent;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.util.MidiFileUtils;

public class ServerMidiSequencer {
    private final Runnable sequenceEndCallback;
    
    // MIDI Sequence
    private Sequence activeSequence;
    private BasicMidiInfo activeSequenceInfo;
    private Integer songLengthSeconds;
    private byte[] channelMapping;

    // Runtime
    private Long startPlayMicros = null;

    // Midi System
    private SimpleThreadSequencer<ServerMidiInputReceiver> activeSequencer;

    public ServerMidiSequencer(Consumer<BasicMidiEvent> eventHandler, Runnable sequenceEndCallback) {
        initializeSequencer(new ServerMidiInputReceiver(eventHandler));
        this.sequenceEndCallback = sequenceEndCallback;
    }

    public Boolean isPlaying() {
        return this.activeSequencer.isOpen() ? this.activeSequencer.isRunning() : false;
    }

    public Boolean isInProgress() {
        Integer seconds = this.getPositionSeconds();
        return seconds != null && seconds > 0;
    }

    public BasicMidiInfo getSequenceInfo() {
        return this.activeSequenceInfo;
    }

    public Boolean hasSongLoaded() {
        return this.activeSequenceInfo != null && this.activeSequence != null;
    }

    public void unloadSong() {
        this.stop();
        this.activeSequenceInfo = null;
        this.songLengthSeconds = null;
        this.channelMapping = null;
    }

    public UUID getSequenceId() {
        return this.activeSequenceInfo != null ? this.activeSequenceInfo.fileId : null;
    }
    
    public Integer getPositionSeconds() {
        if(this.activeSequencer.isOpen()) {
            return Long.valueOf(this.activeSequencer.getMicrosecondPosition() / 1000000).intValue();
        } else {
            return null;
        }
    }

    public Integer getSongLengthSeconds() {
        return this.songLengthSeconds;
    }

    public byte[] getChannelMapping() {
        return this.channelMapping;
    }

    public void load(BasicMidiInfo info, Sequence sequence) {
        if(this.activeSequencer != null) {
            try {
                this.activeSequencer.setSequence(sequence);
                this.activeSequencer.setTickPosition(0);
                this.activeSequenceInfo = info;
                this.activeSequence = sequence;
                this.songLengthSeconds = MidiFileUtils.getSongLenghtSeconds(sequence);
                this.channelMapping = MidiFileUtils.getChannelMapping(sequence);
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to load sequence: " + info.fileName + " - " + e.getMessage());
                this.close();
            }
        }
    }

    public void play() {
        if(this.hasSongLoaded() && !this.activeSequencer.isRunning()) {
            if(!this.activeSequencer.isOpen()) {
                this.activeSequencer.open();
                try {
                    this.activeSequencer.setSequence(this.activeSequence);
                } catch(Exception e) {
                    MIMIMod.LOGGER.error("Failed to load sequence: " + this.activeSequenceInfo.fileName + " - " + e.getMessage());
                    this.close();
                }
            }

            if(this.activeSequencer.isOpen()) {
                if(this.startPlayMicros != null) {
                    this.activeSequencer.setMicrosecondPosition(this.startPlayMicros);
                    this.startPlayMicros = null;
                }
                this.activeSequencer.start();
            }
        }
    }
    
    public void setPositionPercent1000(Integer percent) {
        if(this.hasSongLoaded()) {
            Boolean wasPlaying = this.isPlaying();

            if(wasPlaying) {
                this.pause();
            }

            Long newMicroPos = Double.valueOf((Double.valueOf(percent)/1000.0) * Double.valueOf(this.activeSequence.getMicrosecondLength())).longValue();
            newMicroPos = newMicroPos < 0 ? 0 : (newMicroPos >= this.activeSequence.getMicrosecondLength() ? this.activeSequence.getMicrosecondLength()-100 : newMicroPos);
            this.startPlayMicros = newMicroPos;

            if(wasPlaying) {
                this.play();
            }
        }
    }

    public void pause() {
        if(this.activeSequencer.isOpen() && this.activeSequencer.isRunning()) {
            this.activeSequencer.pause();
        }
    }
    
    public void stop() {
        if(this.activeSequencer.isOpen()) {
            try {
                this.activeSequencer.stop();
                this.activeSequencer.close();
            } catch (Exception e) {
                MIMIMod.LOGGER.error("Failed to stop sequencer: ", e);
                this.close();
            }
        }
    }
    
    public void close() {
        if(this.activeSequencer.isOpen()) {
            try {
                this.activeSequencer.stop();
                this.activeSequencer.close();
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to stop sequencer: ", e);
            }
            this.activeSequencer = null;
        }

        this.activeSequenceInfo = null;
        this.songLengthSeconds = null;
        this.channelMapping = null;
    }

    protected Boolean initializeSequencer(ServerMidiInputReceiver receiver) {
        try { 
            ServerMidiSequencer self = this;
            this.activeSequencer = new SimpleThreadSequencer<>(receiver);
            this.activeSequencer.addMetaEventListener(new MetaEventListener(){
                @Override
                public void meta(MetaMessage meta) {
                    if(MidiUtils.isMetaEndOfTrack(meta) && !activeSequencer.isRunning()) {
                        self.stop();
                        self.sequenceEndCallback.run();
                    }
                }
            });
            return true;
        } catch(Exception e) {
            throw new RuntimeException("Failed to create sequencer: ", e);
        }
    }
}