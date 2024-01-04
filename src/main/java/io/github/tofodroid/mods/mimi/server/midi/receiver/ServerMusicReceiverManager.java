package io.github.tofodroid.mods.mimi.server.midi.receiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentHandheld;
import io.github.tofodroid.mods.mimi.common.midi.TransmitterNoteEvent;
import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerMusicReceiverManager {
    private static final List<InteractionHand> ENTITY_INSTRUMENT_ITER = Collections.unmodifiableList(
        Arrays.asList(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND, null)
    );

    protected static final ConcurrentHashMap<UUID, List<? extends AMusicReceiver>> OWNED_RECEIVERS = new ConcurrentHashMap<>();
    protected static final ConcurrentHashMap<UUID, List<AMusicReceiver>> SOURCE_LINKED_RECEIVERS = new ConcurrentHashMap<>();

    public static void handlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        List<AMusicReceiver> receivers = SOURCE_LINKED_RECEIVERS.get(sourceId);

        if(receivers != null) {
            receivers.stream().forEach(receiver -> 
                receiver.handlePacket(packet, sourceId, sourcePos, sourceLevel)
            );
        }
    }

    @SuppressWarnings("null")
    public static void loadMechanicalMaestroInstrumentReceivers(TileMechanicalMaestro tile) {
        if(!(tile.getLevel() instanceof ServerLevel)) {
            return;
        }

        List<InstrumentMusicReceiver> receivers = new ArrayList<>();

        tile.getInstrumentStacks().forEach(instrumentStack -> {
            if(tile.getLevel() != null && instrumentStack != null && InstrumentDataUtils.getMidiSource(instrumentStack) != null) {
                InstrumentMusicReceiver newReceiver = new InstrumentMusicReceiver(
                    tile::getBlockPos,
                    () -> tile.getLevel().dimension(),
                    TileMechanicalMaestro.MECH_SOURCE_ID,
                    instrumentStack
                );
                receivers.add(newReceiver);
            }
        });
        allNotesOffRemovedInstrumentRecievers(receivers, tile.getUUID(), (ServerLevel)tile.getLevel());

        if(!receivers.isEmpty()) {
            OWNED_RECEIVERS.put(tile.getUUID(), receivers);
        } else {
            OWNED_RECEIVERS.remove(tile.getUUID());
        }
    }

    public static void loadEntityInstrumentReceivers(LivingEntity entity) {
        if(!(entity.level() instanceof ServerLevel)) {
            return;
        }

        List<InstrumentMusicReceiver> receivers = new ArrayList<>();

        ENTITY_INSTRUMENT_ITER.forEach(hand -> {
            ItemStack instrumentStack = hand != null ? 
                ItemInstrumentHandheld.getEntityHeldInstrumentStack(entity, hand) : 
                BlockInstrument.getTileInstrumentStackForEntity(entity);
            
            if(entity.level() != null && instrumentStack != null && InstrumentDataUtils.getMidiSource(instrumentStack) != null) {
                InstrumentMusicReceiver newReceiver = new InstrumentMusicReceiver(
                    entity::getOnPos,
                    () -> entity.level().dimension(),
                    entity.getUUID(),
                    instrumentStack
                );
                receivers.add(newReceiver);
            }
        });
        allNotesOffRemovedInstrumentRecievers(receivers, entity.getUUID(), (ServerLevel)entity.level());

        if(!receivers.isEmpty()) {
            OWNED_RECEIVERS.put(entity.getUUID(), receivers);
        } else {
            OWNED_RECEIVERS.remove(entity.getUUID());
        }
    }

    public static void allNotesOffRemovedInstrumentRecievers(List<InstrumentMusicReceiver> newRecievers, UUID ownerId, ServerLevel ownerLevel) {
        List<? extends AMusicReceiver> oldReceivers = OWNED_RECEIVERS.get(ownerId);

        if(oldReceivers != null) {
            oldReceivers.stream()
                .forEach(r -> ((InstrumentMusicReceiver)r).allNotesOff(ownerLevel));
        }
    }
    
    public static void loadConfigurableMidiNoteResponsiveTileReceiver(AConfigurableMidiNoteResponsiveTile tile) { 
        List<ConfigurableMidiNoteResponsiveTileReceiver> receivers = new ArrayList<>();

        if(tile.getLevel() != null && InstrumentDataUtils.getMidiSource(tile.getSourceStack()) != null) {
            receivers.add(new ConfigurableMidiNoteResponsiveTileReceiver(tile));
        }
        
        if(!receivers.isEmpty()) {
            OWNED_RECEIVERS.put(tile.getUUID(), receivers);
        } else {
            OWNED_RECEIVERS.remove(tile.getUUID());
        }
    }

    public static void removeReceivers(UUID id) {
        OWNED_RECEIVERS.remove(id);
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }

        if(event.getFrom().getItem() instanceof ItemInstrumentHandheld || event.getTo().getItem() instanceof ItemInstrumentHandheld) {
            loadEntityInstrumentReceivers(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }
        allInstrumentReceiverNotesOff(event.getEntity().getUUID(), (ServerLevel)event.getEntity().level());
    }

    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel)) {
            return;
        }
        allInstrumentReceiverNotesOff(event.getEntity().getUUID(), (ServerLevel)event.getEntity().level());
    }
    
    @SubscribeEvent
    public static void onEntityChangeDimension(EntityTravelToDimensionEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel) || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        allInstrumentReceiverNotesOff(event.getEntity().getUUID(), (ServerLevel)event.getEntity().level());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        allInstrumentReceiverNotesOff(event.getEntity().getUUID(), (ServerLevel)event.getEntity().level());
        removeReceivers(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        loadEntityInstrumentReceivers(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }
        loadEntityInstrumentReceivers(event.getEntity());
    }
    
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        // Pre-conditions
        if(event.phase != Phase.END || event.side != LogicalSide.SERVER) {
            return;
        } else if(!ServerMusicTransmitterManager.hasPlayingTransmitters()) {
            SOURCE_LINKED_RECEIVERS.clear();
            return;
        }
        
        // Identify linked receivers
        SOURCE_LINKED_RECEIVERS.clear();
        OWNED_RECEIVERS.values().stream().forEach(receiverList -> {
            receiverList.stream().forEach(receiver -> {
                List<AMusicReceiver> linkedReceivers = SOURCE_LINKED_RECEIVERS.computeIfAbsent(
                    receiver.getLinkedId(), k -> Collections.synchronizedList(new ArrayList<>())
                );
                linkedReceivers.add(receiver);
            });            
        });
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        OWNED_RECEIVERS.clear();
        SOURCE_LINKED_RECEIVERS.clear();
    }

    public static void allInstrumentReceiverNotesOff(UUID ownerId, ServerLevel ownerLevel) {
        List<? extends AMusicReceiver> recievers = OWNED_RECEIVERS.get(ownerId);

        if(recievers != null && !recievers.isEmpty()) {
            recievers.stream()
                .filter(r -> r instanceof InstrumentMusicReceiver)
                .forEach(r -> ((InstrumentMusicReceiver)r).allNotesOff(ownerLevel));
        }
    }
}
