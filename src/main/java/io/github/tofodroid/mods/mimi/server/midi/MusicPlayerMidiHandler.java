package io.github.tofodroid.mods.mimi.server.midi;

import java.io.IOException;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

import io.github.tofodroid.com.sun.media.sound.MidiUtils;
import io.github.tofodroid.com.sun.media.sound.RealTimeSequencerProvider;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.tile.TileBroadcaster;
import net.minecraft.world.entity.player.Player;

public class MusicPlayerMidiHandler {
    public final String url;

    private Integer lastTempoBPM;
    private Long pausedTickPosition;
    private Long pausedMicrosecond;
    
    // MIDI Sequencer
    private Sequence activeSequence;
    private Sequencer activeSequencer;
    private Receiver activeReceiver;
    private Transmitter activeTransmitter;

    public MusicPlayerMidiHandler(TileBroadcaster tile, Sequence sequence, String url) throws IOException {
        this.activeSequence = sequence;
        this.url = url;

        try {
            createSequencer(tile);
            this.lastTempoBPM = getTempoBPM(this.activeSequence);
            this.activeSequencer.setSequence(this.activeSequence);
        } catch(Exception e) {
            throw new IOException("Failed to start Server Music Handler: ", e);
        }
    }

    public MusicPlayerMidiHandler(Player player, Sequence sequence, String url) throws IOException  {
        this.activeSequence = sequence;
        this.url = url;

        try {
            createSequencer(player);
            this.lastTempoBPM = getTempoBPM(this.activeSequence);
            this.activeSequencer.setSequence(this.activeSequence);
        } catch(Exception e) {
            throw new IOException("Failed to start Server Music Handler: ", e);
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
        if(this.activeSequencer != null && this.activeSequencer.isOpen()) {
            this.activeSequencer.stop();
        }

        if(this.activeReceiver != null) {
            this.activeReceiver.close();
            this.activeReceiver = null;
        }

        if(this.activeTransmitter != null) {
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
                        tile.stopMusic();
                    } else if(MidiUtils.isMetaTempo(meta) | (meta.getType() == 81 && meta.getData().length == 3)) {
                        byte[] data = meta.getData();
                        int mspq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                        lastTempoBPM = Math.round(60000001f / mspq);
                        activeSequencer.setTempoInBPM(lastTempoBPM);
                    }
                }
            });
            this.activeTransmitter = this.activeSequencer.getTransmitter();
            this.activeReceiver = new BroadcasterReceiver(tile);
            this.activeTransmitter.setReceiver(this.activeReceiver);
            return true;
        } catch(Exception e) {
            this.activeSequencer = null;
            this.activeReceiver = null;
            MIMIMod.LOGGER.error("Failed to create sequencer: ", e);
            return false;
        }
    }

    protected Boolean createSequencer(Player player) {
        try {
            RealTimeSequencerProvider provider = new RealTimeSequencerProvider();
            this.activeSequencer = (Sequencer)provider.getDevice(provider.getDeviceInfo()[0]);
            this.activeSequencer.open();
            this.activeSequencer.addMetaEventListener(new MetaEventListener(){
                @Override
                public void meta(MetaMessage meta) {
                    if(MidiUtils.isMetaEndOfTrack(meta) && !activeSequencer.isRunning()) {
                        ServerMusicPlayerMidiManager.stopTransmitter(player.getUUID());
                    } else if(MidiUtils.isMetaTempo(meta) | (meta.getType() == 81 && meta.getData().length == 3)) {
                        byte[] data = meta.getData();
                        int mspq = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                        lastTempoBPM = Math.round(60000001f / mspq);
                        activeSequencer.setTempoInBPM(lastTempoBPM);
                    }
                }
            });
            this.activeTransmitter = this.activeSequencer.getTransmitter();
            this.activeReceiver = new TransmitterReceiver(player);
            this.activeTransmitter.setReceiver(this.activeReceiver);
            return true;
        } catch(Exception e) {
            throw new RuntimeException("Failed to create sequencer: ", e);
        }
    }
}
