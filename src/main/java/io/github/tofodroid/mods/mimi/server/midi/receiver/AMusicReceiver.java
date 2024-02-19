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

    public MidiNotePacket handlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        if(isPacketInRange(packet, sourcePos)) {
            if(willHandlePacket(packet, sourceId, sourcePos, sourceLevel)) {
                return this.doHandlePacket(packet, sourceId, sourcePos, sourceLevel);
            }
        }
        return null;
    }
    
    protected abstract Boolean willHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel);
    protected abstract MidiNotePacket doHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel);
}
