package io.github.tofodroid.mods.mimi.server.midi.receiver;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.midi.TransmitterNoteEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public abstract class AMusicReceiver {
    protected UUID linkedId;
    protected UUID id;
    protected Supplier<BlockPos> blockPos;
    protected Supplier<ResourceKey<Level>> dimension;
    protected List<Byte> enabledChannels;

    public AMusicReceiver(UUID linkedId, List<Byte> enabledChannels, Supplier<BlockPos> pos, Supplier<ResourceKey<Level>> dim) {
        this.linkedId = linkedId;
        this.blockPos = pos;
        this.id = UUID.randomUUID();
        this.dimension = dim;
        this.enabledChannels = enabledChannels;
    }

    public UUID getLinkedId() {
        return this.linkedId;
    }

    public void handlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        if(willHandlePacket(packet, sourceId, sourcePos, sourceLevel)) {
            this.doHandlePacket(packet, sourceId, sourcePos, sourceLevel);
        }
    }
    
    protected abstract Boolean willHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel);
    protected abstract void doHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel);
}
