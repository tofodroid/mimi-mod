package io.github.tofodroid.mods.mimi.server.midi.receiver;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.midi.TransmitterNoteEvent;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class ReceiverTileReceiver extends AMusicReceiver {
    AConfigurableMidiNoteResponsiveTile tile;

    public ReceiverTileReceiver(TileReceiver tile) {        
        super(MidiNbtDataUtils.getMidiSource(tile.getSourceStack()), tile.getEnabledChannelsInt(), tile.getEnabledChannelsList(), tile::getBlockPos, () -> tile.getLevel().dimension());
        this.tile = tile;
    }

    @Override
    protected MidiNotePacket doHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {        
        if(packet.isNoteOnEvent()) {
            tile.onNoteOn(packet.channel, packet.note, packet.velocity, null, packet.noteServerTime);
        } else if(packet.isNoteOffEvent()) {
            tile.onNoteOff(packet.channel, packet.note, packet.velocity, null);
        } else if(packet.isAllNotesOffEvent()) {
            tile.onAllNotesOff(packet.channel, null);
        }

        return null;
    }

    @Override
    protected Boolean willHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        if(packet.isControlEvent()) {
            return false;
        }

        if(packet.isNoteOnEvent()) {
            return tile.shouldTriggerFromNoteOn(packet.channel, packet.note, packet.velocity, null);
        } else if(packet.isNoteOffEvent()) {
            return tile.shouldTriggerFromNoteOff(packet.channel, packet.note, packet.velocity, null);
        } else if(packet.isAllNotesOffEvent()) {
            return tile.shouldTriggerFromAllNotesOff(packet.channel, null);
        }
        return false;
    }
}
