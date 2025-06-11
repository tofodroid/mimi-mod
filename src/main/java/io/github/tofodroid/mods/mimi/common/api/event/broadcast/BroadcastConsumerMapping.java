package io.github.tofodroid.mods.mimi.common.api.event.broadcast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BroadcastConsumerMapping {
    protected final UUID broadcasterId;

    @SuppressWarnings("unchecked")
    private List<IBroadcastConsumer>[] CHANNEL_CONSUMERS = (ArrayList<IBroadcastConsumer>[]) new ArrayList[16];
    private List<IBroadcastConsumer> LINKED_CONSUMERS = new ArrayList<>();

    public BroadcastConsumerMapping(UUID broadcasterId, List<IBroadcastConsumer> linkedConsumers) {
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
        for(IBroadcastConsumer consumer : LINKED_CONSUMERS) {
            if(consumer.getEnabledChannelsList() != null) {
                for(Byte enabledChannel : consumer.getEnabledChannelsList()) {
                    CHANNEL_CONSUMERS[enabledChannel].add(consumer);
                }
            }
        }
    }

    public Boolean isEmpty() {
        return this.LINKED_CONSUMERS.isEmpty();
    }

    public List<IBroadcastConsumer> getConsumersForChannel(Byte channel) {
        if(channel == BroadcastEvent.ALL_CHANNELS) {
            return LINKED_CONSUMERS;
        }
        return CHANNEL_CONSUMERS[channel];
    }
}
