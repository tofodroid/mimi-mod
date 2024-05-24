package io.github.tofodroid.mods.mimi.server.events.broadcast.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

public class BroadcastConsumerInventoryHolder {
    private final UUID ownerId;
    private final Map<Integer, IBroadcastConsumer> holderMap = new Int2ObjectArrayMap<>();

    public BroadcastConsumerInventoryHolder(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public Boolean isEmpty() {
        return holderMap.isEmpty();
    }

    public void putConsumer(Integer slot, IBroadcastConsumer consumer) {
        this.holderMap.put(slot, consumer);
    }

    public IBroadcastConsumer removeConsumer(Integer slot) {
        return this.holderMap.remove(slot);
    }

    public IBroadcastConsumer getConsumer(Integer slot) {
        return holderMap.get(slot);
    }

    public List<IBroadcastConsumer> getConsumers() {
        if(holderMap.isEmpty()) {
            return new ArrayList<>(0);
        }
        return new ArrayList<>(holderMap.values());
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }
}
