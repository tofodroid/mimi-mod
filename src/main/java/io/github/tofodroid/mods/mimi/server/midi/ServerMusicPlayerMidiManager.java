package io.github.tofodroid.mods.mimi.server.midi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.sound.midi.Sequence;

import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.ItemTransmitter;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileCacheManager;
import io.github.tofodroid.mods.mimi.common.network.ActiveTransmitterIdPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiStatus.STATUS_CODE;
import io.github.tofodroid.mods.mimi.common.tile.TileBroadcaster;
import io.github.tofodroid.mods.mimi.util.RemoteMidiUrlUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerMusicPlayerMidiManager {
    protected static Map<UUID,MusicPlayerMidiHandler> BROADCASTER_MAP = new HashMap<>();
    protected static Map<UUID,Pair<Integer,MusicPlayerMidiHandler>> TRANSMITTER_MAP = new HashMap<>();
    protected static Map<UUID,ItemStack> TRANSMITTER_STACK_MAP = new HashMap<>();

    // Util
    public static void revalidate() {
        // Broadcasters
        final ArrayList<UUID> toRemove = new ArrayList<>();
        BROADCASTER_MAP.entrySet().forEach(entry -> {
            if(!RemoteMidiUrlUtils.validateMidiHost(entry.getValue().url) && !RemoteMidiUrlUtils.validateFileUrl(entry.getValue().url)) {
                toRemove.add(entry.getKey());
            }
        });
        for(UUID id : toRemove) {
            stopBroadcaster(id);
        }

        // Transmitters
        toRemove.clear();
        TRANSMITTER_MAP.entrySet().forEach(entry -> {
            if(!RemoteMidiUrlUtils.validateMidiHost(entry.getValue().getRight().url) && !RemoteMidiUrlUtils.validateFileUrl(entry.getValue().getRight().url)) {
                toRemove.add(entry.getKey());
            }
        });
        for(UUID id : toRemove) {
            stopTransmitter(id);
        }
    }

    // Broadcaster
    public static Boolean createBroadcaster(TileBroadcaster tile, String midiUrl) {
        Pair<Sequence, STATUS_CODE> midiStatus = MidiFileCacheManager.getOrCreateCachedSequence(midiUrl);

        if(midiStatus.getRight() == null && midiStatus.getLeft() != null) {
            try {
                stopBroadcaster(tile.getMusicPlayerId());
                BROADCASTER_MAP.put(tile.getMusicPlayerId(), new MusicPlayerMidiHandler(tile, midiStatus.getLeft(), midiUrl));
                return true;
            } catch(Exception e) {
                MIMIMod.LOGGER.warn(e);
            }
        }
        
        return false;
    }

    public static void playBroadcaster(UUID id) {
        MusicPlayerMidiHandler handler = BROADCASTER_MAP.get(id);

        if(handler != null) {
            handler.play();
        }
    }

    public static void pauseBroadcaster(UUID id) {
        MusicPlayerMidiHandler handler = BROADCASTER_MAP.get(id);

        if(handler != null) {
            handler.pause();
        }
    }

    public static void stopBroadcaster(UUID id) {
        MusicPlayerMidiHandler handler = BROADCASTER_MAP.get(id);

        if(handler != null) {
            handler.close();
            BROADCASTER_MAP.remove(id);
        }
    }

    public static MusicPlayerMidiHandler getBroadcaster(UUID id) {
        return BROADCASTER_MAP.get(id);
    }
    
    // Transmitter
    public static Boolean createTransmitter(Player player, Integer slot, String midiUrl) {
        Pair<Sequence, STATUS_CODE> midiStatus = MidiFileCacheManager.getOrCreateCachedSequence(midiUrl);

        if(midiStatus.getRight() == null && midiStatus.getLeft() != null && player.getInventory().getItem(slot).getItem() instanceof ItemTransmitter) {
            try {
                stopTransmitter(player.getUUID());
                TRANSMITTER_MAP.put(player.getUUID(), Pair.of(slot, new MusicPlayerMidiHandler(player, midiStatus.getLeft(), midiUrl)));
                ItemTransmitter.setTransmitId(player.getInventory().getItem(slot), UUID.randomUUID());
                TRANSMITTER_STACK_MAP.put(player.getUUID(), player.getInventory().getItem(slot));
                return true;
            } catch(Exception e) {
                MIMIMod.LOGGER.warn(e);
            }
        }
        
        return false;
    }

    public static void playTransmitter(UUID id) {
        Pair<Integer,MusicPlayerMidiHandler> handler = TRANSMITTER_MAP.get(id);

        if(handler != null) {
            handler.getRight().play();
        }
    }
    
    public static void pauseTransmitter(UUID id) {
        Pair<Integer,MusicPlayerMidiHandler> handler = TRANSMITTER_MAP.get(id);

        if(handler != null) {
            handler.getRight().pause();
        }
    }

    public static void stopTransmitter(UUID id) {
        Pair<Integer,MusicPlayerMidiHandler> handler = TRANSMITTER_MAP.get(id);

        if(handler != null) {
            handler.getRight().close();
            TRANSMITTER_MAP.remove(id);
            TRANSMITTER_STACK_MAP.remove(id);
            
            ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(id);

            if(player != null) {
                NetworkManager.INFO_CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new ActiveTransmitterIdPacket(null)
                );
            }
            
        }
    }

    public static Pair<Integer,MusicPlayerMidiHandler> getTransmitter(UUID id) {
        return TRANSMITTER_MAP.get(id);
    }
    
    public static ItemStack getTransmitterStack(UUID id) {
        return TRANSMITTER_STACK_MAP.get(id);
    }
    
    public static void forceUpdateTransmitterStack(UUID id, ItemStack newStack) {
        if(TRANSMITTER_STACK_MAP.get(id) != null) {
            TRANSMITTER_STACK_MAP.put(id, newStack);
        }
    }
    
    public static void clearMusicPlayers() {
        for(UUID id : BROADCASTER_MAP.keySet()) {
            stopBroadcaster(id);
        }
        BROADCASTER_MAP = new HashMap<>();
        
        for(UUID id : TRANSMITTER_MAP.keySet()) {
            stopTransmitter(id);
        }
        TRANSMITTER_MAP = new HashMap<>();
        TRANSMITTER_STACK_MAP = new HashMap<>();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.SERVER) {
            return;
        }
        
        if(TRANSMITTER_MAP.containsKey(event.player.getUUID())) {
            ItemStack oldStack = TRANSMITTER_STACK_MAP.get(event.player.getUUID()).copy();
            ItemStack newStack = event.player.getInventory().getItem(TRANSMITTER_MAP.get(event.player.getUUID()).getLeft()).copy();

            if(newStack.getItem() instanceof ItemTransmitter) {
                ItemTransmitter.setPublicTransmit(oldStack, ItemTransmitter.getPublicTransmit(newStack));

                if(ItemStack.matches(oldStack, newStack)) {
                    TRANSMITTER_STACK_MAP.put(event.player.getUUID(), newStack);
                    return;
                }
            }

            stopTransmitter(event.player.getUUID());
            NetworkManager.INFO_CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> (ServerPlayer)event.player),
                new ActiveTransmitterIdPacket(null)
            );
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
        NetworkManager.INFO_CHANNEL.send(
            PacketDistributor.PLAYER.with(() -> (ServerPlayer)event.getEntity()),
            new ActiveTransmitterIdPacket(null)
        );
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        
        stopTransmitter(event.getEntity().getUUID());
    }
}
