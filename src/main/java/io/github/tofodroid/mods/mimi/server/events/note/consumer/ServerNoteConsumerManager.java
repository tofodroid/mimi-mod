package io.github.tofodroid.mods.mimi.server.events.note.consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.tile.TileListener;
import io.github.tofodroid.mods.mimi.server.events.note.NoteEvent;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class ServerNoteConsumerManager {
    // Constants
    private static final List<ANoteConsumer> EMPTY_LIST = Arrays.asList();

    // Data Maps - Cleared on level load
    protected static final Map<UUID, ANoteConsumer> OWNED_CONSUMERS = new HashMap<>();

    // Cache Maps - Cleared every n ticks
    private static final Map<Byte, List<ANoteConsumer>> CONSUMER_LOOKUP = new Byte2ObjectOpenHashMap<>();


    public static List<ANoteConsumer> lookupConsumers(Byte instrumentId) {        
        if(instrumentId != null) {
            return CONSUMER_LOOKUP.getOrDefault(instrumentId, EMPTY_LIST);
        }
        return EMPTY_LIST;
    }

    public static void handleBroadcastPacket(MidiNotePacket packet, ResourceKey<Level> sourceDimension) {
        ServerNoteConsumerManager.handlePacket(packet, null, sourceDimension);
    }

    public static void handlePacket(MidiNotePacket packet, UUID senderId, ServerLevel sourceLevel) {
        ServerNoteConsumerManager.handlePacket(packet, senderId, sourceLevel.dimension());
    }

    private static void handlePacket(MidiNotePacket packet, UUID senderId, ResourceKey<Level> dimension) {
        final List<ANoteConsumer> noteConsumers = lookupConsumers(packet.instrumentId);
        final List<ANoteConsumer> allConsumers = lookupConsumers(ANoteConsumer.ALL_INSTRUMENTS_ID);

        if(!noteConsumers.isEmpty() || !allConsumers.isEmpty()) {
            final BiFunction<NoteEvent, ANoteConsumer, Boolean> handler;

            if(packet.isNoteOffPacket()) {
                handler = ServerNoteConsumerManager::handleNoteOff;
            } else if(packet.isNoteOnPacket()) {
                handler = ServerNoteConsumerManager::handleNoteOn;
            } else if(packet.isAllNotesOffPacket()) {
                handler = ServerNoteConsumerManager::handleAllNotesOff;
            } else if(packet.isControlPacket()) {
                handler = ServerNoteConsumerManager::handleControl;
            } else {
                handler = null;
            }

            if(handler != null) {
                for(ANoteConsumer consumer : noteConsumers) {
                    handler.apply(new NoteEvent(packet, senderId, dimension), consumer);
                }
                for(ANoteConsumer consumer : allConsumers) {
                    handler.apply(new NoteEvent(packet, senderId, dimension), consumer);
                }
            }
        }
    }

    public static Boolean handleNoteOn(NoteEvent input, ANoteConsumer consumer) {
        return consumer.handleNoteOn(input);
    }

    public static Boolean handleNoteOff(NoteEvent input, ANoteConsumer consumer) {
        return consumer.handleNoteOff(input);
    }

    public static Boolean handleAllNotesOff(NoteEvent input, ANoteConsumer consumer) {
        return consumer.handleAllNotesOff(input);
    }

    public static Boolean handleControl(NoteEvent input, ANoteConsumer consumer) {
        return consumer.handleControl(input);
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
            OWNED_CONSUMERS.put(tile.getUUID(), new ListenerNoteConsumer(tile));
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
            consumer.tick();
            List<ANoteConsumer> consumerList = CONSUMER_LOOKUP.computeIfAbsent(consumer.instrumentId, (iid) -> new ArrayList<>());
            consumerList.add(consumer);
        });
    }

    public static void onServerStopping() {
        OWNED_CONSUMERS.clear();
        CONSUMER_LOOKUP.clear();
    }
}
