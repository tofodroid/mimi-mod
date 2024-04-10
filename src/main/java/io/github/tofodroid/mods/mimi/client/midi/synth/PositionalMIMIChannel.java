package io.github.tofodroid.mods.mimi.client.midi.synth;

import javax.sound.midi.MidiChannel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class PositionalMIMIChannel extends MIMIChannel{
    protected BlockPos lastNotePos;

    public PositionalMIMIChannel(Integer channelNum, MidiChannel channel) {
        super(channelNum, channel);
    }

    @Override
    public void noteOn(BlockPos notePos) {
        super.noteOn(notePos);
        this.lastNotePos = notePos;
    }

    @Override
    public Boolean tick(Player clientPlayer, Boolean isClientChannel) {
        if(!this.isIdle() && this.lastNotePos != null && Math.sqrt(clientPlayer.getOnPos().distSqr(lastNotePos)) <= 72d) {
            if(!isClientChannel) {
                this.channel.controlChange(7, MIMISynthUtils.getVolumeForRelativeNotePosition(clientPlayer.getEyePosition(), lastNotePos));
                this.channel.controlChange(10, MIMISynthUtils.getLRPanForRelativeNotePosition(clientPlayer.getEyePosition(), lastNotePos, clientPlayer.getYHeadRot()));
            } else {
                this.channel.controlChange(7, MIMISynthUtils.getVolumeForRelativeNoteDistance(0d));
            }
            return true;
        } else {
            return false;
        }
    }
}
