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
    protected MidiNotePacket doHandleNoteOnPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        tile.onNoteOn(packet.channel, packet.note, packet.velocity, null, packet.noteServerTime);
        return null;
    }
    
    @Override
    protected MidiNotePacket doHandleNoteOffPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        tile.onNoteOff(packet.channel, packet.note, packet.velocity, null);
        return null;
    }

    @Override
    protected MidiNotePacket doHandleAllNotesOffPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        tile.onAllNotesOff(packet.channel, null);
        return null;
    }
    @Override
    protected Boolean willHandleNoteOnPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        return tile.shouldTriggerFromNoteOn(packet.channel, packet.note, packet.velocity, null);
    }

    @Override
    protected Boolean willHandleNoteOffPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        return tile.shouldTriggerFromNoteOff(packet.channel, packet.note, packet.velocity, null);
    }

    @Override
    protected Boolean willHandleAllNotesOffPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        return tile.shouldTriggerFromAllNotesOff(packet.channel, null);
    }
}
