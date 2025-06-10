package io.github.tofodroid.mods.mimi.server.events.note.api;

import io.github.tofodroid.mods.mimi.common.api.event.note.NoteEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface INoteConsumer {
    public static final Byte ALL_INSTRUMENTS_ID = -1;

    // Data
    default public Boolean isEventInRange(NoteEvent event) {
        return event.dimension.equals(getDimension()) && (event.range < 0 || Math.floor(Math.abs(Math.sqrt(event.pos.distSqr(getBlockPos())))) <= event.range);
    }

    public abstract BlockPos getBlockPos();
    public abstract ResourceKey<Level> getDimension();
    public abstract Byte getInstrumentId();

    // Lifecycle
    public abstract void tickConsumer();
    public abstract void onConsumerRemoved();
    
    // Note Events
    default public void consumeEvent(NoteEvent message) {
        if(willHandleEvent(message) && isEventInRange(message)) {
            this.doHandleEvent(message);
        }
    }

    public abstract Boolean willHandleEvent(NoteEvent message);
    public abstract void doHandleEvent(NoteEvent message);
}
