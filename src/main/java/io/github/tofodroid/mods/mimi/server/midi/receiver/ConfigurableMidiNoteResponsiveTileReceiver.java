package io.github.tofodroid.mods.mimi.server.midi.receiver;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.midi.TransmitterNoteEvent;
import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class ConfigurableMidiNoteResponsiveTileReceiver extends AMusicReceiver {
    AConfigurableMidiNoteResponsiveTile tile;

    public ConfigurableMidiNoteResponsiveTileReceiver(AConfigurableMidiNoteResponsiveTile tile) {        
        super(MidiNbtDataUtils.getMidiSource(tile.getSourceStack()), tile::getBlockPos, () -> tile.getLevel().dimension());
        this.tile = tile;
    }

    @Override
    protected void doHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        if(packet.isNoteOnEvent()) {
            tile.onNoteOn(packet.channel, packet.note, packet.velocity, null);
        } else if(packet.isNoteOffEvent()) {
            tile.onNoteOff(packet.channel, packet.note, packet.velocity, null);
        } else if(packet.isAllNotesOffEvent()) {
            tile.onAllNotesOff(packet.channel, null);
        }
    }

    @Override
    protected Boolean willHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        if(Math.abs(Math.sqrt(sourcePos.distSqr(blockPos.get()))) <= (packet.isNoteOffEvent() ? 32 : 16) && sourceLevel.dimension().equals(dimension.get())) {
            if(packet.isNoteOnEvent()) {
                return tile.shouldTriggerFromNoteOn(packet.channel, packet.note, packet.velocity, null);
            } else if(packet.isNoteOffEvent()) {
                return tile.shouldTriggerFromNoteOff(packet.channel, packet.note, packet.velocity, null);
            } else if(packet.isAllNotesOffEvent()) {
                return tile.shouldTriggerFromAllNotesOff(packet.channel, null);
            }
        }
        return false;
    }
}
