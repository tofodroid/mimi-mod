package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.com.sun.media.sound.MidiUtils;
import io.github.tofodroid.com.sun.media.sound.RealTimeSequencerProvider;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileCacheManager;
import io.github.tofodroid.mods.mimi.common.midi.MidiInputSourceManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiInfoPacket;

public class MidiTransmitterManager extends MidiInputSourceManager {
    private Integer lastTempoBPM;
    private Long pausedTickPosition;
    private Long pausedMicrosecond;
    
    // MIDI Sequencer
    private Sequencer activeSequencer;
    private MidiTransmitterInputReceiver activeReceiver;
    private Transmitter activeTransmitter;

    public void loadURL(String midiUrl) {
        if(this.isOpen()) {
            if(this.isPlaying()) {
                this.stop();
            }

            Pair<Sequence,ServerMidiInfoPacket.STATUS_CODE> result = MidiFileCacheManager.getOrCreateCachedSequence(midiUrl);

            if(result.getRight() == null && result.getLeft() != null) {
                try {
                    this.activeSequencer.setSequence(result.getLeft());
                    this.lastTempoBPM = getTempoBPM(result.getLeft());
                    this.activeSequencer.setTempoInBPM(this.lastTempoBPM);
                    this.play();
                } catch(Exception e) {
                    MIMIMod.LOGGER.warn("Failed to start sequencer: ", e);
                }
            } else {
                MIMIMod.LOGGER.warn("Failed to load MIDI: ", result.getValue().name());
            }
        }
    }

    public Boolean isPlaying() {
        return this.activeSequencer != null ? this.activeSequencer.isRunning() : false;
    }

    public Boolean isInProgress() {
        Integer seconds = this.getPositionSeconds();
        return seconds != null && seconds > 0;
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

    public void play() {
        if(this.activeSequencer != null && !this.activeSequencer.isRunning()) {
            if(this.pausedTickPosition != null) {
                this.activeSequencer.setTickPosition(this.pausedTickPosition);
            }

            if(this.lastTempoBPM != null) {
                this.activeSequencer.setTempoInBPM(this.lastTempoBPM);
            }

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
    }
    
    public void stop() {
        this.pausedTickPosition = null;
        this.pausedMicrosecond = null;

        if(this.activeSequencer != null) {
            this.activeSequencer.stop();
            this.activeSequencer.setTickPosition(0);
        }
    }
    
    public void close() {
        if(this.activeSequencer != null) {
            this.activeSequencer.stop();
        }

        if(this.activeReceiver != null) {
            this.activeReceiver.close();
            this.activeReceiver = null;
        }

        if(this.activeTransmitter != null) {
            this.activeTransmitter.setReceiver(null);
            this.activeTransmitter.close();
            this.activeTransmitter = null;
        }

        if(this.activeSequencer != null) {
            try {
                this.activeSequencer.close();
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to stop sequencer: ", e);
            }
            this.activeSequencer = null;
        }

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

        return 120;        
    }

    public Boolean isOpen() {
        return this.activeSequencer != null && this.activeSequencer.isOpen();
    }
    
    @Override
    protected void openTransmitter() {
        if(isOpen()) {
            try {
                this.activeTransmitter = this.activeSequencer.getTransmitter();
                this.activeReceiver = new MidiTransmitterInputReceiver();
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
                RealTimeSequencerProvider provider = new RealTimeSequencerProvider();
                this.activeSequencer = (Sequencer)provider.getDevice(provider.getDeviceInfo()[0]);
                this.activeSequencer.open();
                this.activeSequencer.addMetaEventListener(new MetaEventListener(){
                    @Override
                    public void meta(MetaMessage meta) {
                        if(MidiUtils.isMetaEndOfTrack(meta) && !activeSequencer.isRunning()) {
                            stop();
                        } else if(MidiUtils.isMetaTempo(meta) | (meta.getType() == 81 && meta.getData().length == 3)) {
                            byte[] data = meta.getData();
                            int mspq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                            lastTempoBPM = Math.round(60000001f / mspq);
                            activeSequencer.setTempoInBPM(lastTempoBPM);
                        }
                    }
                });
            } catch(Exception e) {
                // TODO
            }
        }
        
        if(this.activeTransmitter == null) {
            this.openTransmitter();
        }
    }
}
