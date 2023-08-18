package io.github.tofodroid.mods.mimi.server.midi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerMusicPlayerMidiManager {
    protected static Map<UUID,MusicPlayerMidiHandler> TRANSMITTER_MAP = new HashMap<>();

    // Transmitter
    public static Boolean createOrReplaceTransmitter(Player player, Sequence sequence) {
        MusicPlayerMidiHandler oldHandler = TRANSMITTER_MAP.get(player.getUUID());

        if(oldHandler != null) {
            oldHandler.close();
        }
        
        try {
            MusicPlayerMidiHandler newHandler = new MusicPlayerMidiHandler(player, sequence);    
            TRANSMITTER_MAP.put(player.getUUID(), newHandler);
            return true;
        } catch(InvalidMidiDataException e) {
            MIMIMod.LOGGER.error(e);
        }
        return false;
    }

    public static void playTransmitter(UUID id) {
        MusicPlayerMidiHandler handler = TRANSMITTER_MAP.get(id);

        if(handler != null) {
            handler.play();
        }
    }
    
    public static void seekTransmitter(UUID id, Integer seekPositionSeconds) {
        MusicPlayerMidiHandler handler = TRANSMITTER_MAP.get(id);

        if(handler != null && seekPositionSeconds == 0) {
            Boolean wasPlaying = handler.isPlaying();
            handler.stop();

            if(wasPlaying) {
                handler.play();
            }
        }
    }

    public static void markTransmitterComplete(UUID id) {
        MusicPlayerMidiHandler handler = TRANSMITTER_MAP.get(id);

        if(handler != null) {
            handler.markComplete();
        }
    }

    public static void pauseTransmitter(UUID id) {
        MusicPlayerMidiHandler handler = TRANSMITTER_MAP.get(id);

        if(handler != null) {
            handler.pause();
        }
    }

    public static void stopTransmitter(UUID id) {
        MusicPlayerMidiHandler handler = TRANSMITTER_MAP.get(id);

        if(handler != null) {
            handler.stop();
        }
    }

    public static MusicPlayerMidiHandler getTransmitterHandler(UUID id) {
        return TRANSMITTER_MAP.get(id);
    }

    public static Boolean hasTransmitter(UUID id) {
        return TRANSMITTER_MAP.containsKey(id);
    }
    
    public static void clearMusicPlayers() {
        for(UUID id : TRANSMITTER_MAP.keySet()) {
            stopTransmitter(id);
        }
        TRANSMITTER_MAP = new HashMap<>();
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

        stopTransmitter(event.getEntity().getUUID());
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        
        stopTransmitter(event.getEntity().getUUID());
    }
}
