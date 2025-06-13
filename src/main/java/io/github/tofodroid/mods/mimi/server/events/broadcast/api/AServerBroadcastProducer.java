package io.github.tofodroid.mods.mimi.server.events.broadcast.api;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.api.event.broadcast.BroadcastConsumerMapping;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.IBroadcastConsumer;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.IBroadcastProducer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class AServerBroadcastProducer implements IBroadcastProducer {
    protected final UUID ownerId;
    protected final Supplier<BlockPos> blockPos;
    protected final Supplier<ResourceKey<Level>> dimension;
    protected BroadcastConsumerMapping consumerCache;

    public AServerBroadcastProducer(UUID ownerId, Supplier<BlockPos> blockPos, Supplier<ResourceKey<Level>> dimension) {
        this.ownerId = ownerId;
        this.blockPos = blockPos;
        this.dimension = dimension;
    }

    @Override
    public UUID getOwnerId() {
        return this.ownerId;
    }
    
    @Override
    public ResourceKey<Level> getBroadcastDimension() {
        return this.dimension.get();
    }

    @Override
    public BlockPos getBroadcastPos() {
        return this.blockPos.get();
    }
    
    @Override
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
