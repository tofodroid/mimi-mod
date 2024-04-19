package io.github.tofodroid.mods.mimi.server.events.broadcast.api;

import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.midi.BasicMidiEvent;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IBroadcastProducer extends AutoCloseable {
    // Data
    public abstract UUID getOwnerId();
    public abstract BlockPos getBlockPos();
    public abstract Integer getBroadcastRange();
    public abstract ResourceKey<Level> getDimension();
    public abstract BroadcastConsumerMapping getConsumers();
    public abstract void linkConsumers(List<IBroadcastConsumer> consumers);

    default public void reindex() {
        getConsumers().reindex();
    }
    
    // Lifecycle
    public abstract void tickProducer();
    public abstract void onProducerRemoved();

    // Events
    default public void allNotesOff() {
        this.broadcast(BroadcastEvent.createAllNotesOffEvent(getOwnerId(), getDimension(), getBlockPos(), getBroadcastRange()));
    }

    default public void broadcast(BasicMidiEvent event) {
        BroadcastConsumerMapping consumers = getConsumers();

        if(consumers != null && !consumers.isEmpty()) {
            BroadcastEvent input = new BroadcastEvent(event, getOwnerId(), getDimension(), getBlockPos(), getBroadcastRange());

            switch(event.type) {
                case NOTE_ON:
                    for(IBroadcastConsumer consumer : consumers.getConsumersForChannel(event.channel)) {
                        consumer.consumeNoteOn(input);
                    }
                    break;
                case NOTE_OFF:
                    for(IBroadcastConsumer consumer : consumers.getConsumersForChannel(event.channel)) {
                        consumer.consumeNoteOff(input);
                    }
                    break;
                case ALL_NOTES_OFF:
                    for(IBroadcastConsumer consumer : consumers.getConsumersForChannel(event.channel)) {
                        consumer.consumeAllNotesOff(input);                        
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
