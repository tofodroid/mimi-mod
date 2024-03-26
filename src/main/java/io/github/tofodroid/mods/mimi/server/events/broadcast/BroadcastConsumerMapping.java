package io.github.tofodroid.mods.mimi.server.events.broadcast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.ABroadcastConsumer;

public class BroadcastConsumerMapping {
    protected final UUID broadcasterId;

    @SuppressWarnings("unchecked")
    private List<ABroadcastConsumer>[] CHANNEL_CONSUMERS = (ArrayList<ABroadcastConsumer>[]) new ArrayList[16];
    private List<ABroadcastConsumer> LINKED_CONSUMERS = new ArrayList<>();

    public BroadcastConsumerMapping(UUID broadcasterId, List<ABroadcastConsumer> linkedConsumers) {
        for(int i = 0; i < 16; i++) {
            CHANNEL_CONSUMERS[i] = new ArrayList<>(0);
        }
        
        this.broadcasterId = broadcasterId;
        this.LINKED_CONSUMERS = linkedConsumers;
        this.reindex();
    }

    public void reindex() {
        for(int i = 0; i < 16; i++) {
            CHANNEL_CONSUMERS[i].clear();
        }
        for(ABroadcastConsumer consumer : LINKED_CONSUMERS) {
            for(Byte enabledChannel : consumer.getEnabledChannelsList()) {
                CHANNEL_CONSUMERS[enabledChannel].add(consumer);
            }
        }
    }

    public List<ABroadcastConsumer> getConsumersForChannel(Byte channel) {
        if(channel == BroadcastEvent.ALL_CHANNELS) {
            return LINKED_CONSUMERS;
        }
        return CHANNEL_CONSUMERS[channel];
    }
}
