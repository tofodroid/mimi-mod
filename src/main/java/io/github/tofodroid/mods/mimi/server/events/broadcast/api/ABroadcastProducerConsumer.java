package io.github.tofodroid.mods.mimi.server.events.broadcast.api;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class ABroadcastProducerConsumer implements IBroadcastProducer, IBroadcastConsumer {
    public static final Byte ALL_CHANNELS_ID = Byte.MAX_VALUE;
    
    protected UUID ownerId;
    protected UUID linkedId;
    protected Supplier<BlockPos> blockPos;
    protected Supplier<ResourceKey<Level>> dimension;
    protected Integer enabledChannels;
    protected List<Byte> enabledChannelsList;
    protected BroadcastConsumerMapping consumerCache;

    public ABroadcastProducerConsumer(UUID ownerId, UUID linkedId, Integer enabledChannels, List<Byte> enabledChannelsList, Supplier<BlockPos> pos, Supplier<ResourceKey<Level>> dimension) {
        this.ownerId = ownerId;
        this.linkedId = linkedId;
        this.blockPos = pos;
        this.dimension = dimension;
        this.enabledChannels = enabledChannels;
        this.enabledChannelsList = enabledChannelsList;
    }

    public ABroadcastProducerConsumer(UUID ownerId, UUID linkedId, Integer enabledChannels, List<Byte> enabledChannelsList, BlockPos pos, ResourceKey<Level> dimension) {
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

    public BlockPos getBlockPos() {
        return this.blockPos.get();
    }

    public List<Byte> getEnabledChannelsList() {
        return this.enabledChannelsList;
    }

    public BroadcastConsumerMapping getConsumers() {
        return this.consumerCache;
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

    public void linkConsumers(List<IBroadcastConsumer> consumers) {
        this.consumerCache = new BroadcastConsumerMapping(this.ownerId, consumers);
    }


    public void tickProducer() {/*Default no-op*/}
    public void tickConsumer() {/*Default no-op*/}
    public void close() throws Exception {this.onProducerRemoved(); this.onConsumerRemoved();}
}