package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.sound.midi.MidiChannel;

import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class MIMIChannel {
    public static final Integer MIDI_CHANNEL_IDLE_SECONDS = 8;

    protected final MidiChannel channel;
    protected final Integer channelNum;
    protected Instant lastNoteTime;

    public MIMIChannel(Integer channelNum, MidiChannel channel) {
        this.channelNum = channelNum;
        this.channel = channel;
        this.setVolume(Integer.valueOf(0).byteValue());
    }

    public void setInstrument(InstrumentSpec instrument) {
        this.channel.programChange(instrument.midiBankNumber * 128, instrument.midiPatchNumber);
        this.setVolume(Integer.valueOf(0).byteValue());
        this.channel.allSoundOff();
        this.channel.resetAllControllers();
    }

    public void reset() {
        this.lastNoteTime = null;
        this.setVolume(Integer.valueOf(0).byteValue());
        this.channel.allSoundOff();
        this.channel.resetAllControllers();
    }

    public void noteOn(BlockPos notePos) {
        this.lastNoteTime = Instant.now();
    }

    public void allNotesOff() {
        if(this.channel != null) {
            this.channel.resetAllControllers();
            this.channel.allSoundOff();
        }
    }

    public void controlChange(Byte controller, Byte value) {
        this.channel.controlChange(controller, value);
    }

    public void setVolume(Byte volume) {
        this.channel.controlChange(7, volume);
    }

    public void setLRPan(Byte lrPan) {
        this.channel.controlChange(10, lrPan);
    }

    public Boolean tick(Player clientPlayer, Boolean isClientChannel) {
        if(!this.isIdle()) {
            return true;
        } else if(this.lastNoteTime != null) {
            return false;
        } else {
            return null;
        }
    }
    
    public Integer getChannelNumber() {
        return this.channelNum;
    }
    
    protected Boolean isIdle() {
        if(lastNoteTime != null) {
            return Math.abs(ChronoUnit.SECONDS.between(Instant.now(), lastNoteTime)) > MIDI_CHANNEL_IDLE_SECONDS;
        }
        return true;
    }
}
