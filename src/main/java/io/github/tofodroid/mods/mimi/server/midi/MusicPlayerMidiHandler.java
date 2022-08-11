package io.github.tofodroid.mods.mimi.server.midi;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

import io.github.tofodroid.com.sun.media.sound.MidiUtils;
import io.github.tofodroid.com.sun.media.sound.RealTimeSequencerProvider;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileInfo;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiInfoPacket;
import io.github.tofodroid.mods.mimi.common.tile.TileBroadcaster;

public class MusicPlayerMidiHandler {
    private Integer lastTempoBPM;
    private MidiFileInfo midiInfo = null;
    private Boolean error = false;
    private Boolean endOfMusic = false;
    private ServerMidiInfoPacket.STATUS_CODE status = null;
    
    // MIDI Sequencer
    private Sequence activeSequence;
    private Sequencer activeSequencer;
    private MusicPlayerReceiver activeReceiver;
    private Transmitter activeTransmitter;

    public MusicPlayerMidiHandler(TileBroadcaster tile, Sequence sequence, ServerMidiInfoPacket.STATUS_CODE errorStatus) {
        this.activeSequence = sequence;

        if(errorStatus != null) {
            this.error = true;
            this.status = errorStatus;
        } else if(this.activeSequence != null && createSequencer(tile)) {
            try {
                this.lastTempoBPM = getTempoBPM(this.activeSequence);
                this.activeSequencer.setSequence(this.activeSequence);
                this.midiInfo = MidiFileInfo.fromSequence("server", sequence);
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Failed to start sequencer: ", e);
                this.error = true;
                this.status = ServerMidiInfoPacket.STATUS_CODE.ERROR_OTHER;
            }
        } else {
            this.error = true;
            this.status = ServerMidiInfoPacket.STATUS_CODE.ERROR_OTHER;
        }

        if(this.error) {
            tile.endOfMusic();
            this.endOfMusic = true;
        }
    }

    public ServerMidiInfoPacket.STATUS_CODE getErrorStatus() {
        return this.status;
    }

    public Boolean isInError() {
        return this.error;
    }

    public Boolean isAtEndOfSong() {
        return this.endOfMusic;
    }
    
    public MidiFileInfo getMidiFileInfo() {
        return this.midiInfo;
    }

    public Integer getPositionSeconds() {
        if(this.activeSequencer != null && this.activeSequencer.isOpen()) {
            if(this.activeSequencer.isRunning()) {
                return Long.valueOf( this.activeSequencer.getMicrosecondPosition() / 1000000).intValue();
            } else if(this.endOfMusic) {
                return this.midiInfo.songLength;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void play() {
        if(this.activeSequencer != null && !this.activeSequencer.isRunning()) {
            this.activeSequencer.start();
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
            this.activeSequencer.stop();
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
    
    protected Boolean createSequencer(TileBroadcaster tile) {
        try {
            RealTimeSequencerProvider provider = new RealTimeSequencerProvider();
            this.activeSequencer = (Sequencer)provider.getDevice(provider.getDeviceInfo()[0]);
            this.activeSequencer.open();
            this.activeSequencer.addMetaEventListener(new MetaEventListener(){
                @Override
                public void meta(MetaMessage meta) {
                    if(MidiUtils.isMetaEndOfTrack(meta) && !activeSequencer.isRunning()) {
                        activeReceiver.endOfTrack();
                        endOfMusic = true;
                    } else if(MidiUtils.isMetaTempo(meta) | (meta.getType() == 81 && meta.getData().length == 3)) {
                        byte[] data = meta.getData();
                        int mspq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                        lastTempoBPM = Math.round(60000001f / mspq);
                        activeSequencer.setTempoInBPM(lastTempoBPM);
                    }
                }
            });
            this.activeTransmitter = this.activeSequencer.getTransmitter();
            this.activeReceiver = new MusicPlayerReceiver(tile);
            this.activeTransmitter.setReceiver(this.activeReceiver);
            return true;
        } catch(Exception e) {
            this.activeSequencer = null;
            this.activeReceiver = null;
            MIMIMod.LOGGER.error("Failed to create sequencer: ", e);
            return false;
        }
    }
}
