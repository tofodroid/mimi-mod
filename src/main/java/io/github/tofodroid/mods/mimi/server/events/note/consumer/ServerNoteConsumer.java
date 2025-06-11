package io.github.tofodroid.mods.mimi.server.events.note.consumer;

import java.util.LinkedHashMap;

import io.github.tofodroid.mods.mimi.common.api.event.MidiEventType;
import io.github.tofodroid.mods.mimi.common.api.event.note.NoteEvent;
import io.github.tofodroid.mods.mimi.server.ServerExecutorProxy;
import it.unimi.dsi.fastutil.longs.Long2BooleanArrayMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class ServerNoteConsumer {
    // Constants
    private static final Integer CLEAR_EVENT_CACHE_EVERY_TICKS = 10;

    // Cache Data
    private static Integer eventCacheClearTickCounter = 0;

    // Cache maps
    private static final LinkedHashMap<ResourceKey<Level>, Long2BooleanArrayMap> EVENT_CACHE_MAP = new LinkedHashMap<>();
    
    // Events
    public static void onServerTick() {
        if(eventCacheClearTickCounter >= CLEAR_EVENT_CACHE_EVERY_TICKS) {
            eventCacheClearTickCounter = 0;
            EVENT_CACHE_MAP.clear();
        } else {
            eventCacheClearTickCounter++;
        }
    }

    public static void handleEvent(NoteEvent message) {
        ServerLevel worldIn = ServerExecutorProxy.getLevel(message.dimension);

        Long2BooleanArrayMap eventMap = EVENT_CACHE_MAP.computeIfAbsent(
            worldIn.dimension(), d -> new Long2BooleanArrayMap() 
        );
        
        if(message.type == MidiEventType.NOTE_ON) {
            eventMap.computeIfAbsent(
                message.pos.asLong(),
                (key) -> {
                    if(worldIn.isLoaded(message.pos)) {
                        worldIn.gameEvent(GameEvent.INSTRUMENT_PLAY, message.pos, GameEvent.Context.of(worldIn.getBlockState(message.pos)));
                        worldIn.gameEvent(GameEvent.JUKEBOX_PLAY, message.pos, GameEvent.Context.of(worldIn.getBlockState(message.pos)));
                        worldIn.levelEvent(null, NoteEvent.MIMI_NOTE_PLAYING_LEVEL_EVENT_ID, message.pos, message.note);
                    }
                    return true;
                }
            );
        }
    }
}
