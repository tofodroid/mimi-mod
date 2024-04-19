package io.github.tofodroid.mods.mimi.server.events.broadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.BroadcastConsumerInventoryHolder;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.IBroadcastConsumer;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.IBroadcastProducer;

public class BroadcastManager {
    // Producers
    protected static final HashMap<UUID, IBroadcastProducer> OWNED_PRODUCERS = new HashMap<>();
    
    // Consumers
    protected static final HashMap<UUID, BroadcastConsumerInventoryHolder> OWNED_CONSUMERS = new HashMap<>();

    // Producer --> Consumers
    protected static final HashMap<UUID, List<IBroadcastConsumer>> LINKED_CONSUMERS = new HashMap<>();

    // Producer
    public static <T extends IBroadcastProducer> T registerProducer(T producer) {
        OWNED_PRODUCERS.computeIfAbsent(producer.getOwnerId(), (id) -> producer).linkConsumers(
            LINKED_CONSUMERS.computeIfAbsent(producer.getOwnerId(), (id) -> new ArrayList<>())
        );
        return producer;
    }

    public static IBroadcastProducer getBroadcastProducer(UUID producerId) {
        return OWNED_PRODUCERS.get(producerId);
    }

    public static void removeBroadcastProducer(UUID producerId) {
        IBroadcastProducer producer = OWNED_PRODUCERS.remove(producerId);

        if(producer != null) {
            try {   
                producer.close();
            } catch(Exception e) {}
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends IBroadcastProducer> List<T> getBroadcastProducersByType(Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for(IBroadcastProducer producer : OWNED_PRODUCERS.values()) {
            if(producer.getClass().equals(clazz)) {
                result.add((T)producer);
            }
        }
        return result;
    }

    private static void clearProducers() {
        for(IBroadcastProducer producer : OWNED_PRODUCERS.values()) {
            try {
                producer.close();
            } catch(Exception e) {}
        }
        OWNED_PRODUCERS.clear();
    }

    // Consumer
    public static void registerConsumers(BroadcastConsumerInventoryHolder holder) {
        BroadcastConsumerInventoryHolder existing = OWNED_CONSUMERS.get(holder.getOwnerId());

        if(existing != null && !existing.isEmpty()) {
            MIMIMod.LOGGER.warn("Attempted to register new consumers for owner ID that already has non-closed consumers: " + holder.getOwnerId());
            return;
        }

        if(!holder.isEmpty()) {
            OWNED_CONSUMERS.put(holder.getOwnerId(), holder);
            for(IBroadcastConsumer consumer : holder.getConsumers()) {
                LINKED_CONSUMERS.computeIfAbsent(consumer.getLinkedId(), (id) -> new ArrayList<>()).add(consumer);
                
                IBroadcastProducer linkedProducer = OWNED_PRODUCERS.get(consumer.getLinkedId());
    
                if(linkedProducer != null) {
                    linkedProducer.reindex();
                }
            }
        }
    }

    public static BroadcastConsumerInventoryHolder getOwnedBroadcastConsumers(UUID ownerId) {
        return OWNED_CONSUMERS.get(ownerId);
    }

    public static void removeOwnedBroadcastConsumers(UUID ownerId) {
        BroadcastConsumerInventoryHolder removedConsumers = OWNED_CONSUMERS.remove(ownerId);

        if(removedConsumers != null) {
            for(IBroadcastConsumer consumer : removedConsumers.getConsumers()) {  
                if(consumer.getLinkedId() != null) {
                    List<IBroadcastConsumer> linkedConsumerList = LINKED_CONSUMERS.get(consumer.getLinkedId());
                    if(linkedConsumerList != null) {
                        linkedConsumerList.remove(consumer);
                    }
                    
                    IBroadcastProducer linkedProducer = OWNED_PRODUCERS.get(consumer.getLinkedId());
                    if(linkedProducer != null) {
                        linkedProducer.reindex();
                    }
                }
                consumer.onConsumerRemoved();
            }
        }
    }

    private static void clearConsumers() {
        for(BroadcastConsumerInventoryHolder holder : OWNED_CONSUMERS.values()) {
            for(IBroadcastConsumer consumer : holder.getConsumers()) {
                consumer.onConsumerRemoved();
            }
        }
        OWNED_CONSUMERS.clear();
    }

    // Events
    public static void onServerTick() {
        for(IBroadcastProducer producer : OWNED_PRODUCERS.values()) {
            producer.tickProducer();
        }

        for(BroadcastConsumerInventoryHolder holder : OWNED_CONSUMERS.values()) {
            for(IBroadcastConsumer consumer : holder.getConsumers()) {
                consumer.tickConsumer();
            }
        }
    }

    public static void onServerStopping() {
        clearProducers();
        clearConsumers();
    }
}
