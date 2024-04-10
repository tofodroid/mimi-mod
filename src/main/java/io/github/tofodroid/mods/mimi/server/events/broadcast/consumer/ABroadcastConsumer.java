package io.github.tofodroid.mods.mimi.server.events.broadcast.consumer;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class ABroadcastConsumer {
    public static final Byte ALL_CHANNELS_ID = Byte.MAX_VALUE;
    
    protected UUID ownerId;
    protected UUID linkedId;
    protected Supplier<BlockPos> blockPos;
    protected Supplier<ResourceKey<Level>> dimension;
    protected Integer enabledChannels;
    protected List<Byte> enabledChannelsList;

    public ABroadcastConsumer(UUID ownerId, UUID linkedId, Integer enabledChannels, List<Byte> enabledChannelsList, Supplier<BlockPos> pos, Supplier<ResourceKey<Level>> dimension) {
        this.ownerId = ownerId;
        this.linkedId = linkedId;
        this.blockPos = pos;
        this.dimension = dimension;
        this.enabledChannels = enabledChannels;
        this.enabledChannelsList = enabledChannelsList;
    }

    public ABroadcastConsumer(UUID ownerId, UUID linkedId, Integer enabledChannels, List<Byte> enabledChannelsList, BlockPos pos, ResourceKey<Level> dimension) {
        this(ownerId, linkedId, enabledChannels, enabledChannelsList, () -> pos, () -> dimension);
    }

    public UUID getLinkedId() {
        return this.linkedId;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public ResourceKey<Level> getDimension() {
        return this.dimension.get();
    }

    public List<Byte> getEnabledChannelsList() {
        return this.enabledChannelsList;
    }

    public List<ABroadcastConsumer> getConsumers() {
        return List.of(this);
    }

    protected Boolean isPacketInRange(BroadcastEvent packet) {
        return Math.abs(Math.sqrt(packet.pos.distSqr(blockPos.get()))) <= (packet.isNoteOffEvent() ? 32 : 32) && packet.dimension.equals(this.dimension.get());
    }

    public void consumeNoteOn(BroadcastEvent message) {
        if(willHandleNoteOn(message) && isPacketInRange(message)) {
            this.doHandleNoteOn(message);
        }
    }

    public void consumeNoteOff(BroadcastEvent message) {
        if(willHandleNoteOff(message) && isPacketInRange(message)) {
            this.doHandleNoteOff(message);
        }
    }

    public void consumeAllNotesOff(BroadcastEvent message) {
        if(willHandleAllNotesOff(message) && isPacketInRange(message)) {
            this.doHandleAllNotesOff(message);
        }
    }
    
    public void tick() {/* Default no-op */}

    public abstract void onRemoved();
    protected abstract Boolean willHandleNoteOn(BroadcastEvent message);
    protected abstract void doHandleNoteOn(BroadcastEvent message);
    protected abstract Boolean willHandleNoteOff(BroadcastEvent message);
    protected abstract void doHandleNoteOff(BroadcastEvent message);
    protected abstract Boolean willHandleAllNotesOff(BroadcastEvent message);
    protected abstract void doHandleAllNotesOff(BroadcastEvent message);
}
