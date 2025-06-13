package io.github.tofodroid.mods.mimi.common.api.event.broadcast;

import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.ServerExecutorProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IBroadcastProducer extends AutoCloseable {
    // Data
    public abstract UUID getOwnerId();
    public abstract BlockPos getBroadcastPos();
    public abstract Integer getBroadcastRange();
    public abstract ResourceKey<Level> getBroadcastDimension();
    public abstract BroadcastConsumerMapping getConsumers();
    public abstract void linkConsumers(List<IBroadcastConsumer> consumers);

    default public void reindex() {
        getConsumers().reindex();
    }
    
    // Lifecycle
    public abstract void tickProducer();
    public abstract void onProducerRemoved();

    // Events
    default public void reset() {
        this.broadcast(BroadcastEvent.reset(getOwnerId(), getBroadcastDimension(), getBroadcastPos(), MIMIMod.getProxy().getCurrentServerMillis()));
    }

    default public void broadcast(BroadcastEvent event) {
        if(event == null) return;

        BroadcastConsumerMapping consumers = getConsumers();

        if(consumers != null && !consumers.isEmpty()) {
            for(IBroadcastConsumer consumer : consumers.getConsumersForChannel(event.channel)) {
                ServerExecutorProxy.executeOnServerThread(() -> consumer.consumeEvent(event));
            }
        }
    }
}
