package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import io.github.tofodroid.com.sun.media.sound.SoftChannelProxy;
import io.github.tofodroid.com.sun.media.sound.SoftSynthesizer;

import javax.sound.midi.Soundbank;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

import io.github.tofodroid.mods.mimi.common.network.NoteEventPacket;
import net.minecraft.world.entity.player.Player;

public class BroadcastedNoteMIMISynth extends AMIMISynth<MIMIChannel> {
    protected SoftSynthesizer internalSynth;
    
    public BroadcastedNoteMIMISynth(AudioFormat format, SourceDataLine dataLine, Boolean jitterCorrection, Integer latency, Soundbank sounds)  {
       super(format, dataLine, jitterCorrection, latency, sounds);
    }

    public Boolean tick(Player clientPlayer) {
        if(this.channelAssignmentMap != null && !this.channelAssignmentMap.isEmpty()) {
            // Tick channels
            List<MIMIChannel> toRemove = new ArrayList<>();
            for(MIMIChannel channel : channelAssignmentMap.keySet()) {
                UUID playerId = getUUIDFromChannelId(channelAssignmentMap.get(channel));

                if(!channel.tick(clientPlayer, playerId.toString().equals(clientPlayer.getUUID().toString()))) {
                    toRemove.add(channel);
                }
            }
    
            // Unassign idle channels
            for(MIMIChannel remove : toRemove) {
                remove.clear();
                channelAssignmentMap.remove(remove);
            }

            return true;
        }
        
        return false;
    }
    
    @Override
    protected MIMIChannel createChannel(Integer num, SoftChannelProxy channel) {
        return new MIMIChannel(num, channel);
    }

    @Override
    protected String createChannelId(NoteEventPacket message) {
        return message.player.toString() + "$" + message.instrumentId.toString() + "$" + message.channel.toString();
    }

    protected UUID getUUIDFromChannelId(String channelId) {
        return UUID.fromString(channelId.substring(0, channelId.indexOf("$")));
    }

    protected Byte getInstrumentIdFromChannelId(String channelId) {
        return Byte.valueOf(channelId.substring(channelId.indexOf("$")+1, channelId.lastIndexOf("$")));
    }

    protected Byte getChannelFromChannelId(String channelId) {
        return Byte.valueOf(channelId.substring(channelId.lastIndexOf("$")+1));
    }
}
