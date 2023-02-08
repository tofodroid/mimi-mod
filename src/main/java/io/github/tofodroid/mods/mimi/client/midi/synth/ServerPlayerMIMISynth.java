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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class ServerPlayerMIMISynth extends AMIMISynth<MIMIChannel> {
    protected SoftSynthesizer internalSynth;
    
    public ServerPlayerMIMISynth(Boolean jitterCorrection, Integer latency, Soundbank sounds)  {
       super(jitterCorrection, latency, sounds);
    }

    public Boolean tick(Player clientPlayer) {
        if(!this.channelAssignmentMap.isEmpty()) {
            Map<UUID,Pair<Byte,Byte>> newChannelParamsMap = new HashMap<>();
            Map<UUID,List<Byte>> playerInstrumentsMap = new HashMap<>();
            List<MIMIChannel> toRemove = new ArrayList<>();

            for(Entry<MIMIChannel,String> entry : channelAssignmentMap.entrySet()) {
                Boolean playerExists = true;
                UUID playerId = getUUIDFromChannelId(entry.getValue());
                Byte instrumentId = getInstrumentIdFromChannelId(entry.getValue());

                // Generate new data for players we haven't already seen
                if(newChannelParamsMap.get(playerId) == null) {
                    Player assignedPlayer = clientPlayer.level.getPlayerByUUID(playerId);

                    if(assignedPlayer == null) {
                        toRemove.add(entry.getKey());
                        playerExists = false;
                    } else {
                        TileInstrument instrumentTile = BlockInstrument.getTileInstrumentForEntity(assignedPlayer);
                        newChannelParamsMap.put(playerId, new ImmutablePair<>(
                            clientPlayer.getUUID() != playerId ? 
                                MIMISynthUtils.getVolumeForRelativeNotePosition(clientPlayer.getOnPos(), assignedPlayer.getOnPos()) :
                                MIMISynthUtils.getVolumeForRelativeNoteDistance(0d),
                            clientPlayer.getUUID() != playerId ? 
                                MIMISynthUtils.getLRPanForRelativeNotePosition(clientPlayer.getOnPos(), assignedPlayer.getOnPos(), clientPlayer.getYHeadRot())
                                : null
                        ));
                        playerInstrumentsMap.put(playerId, Arrays.asList(
                            ItemInstrument.getEntityHeldInstrumentId(assignedPlayer, InteractionHand.MAIN_HAND),
                            ItemInstrument.getEntityHeldInstrumentId(assignedPlayer, InteractionHand.OFF_HAND),
                            instrumentTile != null ? instrumentTile.getInstrumentId() : null
                        ));
                    }
                }

                if(!playerExists || !entry.getKey().tick(clientPlayer) || (instrumentId == null || !playerInstrumentsMap.get(playerId).contains(instrumentId))) {
                    toRemove.add(entry.getKey());
                } else {
                    Pair<Byte,Byte> newParams = newChannelParamsMap.get(playerId);
                    entry.getKey().setVolume(newParams.getLeft());
                    if(newParams.getRight() != null) entry.getKey().setLRPan(newParams.getRight());
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
