package io.github.tofodroid.mods.mimi.server.events.broadcast.api;

import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IBroadcastConsumer extends AutoCloseable {
    public static final Byte ALL_CHANNELS_ID = Byte.MAX_VALUE;
    public static final Integer NOTE_OFF_EVENT_RANGE_ADD = 16;

    // Data
    default public Boolean isPacketInRange(BroadcastEvent packet) {
        return Math.abs(Math.sqrt(packet.pos.distSqr(getBlockPos()))) <= (packet.isNoteOffEvent() ? packet.range + NOTE_OFF_EVENT_RANGE_ADD : packet.range) && packet.dimension.equals(getDimension());
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
    default public void consumeNoteOn(BroadcastEvent message) {
        if(willHandleNoteOn(message) && isPacketInRange(message)) {
            this.doHandleNoteOn(message);
        }
    }

    default public void consumeNoteOff(BroadcastEvent message) {
        if(willHandleNoteOff(message) && isPacketInRange(message)) {
            this.doHandleNoteOff(message);
        }
    }

    default public void consumeAllNotesOff(BroadcastEvent message) {
        if(willHandleAllNotesOff(message) && isPacketInRange(message)) {
            this.doHandleAllNotesOff(message);
        }
    }

    public abstract Boolean willHandleNoteOn(BroadcastEvent message);
    public abstract void doHandleNoteOn(BroadcastEvent message);
    public abstract Boolean willHandleNoteOff(BroadcastEvent message);
    public abstract void doHandleNoteOff(BroadcastEvent message);
    public abstract Boolean willHandleAllNotesOff(BroadcastEvent message);
    public abstract void doHandleAllNotesOff(BroadcastEvent message);
}
