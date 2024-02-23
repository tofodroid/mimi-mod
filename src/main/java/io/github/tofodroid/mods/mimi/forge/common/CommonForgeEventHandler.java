package io.github.tofodroid.mods.mimi.forge.common;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacketHandler;
import io.github.tofodroid.mods.mimi.common.world.ModStructures;
import io.github.tofodroid.mods.mimi.server.midi.ServerMidiManager;
import io.github.tofodroid.mods.mimi.server.midi.receiver.ServerMusicReceiverManager;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import io.github.tofodroid.mods.mimi.server.network.ServerMidiUploadManager;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonForgeEventHandler {
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        ServerMidiManager.onPlayerLoggedIn((ServerPlayer)event.getEntity());
        ServerMusicReceiverManager.onPlayerLoggedIn((ServerPlayer)event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        ServerMidiManager.onPlayerLoggedOut((ServerPlayer)event.getEntity());
        ServerMusicReceiverManager.onPlayerLoggedOut((ServerPlayer)event.getEntity());
        ServerMusicTransmitterManager.onPlayerLoggedOut((ServerPlayer)event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        ServerMusicReceiverManager.onPlayerRespawn((ServerPlayer)event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.SERVER || !(event.player instanceof ServerPlayer)) {
            return;
        }
        
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if(!(event.getEntity().getLevel() instanceof ServerLevel)) {
            return;
        }
        ServerMusicReceiverManager.onLivingEquipmentChange(event.getFrom(), event.getTo(), event.getEntity());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if(!(event.getEntity().getLevel() instanceof ServerLevel)) {
            return;
        }
        ServerMusicReceiverManager.onLivingDeath(event.getEntity());
        ServerMusicTransmitterManager.onLivingDeath(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if(!(event.getEntity().getLevel() instanceof ServerLevel)) {
            return;
        }
        ServerMusicReceiverManager.onEntityTeleport(event.getEntity());
        ServerMusicTransmitterManager.onEntityTeleport(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onEntityChangeDimension(EntityTravelToDimensionEvent event) {
        if(!(event.getEntity().getLevel() instanceof ServerLevel)) {
            return;
        }
        ServerMusicReceiverManager.onEntityChangeDimension(event.getEntity());
        ServerMusicTransmitterManager.onEntityChangeDimension(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.SERVER) {
            return;
        }
        ServerMusicReceiverManager.onServerTick();
        ServerMusicTransmitterManager.onServerTick();
        ServerMidiUploadManager.onServerTick();
        MidiNotePacketHandler.onServerTick();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerMusicReceiverManager.onServerStopping();
        ServerMusicTransmitterManager.onServerStopping();
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        ModStructures.registerVillageStructures(
            event.getServer().registryAccess().registry(Registry.PROCESSOR_LIST_REGISTRY).orElseThrow(),
		    event.getServer().registryAccess().registry(Registry.TEMPLATE_POOL_REGISTRY).orElseThrow()
        );
    }
}
