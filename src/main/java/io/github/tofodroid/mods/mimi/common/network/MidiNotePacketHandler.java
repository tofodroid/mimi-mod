package io.github.tofodroid.mods.mimi.common.network;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashSet;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.tile.TileListener;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class MidiNotePacketHandler {
    private static final LinkedHashMap<ResourceKey<Level>, LinkedHashMap<Long, List<TileListener>>> LISTENER_CACHE_MAP = new LinkedHashMap<>();
    private static final LinkedHashMap<ResourceKey<Level>, LinkedHashMap<Long, List<ServerPlayer>>> PLAYER_CACHE_MAP = new LinkedHashMap<>();
    private static final Integer CLEAR_CACHE_EVERY_TICKS = 5;
    private static Integer cacheClearTickCounter = 0;

    public static void handlePacketServer(final MidiNotePacket message, ServerPlayer sender) {
        handlePacketServer(message, sender.serverLevel(), sender);
    }

    public static void handlePacketServer(final MultiMidiNotePacket message, ServerPlayer sender) {
        MIMIMod.LOGGER.warn("Server received unexpected MultiMidiNotePacket");
    }
    
    public static void handlePacketServer(final MultiMidiNotePacket message, ServerLevel worldIn) {
        // Note: Only used by transmitters
        if(message != null) {
            ArrayList<ServerPlayer> potentialPlayers = new ArrayList<>();

            for(Map.Entry<UUID, List<MidiNotePacket>> entry : message.packets.entrySet()) {
                // Forward to nearby players
                potentialPlayers.addAll(getCachePlayers(worldIn, entry.getValue().get(0).pos));

                // Ensure source player is included if this is coming from music reciever
                ServerPlayer sourcePlayer = (ServerPlayer)worldIn.getPlayerByUUID(entry.getKey());
                if(sourcePlayer != null) {
                    potentialPlayers.add(sourcePlayer);
                }

                // Process Listeners
                entry.getValue().forEach(packet ->
                    processListeners(packet, worldIn)
                );
            }

            // Send
            for(ServerPlayer player : (message.packets.size() > 1 ? potentialPlayers : new HashSet<>(potentialPlayers))) {
                NetworkProxy.sendToPlayer(player, message);
            }
        }
    }
    
    public static void handlePacketServer(final MidiNotePacket message, ServerLevel worldIn, ServerPlayer sender) {
        if(message != null) {
            // Forward to nearby players
            List<ServerPlayer> potentialPlayers = getCachePlayers(worldIn, message.pos);

            // Ensure source player is included if this is coming from music reciever
            ServerPlayer sourcePlayer = (ServerPlayer)worldIn.getPlayerByUUID(message.player);
            if(sender == null && sourcePlayer != null && !potentialPlayers.contains(sourcePlayer)) {
                potentialPlayers.add(sourcePlayer);
            }

            // Process Listeners and Sculk
            processListeners(message, worldIn);

            // Send
            potentialPlayers.forEach(player -> {
                if(player != sender) {
                    NetworkProxy.sendToPlayer(player, message);
                }
            });
        }
    }

    public static void processListeners(MidiNotePacket message, ServerLevel worldIn) {
        if(!message.isControlPacket()) {
            List<TileListener> listenerTiles = getCacheListeners(worldIn, message.pos);
            
            for(TileListener listener : listenerTiles) {
                if(message.isNoteOffPacket() && listener.shouldTriggerFromNoteOff(null, message.note, message.velocity, message.instrumentId)) {
                    listener.onNoteOff(null, message.note, message.velocity, message.instrumentId);
                } else if(message.isNoteOnPacket() && listener.shouldTriggerFromNoteOn(null, message.note, message.velocity, message.instrumentId)) {
                    listener.onNoteOn(null, message.note, message.velocity, message.instrumentId, message.noteServerTime);
                } else if(message.isAllNotesOffPacket() && listener.shouldTriggerFromAllNotesOff(null, message.instrumentId)) {
                    listener.onAllNotesOff(null, message.instrumentId);
                }
            };
        }
    }

    public static void handlePacketClient(final MidiNotePacket message) {
        if(MIMIMod.getProxy().isClient()) ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().handlePacket(message); 
    }

    public static void handlePacketClient(final  MultiMidiNotePacket message) {
        if(MIMIMod.getProxy().isClient()) {
            ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().handleMultiPacket(message); 
        }
    }

    public static void onServerTick() {
        if(cacheClearTickCounter >= CLEAR_CACHE_EVERY_TICKS) {
            cacheClearTickCounter = 0;
            LISTENER_CACHE_MAP.clear();
            PLAYER_CACHE_MAP.clear();
        } else {
            cacheClearTickCounter++;
        }        
    }

    public static List<TileListener> getCacheListeners(ServerLevel level, BlockPos pos) {
        LinkedHashMap<Long, List<TileListener>> entityMap = LISTENER_CACHE_MAP.computeIfAbsent(
            level.dimension(), d -> new LinkedHashMap<>() 
        );

        return entityMap.computeIfAbsent(
            pos.asLong(),
            (key) -> {
                level.gameEvent(GameEvent.INSTRUMENT_PLAY, pos, GameEvent.Context.of(level.getBlockState(pos)));
                return getPotentialListeners(level, pos, 64).stream().map(e -> (TileListener)e.getTile()).collect(Collectors.toList());
        });
    }

    public static List<ServerPlayer> getCachePlayers(ServerLevel level, BlockPos pos) {
        LinkedHashMap<Long, List<ServerPlayer>> playerMap = PLAYER_CACHE_MAP.computeIfAbsent(
            level.dimension(), d -> new LinkedHashMap<>() 
        );

        return playerMap.computeIfAbsent(
            pos.asLong(),
            (key) -> getPotentialPlayers(level, pos, 64)
        );
    }

    protected static List<ServerPlayer> getPotentialPlayers(ServerLevel worldIn, BlockPos notePos, Integer range) {
        List<ServerPlayer> potentialEntites = new ArrayList<>();

        AABB queryBox = new AABB(notePos.getX() - range, notePos.getY() - range, notePos.getZ() - range, 
                                                    notePos.getX() + range, notePos.getY() + range, notePos.getZ() + range);
        // potentialEntites = worldIn.getEntitiesOfClass(ServerPlayer.class, queryBox, entity -> {
        //     return entity.isAlive();
        // });

        for(ServerPlayer player : worldIn.players()) {
            if(queryBox.contains(player.getX(), player.getY(), player.getZ()) && player.isAlive()) {
                potentialEntites.add(player);
            }
        }
  
        return potentialEntites;
    }

    protected static List<EntityNoteResponsiveTile> getPotentialListeners(ServerLevel worldIn, BlockPos notePos, Integer range) {
        List<EntityNoteResponsiveTile> potentialEntites = new ArrayList<>();

        AABB queryBox = new AABB(notePos.getX() - range, notePos.getY() - range, notePos.getZ() - range, 
                                                    notePos.getX() + range, notePos.getY() + range, notePos.getZ() + range);
        potentialEntites = worldIn.getEntitiesOfClass(EntityNoteResponsiveTile.class, queryBox, entity -> {
            return entity.getTile() != null && entity.getTile() instanceof TileListener;
        });

        return potentialEntites;
    }
}
