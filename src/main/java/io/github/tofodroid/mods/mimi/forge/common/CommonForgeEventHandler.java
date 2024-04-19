package io.github.tofodroid.mods.mimi.forge.common;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.world.ModStructures;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.instrument.EntityInstrumentConsumerEventHandler;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter.PlayerTransmitterProducerEventHandler;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter.ServerTransmitterManager;
import io.github.tofodroid.mods.mimi.server.events.note.consumer.ServerNoteConsumerManager;
import io.github.tofodroid.mods.mimi.server.midi.ServerMidiManager;
import io.github.tofodroid.mods.mimi.server.network.ServerMidiUploadManager;
import net.minecraft.core.registries.Registries;
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
        PlayerTransmitterProducerEventHandler.onPlayerLoggedIn((ServerPlayer)event.getEntity());
        EntityInstrumentConsumerEventHandler.onPlayerLoggedIn((ServerPlayer)event.getEntity());
        ServerNoteConsumerManager.onPlayerLoggedIn((ServerPlayer)event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        ServerMidiManager.onPlayerLoggedOut((ServerPlayer)event.getEntity());
        PlayerTransmitterProducerEventHandler.onPlayerLoggedOut((ServerPlayer)event.getEntity());
        EntityInstrumentConsumerEventHandler.onPlayerLoggedOut((ServerPlayer)event.getEntity());
        ServerNoteConsumerManager.onPlayerLoggedOut((ServerPlayer)event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        EntityInstrumentConsumerEventHandler.onPlayerRespawn((ServerPlayer)event.getEntity());
        ServerNoteConsumerManager.onPlayerRespawn((ServerPlayer)event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.SERVER || !(event.player instanceof ServerPlayer)) {
            return;
        }  
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }
        EntityInstrumentConsumerEventHandler.onLivingEquipmentChange(event.getFrom(), event.getTo(), event.getEntity());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }
        EntityInstrumentConsumerEventHandler.onLivingDeath(event.getEntity());
        ServerTransmitterManager.onLivingDeath(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }
        EntityInstrumentConsumerEventHandler.onEntityTeleport(event.getEntity());
        ServerTransmitterManager.onEntityTeleport(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onEntityChangeDimension(EntityTravelToDimensionEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }
        EntityInstrumentConsumerEventHandler.onEntityChangeDimension(event.getEntity());
        ServerTransmitterManager.onEntityChangeDimension(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.SERVER) {
            return;
        }
        BroadcastManager.onServerTick();
        ServerNoteConsumerManager.onServerTick();
        ServerMidiUploadManager.onServerTick();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        BroadcastManager.onServerStopping();
        ServerTransmitterManager.onServerStopping();
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        ModStructures.registerVillageStructures(
            event.getServer().registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow(),
		    event.getServer().registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow()
        );
        ServerTransmitterManager.onServerAboutToStart();
    }
}
