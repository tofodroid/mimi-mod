package io.github.tofodroid.mods.mimi.server.events.broadcast.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

public class BroadcastConsumerHolder {
    private final UUID ownerId;
    private final Map<Integer, ABroadcastConsumer> holderMap = new Int2ObjectArrayMap<>();

    public BroadcastConsumerHolder(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public Boolean isEmpty() {
        return holderMap.isEmpty();
    }

    public void putConsumer(Integer slot, ABroadcastConsumer consumer) {
        this.holderMap.put(slot, consumer);
    }

    public ABroadcastConsumer removeConsumer(Integer slot) {
        return this.holderMap.remove(slot);
    }

    public ABroadcastConsumer getConsumer(Integer slot) {
        return holderMap.get(slot);
    }

    public List<ABroadcastConsumer> getConsumers() {
        if(holderMap.isEmpty()) {
            return new ArrayList<>(0);
        }
        return new ArrayList<>(holderMap.values());
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }
}
