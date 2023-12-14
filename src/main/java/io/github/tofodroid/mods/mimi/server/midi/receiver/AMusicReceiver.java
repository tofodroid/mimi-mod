package io.github.tofodroid.mods.mimi.server.midi.receiver;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.midi.TransmitterNoteEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public abstract class AMusicReceiver {
    private UUID linkedId;
    private BlockPos blockPos;
    private ResourceKey<Level> dimension;

    public AMusicReceiver(UUID linkedId, BlockPos pos, ResourceKey<Level> dim) {
        this.linkedId = linkedId;
        this.blockPos = pos;
        this.dimension = dim;
    }

    public UUID getLinkedId() {
        return this.linkedId;
    }

    public void handlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        if(willHandlePacket(packet, sourceId, sourcePos, sourceLevel)) {
            this.doHandlePacket(packet, sourceId, sourcePos, sourceLevel);
        }
    }
    

    protected BlockPos getPos() {
        return this.blockPos;
    }

    protected ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    protected abstract Boolean willHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel);
    protected abstract void doHandlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel);
}
