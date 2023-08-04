package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;

import io.github.tofodroid.com.sun.media.sound.SoftSynthesizer;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.Soundbank;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentHandheld;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class LocalPlayerMIMISynth extends AMIMISynth<MIMIChannel> {
    protected SoftSynthesizer internalSynth;
    
    public LocalPlayerMIMISynth(Boolean jitterCorrection, Integer latency, Soundbank sounds)  {
       super(jitterCorrection, latency, sounds);
    }

    public Boolean tick(Player clientPlayer) {
        if(!this.channelAssignmentMap.isEmpty()) {
            // Generate player data
            List<MIMIChannel> toRemove = new ArrayList<>();
            TileInstrument instrumentTile = BlockInstrument.getTileInstrumentForEntity(clientPlayer);
            List<Byte> playerInstruments = Arrays.asList(
                ItemInstrumentHandheld.getEntityHeldInstrumentId(clientPlayer, InteractionHand.MAIN_HAND),
                ItemInstrumentHandheld.getEntityHeldInstrumentId(clientPlayer, InteractionHand.OFF_HAND),
                instrumentTile != null ? instrumentTile.getInstrumentId() : null
            );

            // Tick Channels
            for(Entry<MIMIChannel,String> entry : channelAssignmentMap.entrySet()) {
                Byte instrumentId = Byte.parseByte(entry.getValue());

                if(!entry.getKey().tick(clientPlayer, true) || (instrumentId == null || !playerInstruments.contains(instrumentId))) {
                    toRemove.add(entry.getKey());
                } else {
                    entry.getKey().setVolume(MIMISynthUtils.getVolumeForRelativeNoteDistance(0d));
                }
            }
    
            // Unassign idle channels
            for(MIMIChannel remove : toRemove) {
                channelAssignmentMap.remove(remove);
                remove.reset();
            }


            return true;
        }

        return false;
    }
    
    @Override
    protected MIMIChannel createChannel(Integer num, MidiChannel channel) {
        return new MIMIChannel(num, channel);
    }

    @Override
    protected String createChannelId(MidiNotePacket message) {
        return message.instrumentId.toString();
    }
}
