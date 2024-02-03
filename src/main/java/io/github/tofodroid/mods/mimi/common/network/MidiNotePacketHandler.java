package io.github.tofodroid.mods.mimi.common.network;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.tile.TileListener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.gameevent.GameEvent;

public class MidiNotePacketHandler {
    private static final ConcurrentHashMap<String, List<EntityNoteResponsiveTile>> ENTITY_CACHE_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<ServerPlayer>> PLAYER_CACHE_MAP = new ConcurrentHashMap<>();
    private static final Integer CLEAR_CACHE_EVERY_TICKS = 5;
    private static Integer cacheClearTickCounter = 0;

    public static void onServerTick() {
        if(cacheClearTickCounter >= CLEAR_CACHE_EVERY_TICKS) {
            cacheClearTickCounter = 0;
            ENTITY_CACHE_MAP.clear();
            PLAYER_CACHE_MAP.clear();
        } else {
            cacheClearTickCounter++;
        }        
    }

    public static void handlePacketServer(final MidiNotePacket message, ServerPlayer sender) {
        handlePacketServer(message, sender.serverLevel(), sender);
    }
    
    public static void handlePacketServer(final MidiNotePacket message, ServerLevel worldIn, ServerPlayer sender) {
        if(message != null) {
            String packetCacheKey = worldIn.dimensionTypeId().location().toString() + message.pos.toShortString();

            // Find nearby players
            List<ServerPlayer> potentialPlayers = PLAYER_CACHE_MAP.computeIfAbsent(
                packetCacheKey,
                (key) -> getPotentialPlayers(worldIn, message.pos, getQueryBoxRange(message.velocity <= 0))
            );

            // Ensure source player is included if this is coming from music reciever
            ServerPlayer sourcePlayer = (ServerPlayer)worldIn.getPlayerByUUID(message.player);
            if(sender == null && sourcePlayer != null && !potentialPlayers.contains(sourcePlayer)) {
                potentialPlayers.add(sourcePlayer);
            }

            // Forward to nearby players
            potentialPlayers.forEach(player -> {
                if(player != sender) {
                    NetworkProxy.sendToPlayer(player, message);
                }
            });

            // Process Listeners and Sculk
            if(!message.isControlPacket()) {
                List<EntityNoteResponsiveTile> potentialEntities = ENTITY_CACHE_MAP.computeIfAbsent(
                    packetCacheKey,
                    (key) -> {
                        List<EntityNoteResponsiveTile> newEntities = getPotentialEntities(worldIn, message.pos, getQueryBoxRange(false));
                        worldIn.gameEvent(GameEvent.INSTRUMENT_PLAY, message.pos, GameEvent.Context.of(worldIn.getBlockState(message.pos)));
                        return newEntities;
                    }
                );
                
                for(TileListener listener : filterToListeners(potentialEntities)) {
                    if(message.isNoteOnPacket() && listener.shouldTriggerFromNoteOn(null, message.note, message.velocity, message.instrumentId)) {
                        listener.onNoteOn(null, message.note, message.velocity, message.instrumentId);
                    } else if(message.isNoteOffPacket() && listener.shouldTriggerFromNoteOff(null, message.note, message.velocity, message.instrumentId)) {
                        listener.onNoteOff(null, message.note, message.velocity, message.instrumentId);
                    } else if(message.isAllNotesOffPacket() && listener.shouldTriggerFromAllNotesOff(null, message.instrumentId)) {
                        listener.onAllNotesOff(null, message.instrumentId);
                    }
                };
            }
        }
    }

    public static void handlePacketClient(final MidiNotePacket message) {
        if(MIMIMod.getProxy().isClient()) ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().handlePacket(message); 
    }

    protected static List<ServerPlayer> getPotentialPlayers(ServerLevel worldIn, BlockPos notePos, Integer range) {
        List<ServerPlayer> potentialEntites = new ArrayList<>();

        AABB queryBox = new AABB(notePos.getX() - range, notePos.getY() - range, notePos.getZ() - range, 
                                                    notePos.getX() + range, notePos.getY() + range, notePos.getZ() + range);
        potentialEntites = worldIn.getEntitiesOfClass(ServerPlayer.class, queryBox, entity -> {
            return entity.isAlive();
        });

        return potentialEntites;
    }

    protected static List<EntityNoteResponsiveTile> getPotentialEntities(ServerLevel worldIn, BlockPos notePos, Integer range) {
        List<EntityNoteResponsiveTile> potentialEntites = new ArrayList<>();

        AABB queryBox = new AABB(notePos.getX() - range, notePos.getY() - range, notePos.getZ() - range, 
                                                    notePos.getX() + range, notePos.getY() + range, notePos.getZ() + range);
        potentialEntites = worldIn.getEntitiesOfClass(EntityNoteResponsiveTile.class, queryBox, entity -> {
            return entity.getTile() != null;
        });

        return potentialEntites;
    }

    protected static List<TileListener> filterToListeners(List<EntityNoteResponsiveTile> entities) {
        return entities.stream().filter(e -> e.getTile() instanceof TileListener).map(e -> (TileListener)e.getTile()).collect(Collectors.toList());
    }

    protected static Integer getQueryBoxRange(Boolean off) {
        return off ? 128 : 64;
    }
}
