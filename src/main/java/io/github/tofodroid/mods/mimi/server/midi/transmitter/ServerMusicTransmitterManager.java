package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sound.midi.Sequence;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;
import io.github.tofodroid.mods.mimi.server.network.ServerMidiUploadManager;
import io.github.tofodroid.mods.mimi.util.EntityUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerMusicTransmitterManager {
    private static final Set<UUID> PLAYING_LIST = new HashSet<>();
    private static final Map<UUID,AServerMusicTransmitter> PLAYER_MAP = new HashMap<>();
    private static final Map<UUID, Set<UUID>> MIDI_LOAD_CACHE_MAP = new HashMap<>();
    private static Boolean hasPlaying = false;

    public static void createTransmitter(ServerPlayer player) {
        AServerMusicTransmitter handler = PLAYER_MAP.get(player.getUUID());
        
        if(handler == null) {
            PLAYER_MAP.put(player.getUUID(), new PlayerTransmitterMusicTransmitter(player));
        }
    }

    public static void createTransmitter(TileTransmitter tile) {
        AServerMusicTransmitter handler = PLAYER_MAP.get(tile.getUUID());
        
        if(handler == null) {
            PLAYER_MAP.put(tile.getUUID(), new TileTransmitterMusicTransmitter(tile));
        }
    }

    public static void addPlaying(UUID id) {
        PLAYING_LIST.add(id);
        hasPlaying = true;
    }

    public static void removePlaying(UUID id) {
        PLAYING_LIST.remove(id);
        hasPlaying = !PLAYING_LIST.isEmpty();
    }

    public static void removeTransmitter(UUID id) {
        AServerMusicTransmitter handler = PLAYER_MAP.remove(id);
        
        if(handler != null) {
            handler.close();
        }
        removePlaying(id);
    }

    public static AServerMusicTransmitter getMusicPlayer(UUID id) {
        return PLAYER_MAP.get(id);
    }
    
    public static void clearMusicPlayers() {
        for(UUID id : PLAYER_MAP.keySet()) {
            AServerMusicTransmitter player = PLAYER_MAP.get(id);

            if(player != null) {
                player.close();
            }
        }
        PLAYING_LIST.clear();
        hasPlaying = false;
        PLAYER_MAP.clear();
    }

    public static Boolean hasTransmitters() {
        return !PLAYER_MAP.isEmpty();
    }

    public static Boolean hasPlayingTransmitters() {
        return hasPlaying;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.SERVER) {
            return;
        }

        if(PLAYER_MAP.containsKey(event.player.getUUID()) && !EntityUtils.playerHasActiveTransmitter(event.player)) {
            PLAYER_MAP.get(event.player.getUUID()).stop();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        clearMusicPlayers();
    }
    
    @SubscribeEvent
    public static void onPlayerDeath(PlayerEvent.PlayerRespawnEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        AServerMusicTransmitter player = getMusicPlayer(event.getEntity().getUUID());
        if(player != null) {
            player.stop();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        removeTransmitter(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        AServerMusicTransmitter player = getMusicPlayer(event.getEntity().getUUID());
        if(player != null) {
            player.allNotesOff();
        }
    }
    
    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        AServerMusicTransmitter player = getMusicPlayer(event.getEntity().getUUID());
        if(player != null) {
            player.allNotesOff();
        }
    }

    public static void onSelectedSongChange(UUID musicPlayerId, BasicMidiInfo newInfo) {
        AServerMusicTransmitter player = getMusicPlayer(musicPlayerId);

        if(player != null) {
            player.loadSong(newInfo);
        }
    }

    public static void startLoadSequence(UUID musicPlayerId, UUID clientId, BasicMidiInfo info) {
        if(MIDI_LOAD_CACHE_MAP.containsKey(info.fileId)) {
            MIDI_LOAD_CACHE_MAP.get(info.fileId).add(musicPlayerId);
        } else {
            MIDI_LOAD_CACHE_MAP.put(info.fileId, new HashSet<>());
            MIDI_LOAD_CACHE_MAP.get(info.fileId).add(musicPlayerId);
            ServerMidiUploadManager.startUploadRequest(clientId, info);
        }
    }

    public static void onSequenceUploadFailed(BasicMidiInfo info) {
        if(MIDI_LOAD_CACHE_MAP.containsKey(info.fileId)) {
            for(UUID musicPlayerId : MIDI_LOAD_CACHE_MAP.get(info.fileId)) {
                AServerMusicTransmitter player = getMusicPlayer(musicPlayerId);
                if(player != null) {
                    player.onSequenceLoadFailed(info);
                }
            }
            MIDI_LOAD_CACHE_MAP.remove(info.fileId);
        }
    }

    public static void onFinishUploadSequence(BasicMidiInfo info, Sequence sequence) {
        if(MIDI_LOAD_CACHE_MAP.containsKey(info.fileId)) {
            for(UUID musicPlayerId : MIDI_LOAD_CACHE_MAP.get(info.fileId)) {
                AServerMusicTransmitter player = getMusicPlayer(musicPlayerId);
                if(player != null) {
                    player.finishLoadSequence(info, sequence);
                }
            }
            MIDI_LOAD_CACHE_MAP.remove(info.fileId);
        }
    }
}
