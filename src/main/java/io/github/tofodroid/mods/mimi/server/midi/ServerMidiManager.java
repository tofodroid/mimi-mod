package io.github.tofodroid.mods.mimi.server.midi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.common.midi.LocalMidiInfo;
import io.github.tofodroid.mods.mimi.common.network.ClientMidiListPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import net.minecraft.server.level.ServerPlayer;

public abstract class ServerMidiManager {
    private static final Map<UUID, List<BasicMidiInfo>> CACHE_MAP = new HashMap<>();

    public static void refreshServerSongs(Boolean forceFromDisk) {
        MIMIMod.getProxy().serverMidiFiles().refresh(forceFromDisk);
        ServerMusicTransmitterManager.onServerSongsRefreshed();
    }

    public static LocalMidiInfo getServerSongById(UUID id) {
        return MIMIMod.getProxy().serverMidiFiles().getInfoById(id);
    }

    public static List<BasicMidiInfo> getServerSongs() {
        return MIMIMod.getProxy().serverMidiFiles().getSortedSongInfos();
    }

    public static List<BasicMidiInfo> getMidiInfosForSourceId(UUID sourceId) {
        return CACHE_MAP.containsKey(sourceId) ? CACHE_MAP.get(sourceId) : new ArrayList<>();
    }

    public static List<BasicMidiInfo> getSortedMidiInfosForSourceId(UUID sourceId) {
        return CACHE_MAP.containsKey(sourceId) ? CACHE_MAP.get(sourceId).stream().sorted((cacheInfoA, cacheInfoB) -> {
            return cacheInfoA.fileName.compareTo(cacheInfoB.fileName);
        }).collect(Collectors.toList()) : new ArrayList<>();
    }

    public static void setCacheInfosForSource(UUID sourceId, List<BasicMidiInfo> cacheInfos) {
        CACHE_MAP.put(sourceId, cacheInfos);
    }

    public static void clearCacheInfosForSource(UUID sourceId) {
        CACHE_MAP.remove(sourceId);
    }

    public static BasicMidiInfo getInfoForSourceAndFile(UUID sourceId, UUID fileId) {
        for(BasicMidiInfo info : getMidiInfosForSourceId(sourceId)) {
            if(info.fileId.toString().equals(fileId.toString())) {
                return info;
            }
        }
        return null;
    }

    public static void onPlayerLoggedIn(ServerPlayer player) {
        NetworkProxy.sendToPlayer(player, new ClientMidiListPacket());
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        clearCacheInfosForSource(player.getUUID());
    }
}

