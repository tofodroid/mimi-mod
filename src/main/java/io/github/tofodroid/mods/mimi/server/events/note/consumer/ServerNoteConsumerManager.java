package io.github.tofodroid.mods.mimi.server.events.note.consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.api.event.note.NoteEvent;
import io.github.tofodroid.mods.mimi.common.network.NoteEventPacket;
import io.github.tofodroid.mods.mimi.common.tile.TileListener;
import io.github.tofodroid.mods.mimi.server.ServerExecutorProxy;
import io.github.tofodroid.mods.mimi.server.events.note.api.INoteConsumer;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ServerNoteConsumerManager {
    // Constants
    private static final List<INoteConsumer> EMPTY_LIST = Arrays.asList();

    // Data Maps - Cleared on level load
    protected static final Map<UUID, INoteConsumer> OWNED_CONSUMERS = new HashMap<>();

    // Cache Maps - Cleared every n ticks
    private static final Map<Byte, List<INoteConsumer>> CONSUMER_LOOKUP = new Byte2ObjectOpenHashMap<>();


    public static List<INoteConsumer> lookupConsumers(Byte instrumentId) {        
        if(instrumentId != null) {
            return CONSUMER_LOOKUP.getOrDefault(instrumentId, EMPTY_LIST);
        }
        return EMPTY_LIST;
    }

    public static void handlePacket(NoteEventPacket packet, Boolean clientSource, UUID senderId, ServerLevel sourceLevel) {
        ServerNoteConsumerManager.handleEvent(packet.toNoteEvent(clientSource, senderId, sourceLevel));
    }

    public static void handleEvent(NoteEvent event) {
        final List<INoteConsumer> noteConsumers = lookupConsumers(event.instrumentId);
        final List<INoteConsumer> allConsumers = lookupConsumers(INoteConsumer.ALL_INSTRUMENTS_ID);

        if(!noteConsumers.isEmpty() || !allConsumers.isEmpty()) {
            for(INoteConsumer consumer : noteConsumers) {
                ServerExecutorProxy.executeOnServerThread(() -> consumer.consumeEvent(event));
            }

            for(INoteConsumer consumer : allConsumers) {
                ServerExecutorProxy.executeOnServerThread(() -> consumer.consumeEvent(event));
            }
        }

        ServerExecutorProxy.executeOnServerThread(() -> ServerNoteConsumer.handleEvent(event));
    }

    public static void loadPlayerConsumer(ServerPlayer player) {
        if(player == null) {
            return;
        }

        if(player.getLevel() instanceof ServerLevel) {
            OWNED_CONSUMERS.put(player.getUUID(), new PlayerNoteConsumer(player));
        } else {
            OWNED_CONSUMERS.remove(player.getUUID());
        }
    }

    public static void loadListenerTileConsumer(TileListener tile) { 
        if(tile == null) {
            return;
        }

        if(tile.getLevel() != null && MidiNbtDataUtils.getFilterInstrument(tile.getSourceStack()) != null) {
            OWNED_CONSUMERS.put(tile.getUUID(), tile);
        } else {
            OWNED_CONSUMERS.remove(tile.getUUID());
        }   
    }

    public static void removeConsumers(UUID id) {
        OWNED_CONSUMERS.remove(id);
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        if(player.getLevel() instanceof ServerLevel) {
            removeConsumers(player.getUUID());
        }
    }

    public static void onPlayerLoggedIn(ServerPlayer player) {
        loadPlayerConsumer(player);
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        loadPlayerConsumer(player);
    }

    public static void onServerTick() {        
        // Identify listeners
        CONSUMER_LOOKUP.clear();
        OWNED_CONSUMERS.values().stream().forEach(consumer -> {
            consumer.tickConsumer();
            List<INoteConsumer> consumerList = CONSUMER_LOOKUP.computeIfAbsent(consumer.getInstrumentId(), (iid) -> new ArrayList<>());
            consumerList.add(consumer);
        });
    }

    public static void onServerStopping() {
        OWNED_CONSUMERS.clear();
        CONSUMER_LOOKUP.clear();
    }
}
