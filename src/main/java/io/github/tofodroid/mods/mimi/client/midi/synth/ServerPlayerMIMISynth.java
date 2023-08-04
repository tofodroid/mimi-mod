package io.github.tofodroid.mods.mimi.client.midi.synth;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
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

public class ServerPlayerMIMISynth extends AMIMISynth<PositionalMIMIChannel> {
    protected SoftSynthesizer internalSynth;
    
    public ServerPlayerMIMISynth(Boolean jitterCorrection, Integer latency, Soundbank sounds)  {
       super(jitterCorrection, latency, sounds);
    }

    public Boolean tick(Player clientPlayer) {
        if(!this.channelAssignmentMap.isEmpty()) {
            Map<UUID,List<Byte>> playerInstrumentsMap = new HashMap<>();
            List<PositionalMIMIChannel> toRemove = new ArrayList<>();

            for(Entry<PositionalMIMIChannel,String> entry : channelAssignmentMap.entrySet()) {
                Boolean playerExists = true;
                UUID playerId = getUUIDFromChannelId(entry.getValue());
                Byte instrumentId = getInstrumentIdFromChannelId(entry.getValue());

                // Generate new data for players we haven't already seen
                if(playerInstrumentsMap.get(playerId) == null) {
                    Player assignedPlayer = clientPlayer.level().getPlayerByUUID(playerId);

                    if(assignedPlayer == null) {
                        toRemove.add(entry.getKey());
                        playerExists = false;
                    } else {
                        TileInstrument instrumentTile = BlockInstrument.getTileInstrumentForEntity(assignedPlayer);
                        playerInstrumentsMap.put(playerId, Arrays.asList(
                            ItemInstrumentHandheld.getEntityHeldInstrumentId(assignedPlayer, InteractionHand.MAIN_HAND),
                            ItemInstrumentHandheld.getEntityHeldInstrumentId(assignedPlayer, InteractionHand.OFF_HAND),
                            instrumentTile != null ? instrumentTile.getInstrumentId() : null
                        ));
                    }
                }

                if(!playerExists || !entry.getKey().tick(clientPlayer, playerId.toString().equals(clientPlayer.getUUID().toString())) || (instrumentId == null || !playerInstrumentsMap.get(playerId).contains(instrumentId))) {
                    toRemove.add(entry.getKey());
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
