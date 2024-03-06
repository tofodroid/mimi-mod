package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import java.util.UUID;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequence;

import io.github.tofodroid.com.sun.media.sound.MidiUtils;
import io.github.tofodroid.com.sun.media.sound.SimpleThreadSequencer;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.AServerMidiInputReceiver;
import io.github.tofodroid.mods.mimi.util.MidiFileUtils;
import net.minecraft.world.entity.player.Player;

public class MidiHandler {
    private final Runnable sequenceEndCallback;
    
    // MIDI Sequence
    private Sequence activeSequence;
    private BasicMidiInfo activeSequenceInfo;
    private Integer songLengthSeconds;
    private byte[] channelMapping;

    // Midi System
    private SimpleThreadSequencer<AServerMidiInputReceiver> activeSequencer;

    public MidiHandler(TileTransmitter tile, Runnable sequenceEndCallback) {
        this(new TileTransmitterMidiReceiver(tile), sequenceEndCallback);
    }

    public MidiHandler(Player player, Runnable sequenceEndCallback) {
        this(new PlayerTransmitterMidiReceiver(player), sequenceEndCallback);
    }

    protected MidiHandler(AServerMidiInputReceiver receiver, Runnable sequenceEndCallback) {
        this.sequenceEndCallback = sequenceEndCallback;
        initializeSequencer(receiver);
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

    public void allNotesOff() {
        if(this.activeSequencer.isOpen()) {
            this.activeSequencer.getAutoConnectedReceiver().sendTransmitterAllNotesOffPacket();
        }
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
                this.activeSequencer.start();
            }
        }
    }
    
    public void setPositionPercent1000(Integer percent) {
        if(this.hasSongLoaded()) {
            this.pause();

            Long newTickPos = Double.valueOf((Double.valueOf(percent)/1000.0) * Double.valueOf(this.activeSequencer.getTickLength())).longValue();
            newTickPos = newTickPos < 0 ? 0 : (newTickPos >= this.activeSequencer.getTickLength() ? this.activeSequencer.getTickLength()-1000 : newTickPos);
            this.activeSequencer.setTickPosition(newTickPos);

            this.play();
        }
    }

    public void pause() {
        if(this.activeSequencer.isOpen() && this.activeSequencer.isRunning()) {
            this.activeSequencer.pause();
            this.allNotesOff();
        }
    }
    
    public void stop() {
        if(this.activeSequencer.isOpen()) {
            try {
                this.activeSequencer.stop();
                this.allNotesOff();
                this.activeSequencer.close();
            } catch (Exception e) {
                MIMIMod.LOGGER.error("Failed to stop sequencer: ", e);
                this.allNotesOff();
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

    protected Boolean initializeSequencer(AServerMidiInputReceiver receiver) {
        try { 
            MidiHandler self = this;
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
