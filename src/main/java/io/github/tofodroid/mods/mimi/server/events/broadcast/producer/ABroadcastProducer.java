package io.github.tofodroid.mods.mimi.server.events.broadcast.producer;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.midi.BasicMidiEvent;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastConsumerMapping;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.ABroadcastConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class ABroadcastProducer implements AutoCloseable {
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

    public void linkConsumers(List<ABroadcastConsumer> consumers) {
        this.consumerCache = new BroadcastConsumerMapping(this.ownerId, consumers);
    }

    public void reindex() {
        this.consumerCache.reindex();
    }

    public void allNotesOff() {
        this.broadcast(BroadcastEvent.createAllNotesOffEvent(this.ownerId, this.dimension.get(), this.blockPos.get()));
    }

    public void broadcast(BasicMidiEvent event) {
        if(consumerCache != null) {
            BroadcastEvent input = new BroadcastEvent(event, this.ownerId, this.dimension.get(), this.blockPos.get());

            switch(event.type) {
                case NOTE_ON:
                    for(ABroadcastConsumer consumer : consumerCache.getConsumersForChannel(event.channel)) {
                        consumer.consumeNoteOn(input);
                    }
                    break;
                case NOTE_OFF:
                    for(ABroadcastConsumer consumer : consumerCache.getConsumersForChannel(event.channel)) {
                        consumer.consumeNoteOff(input);
                    }
                    break;
                case ALL_NOTES_OFF:
                    for(ABroadcastConsumer consumer : consumerCache.getConsumersForChannel(event.channel)) {
                        consumer.consumeAllNotesOff(input);                        
                    }
                    break;
                default:
                    break;
            }
        }
    }
    
    public void tick() {/* Default no-op */}
}
