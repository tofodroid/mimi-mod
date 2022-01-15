package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.sound.midi.MidiChannel;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.util.DebugUtils;
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
        this.channel.programChange(instrument.midiBankNumber, instrument.midiPatchNumber);
        this.setVolume(Integer.valueOf(0).byteValue());
        this.channel.allNotesOff();
    }

    public void reset() {
        this.lastNoteTime = null;
        this.setVolume(Integer.valueOf(0).byteValue());
        this.channel.allNotesOff();
    }

    public void noteOn(InstrumentSpec instrument, Byte note, Byte velocity, BlockPos notePos) {
        channel.noteOn(note, velocity);
        this.lastNoteTime = Instant.now();
        DebugUtils.logNoteTimingInfo(this.getClass(), true, instrument != null ? instrument.instrumentId : MidiNotePacket.ALL_NOTES_OFF, note, velocity, notePos);
    }

    public void noteOff(InstrumentSpec instrument, Byte note) {
        if(MidiNotePacket.ALL_NOTES_OFF.equals(note)) {
            channel.allNotesOff();
        } else { 
            channel.noteOff(note);
        }
        DebugUtils.logNoteTimingInfo(this.getClass(), false, instrument != null ? instrument.instrumentId : MidiNotePacket.ALL_NOTES_OFF, note, null, null);
    }

    public void setVolume(Byte volume) {
        this.channel.controlChange(7, volume);
    }

    public void setLRPan(Byte lrPan) {
        this.channel.controlChange(10, lrPan);
    }

    public Boolean tick(Player clientPlayer) {
        if(!this.isIdle()) {
            return true;
        } else if(this.lastNoteTime != null) {
            return false;
        } else {
            MIMIMod.LOGGER.warn("Attempted to tick improperly channel: " + this.channel.toString());
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
