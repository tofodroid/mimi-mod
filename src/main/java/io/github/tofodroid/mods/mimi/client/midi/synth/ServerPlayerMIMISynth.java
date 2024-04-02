package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import io.github.tofodroid.com.sun.media.sound.SoftSynthesizer;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.Soundbank;

import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import net.minecraft.world.entity.player.Player;

public class ServerPlayerMIMISynth extends AMIMISynth<PositionalMIMIChannel> {
    protected SoftSynthesizer internalSynth;
    
    public ServerPlayerMIMISynth(Boolean jitterCorrection, Integer latency, Soundbank sounds)  {
       super(jitterCorrection, latency, sounds);
    }

    public Boolean tick(Player clientPlayer) {
        if(this.channelAssignmentMap != null && !this.channelAssignmentMap.isEmpty()) {
            // Tick channels
            List<PositionalMIMIChannel> toRemove = new ArrayList<>();
            for(PositionalMIMIChannel channel : channelAssignmentMap.keySet()) {
                UUID playerId = getUUIDFromChannelId(channelAssignmentMap.get(channel));

                if(!channel.tick(clientPlayer, playerId.toString().equals(clientPlayer.getUUID().toString()))) {
                    toRemove.add(channel);
                }
            }
    
            // Unassign idle channels
            for(PositionalMIMIChannel remove : toRemove) {
                remove.reset();
                channelAssignmentMap.remove(remove);
            }

            return true;
        }
        
        return false;
    }
    
    @Override
    protected PositionalMIMIChannel createChannel(Integer num, MidiChannel channel) {
        return new PositionalMIMIChannel(num, channel);
    }

    @Override
    protected String createChannelId(MidiNotePacket message) {
        return getChannelIdForUUIDAndInstrumentId(message.player, message.instrumentId);
    }

    protected UUID getUUIDFromChannelId(String channelId) {
        return UUID.fromString(channelId.substring(0, channelId.indexOf("$")));
    }

    protected Byte getInstrumentIdFromChannelId(String channelId) {
        return Byte.valueOf(channelId.substring(channelId.indexOf("$")+1));
    }

    protected String getChannelIdForUUIDAndInstrumentId(UUID id, Byte instrumentId) {
        return id.toString() + "$" + instrumentId.toString();
    }

}
