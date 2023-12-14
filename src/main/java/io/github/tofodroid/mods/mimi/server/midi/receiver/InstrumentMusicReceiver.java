package io.github.tofodroid.mods.mimi.server.midi.receiver;

import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.midi.TransmitterNoteEvent;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacketHandler;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

public class InstrumentMusicReceiver extends AMusicReceiver {
    private Byte instrumentId;
    private UUID notePlayerId;
    protected Integer enabledChannels;
    protected List<Byte> filteredNotes;
    protected Byte filteredInstrument;
    protected Boolean invertFilterNoteOct;
    protected Boolean invertFilterInstrument;

    public InstrumentMusicReceiver(BlockPos pos, ResourceKey<Level> dim, UUID notePlayerId, ItemStack instrumentStack) {
        super(InstrumentDataUtils.getMidiSource(instrumentStack), pos, dim);
        this.notePlayerId = notePlayerId;
        this.instrumentId = InstrumentDataUtils.getInstrumentId(instrumentStack);
        this.enabledChannels = InstrumentDataUtils.getEnabledChannelsInt(instrumentStack);
        this.filteredNotes = InstrumentDataUtils.getFilterNotes(InstrumentDataUtils.getFilterNote(instrumentStack), InstrumentDataUtils.getFilterOct(instrumentStack));
        this.filteredInstrument = InstrumentDataUtils.getFilterInstrument(instrumentStack);
        this.invertFilterInstrument = InstrumentDataUtils.getInvertInstrument(instrumentStack);
        this.invertFilterNoteOct = InstrumentDataUtils.getInvertNoteOct(instrumentStack);
    }

    @Override
    protected Boolean willHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        return Math.abs(Math.sqrt(sourcePos.distSqr(getPos()))) <= (packet.isNoteOffEvent() ? 32 : 16) && sourceLevel.dimension().equals(getDimension()) && InstrumentDataUtils.isChannelEnabled(this.enabledChannels, packet.channel);
    }

    @Override
    protected void doHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        MidiNotePacket notePacket;

        if(packet.isControlEvent()) {
            notePacket = MidiNotePacket.createControlPacket(packet.getControllerNumber(), packet.getControllerValue(), instrumentId, notePlayerId, getPos(), packet.noteServerTime);
        } else {
            notePacket = MidiNotePacket.createNotePacket(packet.note, packet.velocity, instrumentId, notePlayerId, getPos(), packet.noteServerTime);
        }

        ServerLifecycleHooks.getCurrentServer().execute(
            () -> MidiNotePacketHandler.handlePacketServer(notePacket, sourceLevel, null)
        );
    }
}
