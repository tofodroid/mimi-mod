package io.github.tofodroid.mods.mimi.server.events.broadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.ABroadcastConsumer;
import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.BroadcastConsumerHolder;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.ABroadcastProducer;

public class BroadcastManager {
    // Producers
    protected static final HashMap<UUID, ABroadcastProducer> OWNED_PRODUCERS = new HashMap<>();
    
    // Consumers
    protected static final HashMap<UUID, BroadcastConsumerHolder> OWNED_CONSUMERS = new HashMap<>();

    // Producer --> Consumers
    protected static final HashMap<UUID, List<ABroadcastConsumer>> LINKED_CONSUMERS = new HashMap<>();

    // Producer
    public static void registerProducer(ABroadcastProducer producer) {
        OWNED_PRODUCERS.computeIfAbsent(producer.getOwnerId(), (id) -> producer).linkConsumers(
            LINKED_CONSUMERS.computeIfAbsent(producer.getOwnerId(), (id) -> new ArrayList<>())
        );
    }

    public static ABroadcastProducer getBroadcastProducer(UUID producerId) {
        return OWNED_PRODUCERS.get(producerId);
    }

    public static void removeBroadcastProducer(UUID producerId) {
        ABroadcastProducer producer = OWNED_PRODUCERS.remove(producerId);

        if(producer != null) {
            try {   
                producer.close();
            } catch(Exception e) {}
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends ABroadcastProducer> List<T> getBroadcastProducersByType(Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for(ABroadcastProducer producer : OWNED_PRODUCERS.values()) {
            if(producer.getClass().equals(clazz)) {
                result.add((T)producer);
            }
        }
        return result;
    }

    private static void clearProducers() {
        for(ABroadcastProducer producer : OWNED_PRODUCERS.values()) {
            try {
                producer.close();
            } catch(Exception e) {}
        }
        OWNED_PRODUCERS.clear();
    }

    // Consumer
    protected static void registerConsumers(BroadcastConsumerHolder holder) {
        removeOwnedBroadcastConsumers(holder.getOwnerId());

        if(!holder.isEmpty()) {
            OWNED_CONSUMERS.put(holder.getOwnerId(), holder);
            for(ABroadcastConsumer consumer : holder.getConsumers()) {
                LINKED_CONSUMERS.computeIfAbsent(consumer.getLinkedId(), (id) -> new ArrayList<>()).add(consumer);
                
                ABroadcastProducer linkedProducer = OWNED_PRODUCERS.get(consumer.getLinkedId());
    
                if(linkedProducer != null) {
                    linkedProducer.reindex();
                }
            }
        }
    }

    public static BroadcastConsumerHolder getOwnedBroadcastConsumers(UUID ownerId) {
        return OWNED_CONSUMERS.get(ownerId);
    }

    public static void removeOwnedBroadcastConsumers(UUID ownerId) {
        BroadcastConsumerHolder removedConsumers = OWNED_CONSUMERS.remove(ownerId);

        if(removedConsumers != null) {
            for(ABroadcastConsumer consumer : removedConsumers.getConsumers()) {  
                if(consumer.getLinkedId() != null) {
                    List<ABroadcastConsumer> linkedConsumerList = LINKED_CONSUMERS.get(consumer.getLinkedId());
                    if(linkedConsumerList != null) {
                        linkedConsumerList.remove(consumer);
                    }
                    
                    ABroadcastProducer linkedProducer = OWNED_PRODUCERS.get(consumer.getLinkedId());
                    if(linkedProducer != null) {
                        linkedProducer.reindex();
                    }
                }
                consumer.onRemoved();
            }
        }
    }

    private static void clearConsumers() {
        for(BroadcastConsumerHolder holder : OWNED_CONSUMERS.values()) {
            for(ABroadcastConsumer consumer : holder.getConsumers()) {
                consumer.onRemoved();
            }
        }
        OWNED_CONSUMERS.clear();
    }

    // Events
    public static void onServerTick() {
        for(ABroadcastProducer producer : OWNED_PRODUCERS.values()) {
            producer.tick();
        }

        for(BroadcastConsumerHolder holder : OWNED_CONSUMERS.values()) {
            for(ABroadcastConsumer consumer : holder.getConsumers()) {
                consumer.tick();
            }
        }
    }

    public static void onServerStopping() {
        clearProducers();
        clearConsumers();
    }
}
