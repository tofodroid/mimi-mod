package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import io.github.tofodroid.com.sun.media.sound.SoftChannelProxy;
import io.github.tofodroid.mods.mimi.common.api.event.note.NoteEvent;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.util.ByteUtils;
import io.github.tofodroid.mods.mimi.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class MIMIChannel {
    public static final Integer MIDI_CHANNEL_IDLE_SECONDS = 8;
    public static final Integer MAX_NOTE_DIST = 2 * NoteEvent.NOTE_DEF_RANGE;

    protected final SoftChannelProxy channel;
    protected final Integer channelNum;
    protected Instant lastNoteTime;
    protected BlockPos lastNotePos;
    protected Integer pitchBendRange = 2 << 7;

    public MIMIChannel(Integer channelNum, SoftChannelProxy channel) {
        this.channelNum = channelNum;
        this.channel = channel;
        this.setVolume(ByteUtils.ZERO);
        this.reset();
    }

    public void setInstrument(InstrumentSpec instrument) {
        this.channel.programChange(instrument.midiBankNumber * 128, instrument.midiPatchNumber);
        this.setVolume(ByteUtils.ZERO);
        this.reset();
    }

    public void clear() {
        this.lastNoteTime = null;
        this.pitchBendRange = 2 << 7;
        this.setVolume(ByteUtils.ZERO);
        this.reset();
    }

    public void noteOn(BlockPos notePos) {
        this.lastNoteTime = Instant.now();
        this.lastNotePos = notePos;
    }

    public void reset() {
        if(this.channel != null) {
            this.channel.resetAllControllers(true);
            this.channel.allSoundOff();
        }
        this.pitchBendRange = 2 << 7;
    }

    public void setVolume(Byte volume) {
        this.channel.controlChange(7, volume);
    }

    public void setLRPan(Byte lrPan) {
        this.channel.controlChange(10, lrPan);
    }

    public void setPitchBendRange(Integer range) {
        if(range != this.pitchBendRange && this.channel.getChannel() != null) {
            this.channel.getChannel().rpnChange(0, range);
            this.pitchBendRange = range;
        }
    }

    public Boolean tick(Player clientPlayer, Boolean isClientChannel) {
        if(!this.isIdle() && this.lastNotePos != null) {
            double lastNoteDist = Math.abs(Math.sqrt(EntityUtils.getEntityHeadPos(clientPlayer).distSqr(lastNotePos)));

            if(lastNoteDist <= MAX_NOTE_DIST) {
                if(!isClientChannel) {
                    this.channel.controlChange(7, MIMISynthUtils.getVolumeForRelativeNotePosition(clientPlayer.getEyePosition(), lastNotePos));
                    this.channel.controlChange(10, MIMISynthUtils.getLRPanForRelativeNotePosition(clientPlayer.getEyePosition(), lastNotePos, clientPlayer.getYHeadRot()));
                } else {
                    this.channel.controlChange(7, MIMISynthUtils.getVolumeForRelativeNoteDistance(0d));
                    this.channel.controlChange(10, 63);
                }
                return true;
            } else {
                this.reset();
            }
        }
        return false;
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
