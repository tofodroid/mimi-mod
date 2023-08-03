package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.Soundbank;

import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import net.minecraft.world.entity.player.Player;

public class MechanicalMaestroMIMISynth extends AMIMISynth<PositionalMIMIChannel> {
    public MechanicalMaestroMIMISynth(Boolean jitterCorrection, Integer latency, Soundbank sounds) {
        super(jitterCorrection, latency, sounds);
    }

    @Override
    public Boolean tick(Player clientPlayer) {
        if(!this.channelAssignmentMap.isEmpty()) {
            // Tick channels
            List<PositionalMIMIChannel> toRemove = new ArrayList<>();
            for(PositionalMIMIChannel channel : channelAssignmentMap.keySet()) {
                if(!channel.tick(clientPlayer, false)) {
                    toRemove.add(channel);
                }
            }
    
            // Unassign idle channels
            for(PositionalMIMIChannel remove : toRemove) {
                channelAssignmentMap.remove(remove);
                remove.reset();
            }

            return true;
        }
        
        return false;
    }

    @Override
    protected PositionalMIMIChannel createChannel(Integer num, MidiChannel channel) {
        return new PositionalMIMIChannel(num,channel);
    }

    @Override
    protected String createChannelId(MidiNotePacket message) {
        return message.pos.toShortString() + "$" + message.instrumentId.toString();
    }
}
