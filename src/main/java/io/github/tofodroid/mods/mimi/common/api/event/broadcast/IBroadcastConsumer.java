package io.github.tofodroid.mods.mimi.common.api.event.broadcast;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IBroadcastConsumer extends AutoCloseable {
    public static final Byte ALL_CHANNELS_ID = Byte.MAX_VALUE;

    // Data
    default public Boolean isEventInRange(BroadcastEvent event) {
        return event.dimension.equals(getDimension()) && (event.range < 0 || Math.floor(Math.abs(Math.sqrt(event.pos.distSqr(getBlockPos())))) <= event.range);
    }

    public abstract UUID getLinkedId();
    public abstract UUID getOwnerId();
    public abstract List<Byte> getEnabledChannelsList();
    public abstract BlockPos getBlockPos();
    public abstract ResourceKey<Level> getDimension();

    // Lifecycle
    public abstract void tickConsumer();
    public abstract void onConsumerRemoved();
    
    // Broadcast Events
    default public void consumeEvent(BroadcastEvent message) {
        if(willHandleEvent(message) && isEventInRange(message)) {
            this.doHandleEvent(message);
        }
    }

    public abstract Boolean willHandleEvent(BroadcastEvent message);
    public abstract void doHandleEvent(BroadcastEvent message);
}
