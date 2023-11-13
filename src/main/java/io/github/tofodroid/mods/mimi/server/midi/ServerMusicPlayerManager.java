package io.github.tofodroid.mods.mimi.server.midi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sound.midi.Sequence;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.server.network.ServerMidiUploadManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerMusicPlayerManager {
    protected static final Map<UUID,ServerMusicPlayer> PLAYER_MAP = new HashMap<>();
    protected static final Map<UUID, Set<UUID>> MIDI_LOAD_CACHE_MAP = new HashMap<>();

    public static void createTransmitter(ServerPlayer player) {
        ServerMusicPlayer handler = PLAYER_MAP.get(player.getUUID());
        
        if(handler == null) {
            PLAYER_MAP.put(player.getUUID(), new ServerMusicPlayer(player));
        }
    }

    public static void removeTransmitter(UUID id) {
        ServerMusicPlayer handler = PLAYER_MAP.remove(id);
        
        if(handler != null) {
            handler.close();
        }
    }

    public static ServerMusicPlayer getMusicPlayer(UUID id) {
        return PLAYER_MAP.get(id);
    }
    
    public static void clearMusicPlayers() {
        for(UUID id : PLAYER_MAP.keySet()) {
            removeTransmitter(id);
        }
        PLAYER_MAP.clear();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.SERVER) {
            return;
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
        ServerMusicPlayer musicPlayer = getMusicPlayer(event.getEntity().getUUID());
        musicPlayer.midiHandler.stop();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        removeTransmitter(event.getEntity().getUUID());
    }

    public static void onSelectedSongChange(UUID musicPlayerId, BasicMidiInfo newInfo) {
        ServerMusicPlayer player = getMusicPlayer(musicPlayerId);

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

    public static void onFinishLoadSequence(BasicMidiInfo info, Sequence sequence) {
        if(MIDI_LOAD_CACHE_MAP.containsKey(info.fileId)) {
            for(UUID musicPlayerId : MIDI_LOAD_CACHE_MAP.get(info.fileId)) {
                ServerMusicPlayer player = getMusicPlayer(musicPlayerId);
                if(player != null) {
                    player.finishLoadSequence(info, sequence);
                }
            }
            MIDI_LOAD_CACHE_MAP.remove(info.fileId);
        }

    }
}
