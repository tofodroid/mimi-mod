package io.github.tofodroid.mods.mimi.server.events.broadcast.api;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class ABroadcastProducer implements IBroadcastProducer {
    protected final UUID ownerId;
    protected final Supplier<BlockPos> blockPos;
    protected final Supplier<ResourceKey<Level>> dimension;
    protected BroadcastConsumerMapping consumerCache;

    public ABroadcastProducer(UUID ownerId, Supplier<BlockPos> blockPos, Supplier<ResourceKey<Level>> dimension) {
        this.ownerId = ownerId;
        this.blockPos = blockPos;
        this.dimension = dimension;
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
    
    public BroadcastConsumerMapping getConsumers() {
        return this.consumerCache;
    }

    @Override
    public void linkConsumers(List<IBroadcastConsumer> consumers) {
        this.consumerCache = new BroadcastConsumerMapping(this.ownerId, consumers);
    }

    public void tickProducer() {/*Default no-op*/}
    public void close() throws Exception {this.onProducerRemoved();}
}
