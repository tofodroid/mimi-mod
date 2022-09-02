package io.github.tofodroid.mods.mimi.client.midi.synth;

import javax.sound.midi.MidiChannel;

import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class PositionalMIMIChannel extends MIMIChannel{
    protected BlockPos lastNotePos;

    public PositionalMIMIChannel(Integer channelNum, MidiChannel channel) {
        super(channelNum, channel);
    }

    @Override
    public void noteOn(InstrumentSpec instrument, Byte note, Byte velocity, BlockPos notePos) {
        super.noteOn(instrument, note, velocity, notePos);
        this.lastNotePos = notePos;
    }

    @Override
    public Boolean tick(Player clientPlayer) {
        if(!this.isIdle() && this.lastNotePos != null  && Math.sqrt(clientPlayer.getOnPos().distSqr(lastNotePos)) <= 72d) {
            this.channel.controlChange(7, MIMISynthUtils.getVolumeForRelativeNotePosition(clientPlayer.getOnPos(), lastNotePos));
            this.channel.controlChange(10, MIMISynthUtils.getLRPanForRelativeNotePosition(clientPlayer.getOnPos(), lastNotePos, clientPlayer.getYHeadRot()));
            return true;
        } else if(this.lastNoteTime != null) {
            return false;
        } else {
            return null;
        }
    }
}
