package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import java.util.UUID;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

import io.github.tofodroid.com.sun.media.sound.MidiUtils;
import io.github.tofodroid.com.sun.media.sound.RealTimeSequencerProvider;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.AServerMidiInputReceiver;
import io.github.tofodroid.mods.mimi.util.MidiFileUtils;
import net.minecraft.world.entity.player.Player;

public class MidiHandler {
    private final Runnable sequenceEndCallback;

    // Play State
    private Integer lastTempoBPM;
    private Long pausedTickPosition;
    private Long pausedMicrosecond;
    
    // MIDI Sequence
    private BasicMidiInfo activeSequenceInfo;
    private Integer songLengthSeconds;
    private byte[] channelMapping;

    // Midi System
    private Sequencer activeSequencer;
    private AServerMidiInputReceiver activeReceiver;
    private Transmitter activeTransmitter;

    public MidiHandler(TileTransmitter tile, Runnable sequenceEndCallback) {
        this(new TileTransmitterMidiReceiver(tile), sequenceEndCallback);
    }

    public MidiHandler(Player player, Runnable sequenceEndCallback) {
        this(new PlayerTransmitterMidiReceiver(player), sequenceEndCallback);
    }

    protected MidiHandler(AServerMidiInputReceiver receiver, Runnable sequenceEndCallback) {
        this.activeReceiver = receiver;
        this.sequenceEndCallback = sequenceEndCallback;
        initializeSequencer();
    }

    public Boolean isPlaying() {
        return this.hasSongLoaded() ? this.activeSequencer.isRunning() : false;
    }

    public Boolean isInProgress() {
        Integer seconds = this.getPositionSeconds();
        return seconds != null && seconds > 0;
    }

    public BasicMidiInfo getSequenceInfo() {
        return this.activeSequenceInfo;
    }

    public Boolean hasSongLoaded() {
        return this.activeSequencer != null && this.activeSequenceInfo != null && this.activeSequencer.getSequence() != null;
    }

    public void unloadSong() {
        this.stop();
        this.activeSequenceInfo = null;
        this.songLengthSeconds = null;
        this.lastTempoBPM = null;
        this.channelMapping = null;
    }

    public UUID getSequenceId() {
        return this.activeSequenceInfo != null ? this.activeSequenceInfo.fileId : null;
    }
    
    public Integer getPositionSeconds() {
        if(this.activeSequencer != null && this.activeSequencer.isOpen()) {
            if(this.activeSequencer.isRunning()) {
                return Long.valueOf(this.activeSequencer.getMicrosecondPosition() / 1000000).intValue();
            } else if(!this.activeSequencer.isRunning() && this.pausedMicrosecond != null) {
                return Long.valueOf(this.pausedMicrosecond / 1000000).intValue();
            } else {
                return null;
            }
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
        if(this.activeReceiver != null) {
            this.activeReceiver.sendTransmitterAllNotesOffPacket();
        }
    }

    public void load(BasicMidiInfo info, Sequence sequence) {
        Boolean wasPlaying = this.isPlaying();
        this.stop();

        if(this.activeSequencer != null) {
            try {
                this.activeSequencer.setSequence(sequence);
                this.lastTempoBPM = null;
                this.activeSequenceInfo = info;
                this.songLengthSeconds = MidiFileUtils.getSongLenghtSeconds(sequence);
                this.channelMapping = MidiFileUtils.getChannelMapping(sequence);

                if(wasPlaying) {
                    this.play();
                }
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to load sequence: ", e);
            }
        }
    }

    public void play() {
        if(this.hasSongLoaded() && !this.activeSequencer.isRunning()) {
            if(this.pausedTickPosition != null) {
                this.activeSequencer.setTickPosition(this.pausedTickPosition);
            }

            if(this.lastTempoBPM == null) {
                this.lastTempoBPM = getTempoBPM(this.activeSequencer.getSequence());
            }

            this.activeSequencer.setTempoInBPM(this.lastTempoBPM);

            this.activeSequencer.start();
        }
        this.pausedTickPosition = null;
        this.pausedMicrosecond = null;
    }

    public void pause() {
        if(this.activeSequencer != null && this.activeSequencer.isRunning()) {
            this.pausedTickPosition = this.activeSequencer.getTickPosition();
            this.pausedMicrosecond = this.activeSequencer.getMicrosecondPosition();;
            this.activeSequencer.stop();
        }

        if(this.activeReceiver != null) {
            this.activeReceiver.sendTransmitterAllNotesOffPacket();
        }
    }
    
    public void stop() {
        this.pausedTickPosition = null;
        this.pausedMicrosecond = null;
        this.lastTempoBPM = null;

        if(this.activeSequencer != null) {
            this.activeSequencer.stop();
            this.activeSequencer.setTickPosition(0);
        }

        if(this.activeReceiver != null) {
            this.activeReceiver.sendTransmitterAllNotesOffPacket();
        }
    }
    
    public void close() {
        if(this.activeSequencer != null) {
            this.activeSequencer.stop();
            try {
                this.activeSequencer.close();
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to stop sequencer: ", e);
            }
            this.activeSequencer = null;
        }

        if(this.activeReceiver != null) {
            this.activeReceiver.sendTransmitterAllNotesOffPacket();
            this.activeReceiver.close();
            this.activeReceiver = null;
        }

        if(this.activeTransmitter != null) {
            this.activeTransmitter.close();
            this.activeTransmitter = null;
        }

        this.activeSequenceInfo = null;
        this.songLengthSeconds = null;
        this.channelMapping = null;
    }

    private static Integer getTempoBPM(Sequence sequence) {
        for(Track track : sequence.getTracks()) {
            if(track != null && track.size() > 0) {
                for(int i = 0; i < track.size(); i++) {
                    if(track.get(i).getMessage() instanceof MetaMessage) {
                        MetaMessage message = (MetaMessage)track.get(i).getMessage();
                        if(message.getType() == 81 && message.getData().length == 3) {
                            byte[] data = message.getData();
                            int mspq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                            return Math.round(60000001f / mspq);
                        }
                    }
                }
            }
        }

        // Safe default
        return 120;
    }

    protected Boolean initializeSequencer() {
        try { 
            MidiHandler self = this;
            RealTimeSequencerProvider provider = new RealTimeSequencerProvider();
            this.activeSequencer = (Sequencer)provider.getDevice(provider.getDeviceInfo()[0]);
            this.activeSequencer.open();
            this.activeSequencer.addMetaEventListener(new MetaEventListener(){
                @Override
                public void meta(MetaMessage meta) {
                    if(MidiUtils.isMetaEndOfTrack(meta) && !activeSequencer.isRunning()) {
                        self.stop();
                        self.sequenceEndCallback.run();
                    } else if(MidiUtils.isMetaTempo(meta) | (meta.getType() == 81 && meta.getData().length == 3)) {
                        byte[] data = meta.getData();
                        int mspq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                        lastTempoBPM = Math.round(60000001f / mspq);
                        activeSequencer.setTempoInBPM(lastTempoBPM);
                    }
                }
            });
            this.activeTransmitter = this.activeSequencer.getTransmitter();
            this.activeTransmitter.setReceiver(this.activeReceiver);
            return true;
        } catch(Exception e) {
            throw new RuntimeException("Failed to create sequencer: ", e);
        }
    }
}
