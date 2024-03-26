package io.github.tofodroid.mods.mimi.server.events.note.consumer;

import java.util.LinkedHashMap;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class ServerNoteEventHandler {
    // Constants
    private static final Integer CLEAR_EVENT_CACHE_EVERY_TICKS = 60;

    // Cache Data
    private static Integer eventCacheClearTickCounter = 0;

    // Cache maps
    private static final LinkedHashMap<ResourceKey<Level>, LinkedHashMap<Long, Boolean>> EVENT_CACHE_MAP = new LinkedHashMap<>();



















    
    // Events
    public static void onServerTick() {
        if(eventCacheClearTickCounter >= CLEAR_EVENT_CACHE_EVERY_TICKS) {
            eventCacheClearTickCounter = 0;
            EVENT_CACHE_MAP.clear();
        } else {
            eventCacheClearTickCounter++;
        }
    }







    // public static void processListeners(MidiNotePacket message, ServerLevel worldIn) {
    //         List<TileListener> listenerTiles = getCacheListeners(worldIn, message.pos);
            
    //         for(TileListener listener : listenerTiles) {
    //             if(message.isNoteOffPacket() && listener.shouldTriggerFromNoteOff(null, message.note, message.velocity, message.instrumentId)) {
    //                 listener.onNoteOff(null, message.note, message.velocity, message.instrumentId);
    //             } else if(message.isNoteOnPacket() && listener.shouldTriggerFromNoteOn(null, message.note, message.velocity, message.instrumentId)) {
    //                 listener.onNoteOn(null, message.note, message.velocity, message.instrumentId, message.noteServerTime);
    //             } else if(message.isAllNotesOffPacket() && listener.shouldTriggerFromAllNotesOff(null, message.instrumentId)) {
    //                 listener.onAllNotesOff(null, message.instrumentId);
    //             }
    //         };
    // }

    // public static void onServerTick() {
    //     if(cacheClearTickCounter >= CLEAR_CACHE_EVERY_TICKS) {
    //         cacheClearTickCounter = 0;
    //         LISTENER_CACHE_MAP.clear();
    //         PLAYER_CACHE_MAP.clear();
    //     } else {
    //         cacheClearTickCounter++;
    //     }
    // }

    // public static List<TileListener> getCacheListeners(ServerLevel level, Byte instrumentId, BlockPos pos) {
    //     LinkedHashMap<Byte, List<TileListener>> instrumentMap = LISTENER_CACHE_MAP.get(level.dimension());
        
    //     if(instrumentMap != null) {
    //         final List<TileListener> listeners = instrumentMap.get(instrumentId);

    //         if(receivers != null && !receivers.isEmpty()) {                    
    //             final BiFunction<BroadcastHandlerInput, ABroadcastConsumer, MidiNotePacket> handler;
        


    //     return entityMap.computeIfAbsent(
    //         pos.asLong(),
    //         (key) -> {
    //             return getPotentialListeners(level, pos, 64).stream().map(e -> (TileListener)e.getTile()).collect(Collectors.toList());
    //     });
    // }

    // protected static List<EntityNoteResponsiveTile> getPotentialListeners(ServerLevel worldIn, BlockPos notePos, Integer range) {
    //     List<EntityNoteResponsiveTile> potentialEntites = new ArrayList<>();

    //     AABB queryBox = new AABB(notePos.getX() - range, notePos.getY() - range, notePos.getZ() - range, 
    //                                                 notePos.getX() + range, notePos.getY() + range, notePos.getZ() + range);
    //     potentialEntites = worldIn.getEntitiesOfClass(EntityNoteResponsiveTile.class, queryBox, entity -> {
    //         return entity.getTile() != null && entity.getTile() instanceof TileListener;
    //     });

    //     return potentialEntites;
    // }
    

    // public static List<ServerPlayer> getCachePlayers(ServerLevel level, BlockPos pos) {
    //     LinkedHashMap<Long, List<ServerPlayer>> playerMap = PLAYER_CACHE_MAP.computeIfAbsent(
    //         level.dimension(), d -> new LinkedHashMap<>() 
    //     );

    //     return playerMap.computeIfAbsent(
    //         pos.asLong(),
    //         (key) -> getPotentialPlayers(level, pos, 64)
    //     );
    // }

    // protected static List<ServerPlayer> getPotentialPlayers(ServerLevel worldIn, BlockPos notePos, Integer range) {
    //     List<ServerPlayer> potentialEntites = new ArrayList<>();

    //     AABB queryBox = new AABB(notePos.getX() - range, notePos.getY() - range, notePos.getZ() - range, 
    //                                                 notePos.getX() + range, notePos.getY() + range, notePos.getZ() + range);
    //     // potentialEntites = worldIn.getEntitiesOfClass(ServerPlayer.class, queryBox, entity -> {
    //     //     return entity.isAlive();
    //     // });

    //     for(ServerPlayer player : worldIn.players()) {
    //         if(queryBox.contains(player.getX(), player.getY(), player.getZ()) && player.isAlive()) {
    //             potentialEntites.add(player);
    //         }
    //     }
  
    //     return potentialEntites;
    // }






    // Handlers
    public static void handleControl(UUID playerId, BlockPos playerPos, Byte note, Byte velocity, Byte instrumentId, Long noteServerTime) {

    }
    
    public static void handleNoteOn(UUID playerId, BlockPos playerPos, Byte note, Byte velocity, Byte instrumentId, Long noteServerTime) {

    }

    public static void handleNoteOff(UUID playerId, BlockPos playerPos, Byte note, Byte velocity, Byte instrumentId, Long noteServerTime) {

    }

    public static void handleAllNotesOff(UUID playerId, BlockPos playerPos, Byte note, Byte velocity, Byte instrumentId, Long noteServerTime) {

    }
    
    // Utils
    protected static void processSculk(MidiNotePacket message, ServerLevel worldIn) {
            LinkedHashMap<Long, Boolean> eventMap = EVENT_CACHE_MAP.computeIfAbsent(
                worldIn.dimension(), d -> new LinkedHashMap<>() 
            );
    
            eventMap.computeIfAbsent(
                message.pos.asLong(),
                (key) -> {
                    if(worldIn.isLoaded(message.pos)) {
                        worldIn.gameEvent(GameEvent.INSTRUMENT_PLAY, message.pos, GameEvent.Context.of(worldIn.getBlockState(message.pos)));
                    }
                    return true;
                }
            );
    }
}
