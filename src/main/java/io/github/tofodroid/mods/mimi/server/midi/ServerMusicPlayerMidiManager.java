package io.github.tofodroid.mods.mimi.server.midi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.sound.midi.Sequence;

import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileCacheManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiInfoPacket;
import io.github.tofodroid.mods.mimi.common.tile.TileBroadcaster;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerMusicPlayerMidiManager {
    protected static Map<UUID,MusicPlayerMidiHandler> MUSIC_PLAYER_MAP = new HashMap<>();

    // Music Player Cache
    public static MusicPlayerMidiHandler getOrAddMusicPlayer(TileBroadcaster tile, String midiUrl) {
        MusicPlayerMidiHandler handler = getMusicPlayer(tile);

        if(handler == null) {
            Pair<Sequence,ServerMidiInfoPacket.STATUS_CODE> result = MidiFileCacheManager.getOrCreateCachedSequence(midiUrl);
            MUSIC_PLAYER_MAP.put(tile.getMusicPlayerId(), new MusicPlayerMidiHandler(tile, result.getLeft(), result.getRight()));
            return MUSIC_PLAYER_MAP.get(tile.getMusicPlayerId());
        }

        return handler;
    }
    
    public static MusicPlayerMidiHandler getMusicPlayer(TileBroadcaster tile) {
        if(tile != null) { 
            return MUSIC_PLAYER_MAP.get(tile.getMusicPlayerId());
        }
        return null;
    }

    public static void removeMusicPlayer(TileBroadcaster tile) {
        MusicPlayerMidiHandler handler = getMusicPlayer(tile);

        if(handler != null) {
            handler.close();
            MUSIC_PLAYER_MAP.remove(tile.getMusicPlayerId());
        }
    }

    public static void clearMusicPlayers() {
        for(UUID id : MUSIC_PLAYER_MAP.keySet()) {
            if(MUSIC_PLAYER_MAP.get(id) != null) {
                MUSIC_PLAYER_MAP.get(id).close();
            }
            MUSIC_PLAYER_MAP.remove(id);
        }
        MUSIC_PLAYER_MAP = new HashMap<>();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        clearMusicPlayers();
    }
}
