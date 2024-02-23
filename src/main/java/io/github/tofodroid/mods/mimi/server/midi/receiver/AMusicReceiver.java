package io.github.tofodroid.mods.mimi.server.midi.receiver;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.midi.TransmitterNoteEvent;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public abstract class AMusicReceiver {
    protected UUID linkedId;
    protected UUID id;
    protected Supplier<BlockPos> blockPos;
    protected Supplier<ResourceKey<Level>> dimension;
    protected Integer enabledChannels;
    protected List<Byte> enabledChannelsList;

    public AMusicReceiver(UUID linkedId, Integer enabledChannels, List<Byte> enabledChannelsList, Supplier<BlockPos> pos, Supplier<ResourceKey<Level>> dim) {
        this.linkedId = linkedId;
        this.blockPos = pos;
        this.id = UUID.randomUUID();
        this.dimension = dim;
        this.enabledChannels = enabledChannels;
        this.enabledChannelsList = enabledChannelsList;
    }

    public UUID getLinkedId() {
        return this.linkedId;
    }
    
    protected Boolean isPacketInRange(TransmitterNoteEvent packet, BlockPos sourcePos) {
        return Math.abs(Math.sqrt(sourcePos.distSqr(blockPos.get()))) <= (packet.isNoteOffEvent() ? 32 : 32);
    }

    protected Boolean isPacketChannelEnabled(TransmitterNoteEvent packet) {
        return MidiNbtDataUtils.isChannelEnabled(this.enabledChannels, packet.channel);
    }

    protected Boolean isPacketSameLevel(TransmitterNoteEvent packet, ServerLevel sourceLevel) {
        return sourceLevel.dimension().equals(dimension.get());
    }

    public MidiNotePacket handleNoteOnPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        if(willHandleNoteOnPacket(packet, sourceId, sourcePos, sourceLevel) && isPacketInRange(packet, sourcePos)) {
            return this.doHandleNoteOnPacket(packet, sourceId, sourcePos, sourceLevel);
        }
        return null;
    }

    public MidiNotePacket handleNoteOffPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        if(willHandleNoteOffPacket(packet, sourceId, sourcePos, sourceLevel) && isPacketInRange(packet, sourcePos)) {
            return this.doHandleNoteOffPacket(packet, sourceId, sourcePos, sourceLevel);
        }
        return null;
    }

    public MidiNotePacket handleAllNotesOffPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        if(willHandleAllNotesOffPacket(packet, sourceId, sourcePos, sourceLevel) && isPacketInRange(packet, sourcePos)) {
            return this.doHandleAllNotesOffPacket(packet, sourceId, sourcePos, sourceLevel);
        }
        return null;
    }
    
    protected abstract Boolean willHandleNoteOnPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel);
    protected abstract MidiNotePacket doHandleNoteOnPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel);
    protected abstract Boolean willHandleNoteOffPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel);
    protected abstract MidiNotePacket doHandleNoteOffPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel);
    protected abstract Boolean willHandleAllNotesOffPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel);
    protected abstract MidiNotePacket doHandleAllNotesOffPacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel);
}
