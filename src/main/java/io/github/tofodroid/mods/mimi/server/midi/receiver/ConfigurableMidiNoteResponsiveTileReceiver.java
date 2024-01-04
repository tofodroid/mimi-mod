package io.github.tofodroid.mods.mimi.server.midi.receiver;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.midi.TransmitterNoteEvent;
import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class ConfigurableMidiNoteResponsiveTileReceiver extends AMusicReceiver {
    AConfigurableMidiNoteResponsiveTile tile;

    @SuppressWarnings("null")
    public ConfigurableMidiNoteResponsiveTileReceiver(AConfigurableMidiNoteResponsiveTile tile) {        
        super(InstrumentDataUtils.getMidiSource(tile.getSourceStack()), tile::getBlockPos, () -> tile.getLevel().dimension());
        this.tile = tile;
    }

    @Override
    protected void doHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        tile.onTrigger(packet.channel, packet.note, packet.velocity, null);
    }

    @Override
    protected Boolean willHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        return Math.abs(Math.sqrt(sourcePos.distSqr(blockPos.get()))) <= (packet.isNoteOffEvent() ? 32 : 16) && sourceLevel.dimension().equals(dimension.get()) && 
                tile.shouldTriggerFromMidiEvent(packet.channel, packet.note, packet.velocity, null);
    }
}
