package io.github.tofodroid.mods.mimi.server.midi.receiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentHandheld;
import io.github.tofodroid.mods.mimi.common.midi.TransmitterNoteEvent;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacketHandler;
import io.github.tofodroid.mods.mimi.common.network.MultiMidiNotePacket;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.server.ServerExecutor;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class ServerMusicReceiverManager {
    private static final List<InteractionHand> ENTITY_INSTRUMENT_ITER = Collections.unmodifiableList(
        Arrays.asList(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND, null)
    );

    protected static final ConcurrentHashMap<UUID, List<? extends AMusicReceiver>> OWNED_RECEIVERS = new ConcurrentHashMap<>();
    //protected static final ConcurrentHashMap<UUID, List<AMusicReceiver>> SOURCE_LINKED_RECEIVERS = new ConcurrentHashMap<>();
    protected static final ConcurrentHashMap<ResourceKey<Level>, ConcurrentHashMap<UUID, ConcurrentHashMap<Byte, List<AMusicReceiver>>>> RECEIVER_LOOKUP = new ConcurrentHashMap<>();

    public static void handlePacket(TransmitterNoteEvent packet, UUID sourceId, BlockPos sourcePos, ServerLevel sourceLevel) {
        List<AMusicReceiver> receivers = null;
        ConcurrentHashMap<UUID, ConcurrentHashMap<Byte, List<AMusicReceiver>>> sourceMap = RECEIVER_LOOKUP.get(sourceLevel.dimension());

        if(sourceMap != null) {
            ConcurrentHashMap<Byte, List<AMusicReceiver>> channelMap = sourceMap.get(sourceId);

            if(channelMap != null) {
                receivers = channelMap.get(packet.channel);
            }
        }

        if(receivers != null && !receivers.isEmpty()) {
            HashMap<UUID, List<MidiNotePacket>> packetList = new HashMap<>();

            receivers.forEach(receiver -> {
                MidiNotePacket sendPacket = receiver.handlePacket(packet, sourceId, sourcePos, sourceLevel);

                if(sendPacket != null) {
                    packetList.computeIfAbsent(sendPacket.player, u -> new ArrayList<MidiNotePacket>()).add(sendPacket);
                }
            });

            if(!packetList.isEmpty()) {
                ServerExecutor.executeOnServerThread(
                    () -> MidiNotePacketHandler.handlePacketServer(new MultiMidiNotePacket(packetList, packet.note, packet.noteServerTime, packet.isNoteOffEvent()), sourceLevel)
                );
            }
        }
    }

    @SuppressWarnings("resource")
    public static void loadMechanicalMaestroInstrumentReceivers(TileMechanicalMaestro tile) {
        if(tile == null || !(tile.getLevel() instanceof ServerLevel)) {
            return;
        }

        List<InstrumentMusicReceiver> receivers = new ArrayList<>();

        tile.getInstrumentStacks().forEach(instrumentStack -> {
            if(tile.getLevel() != null && instrumentStack != null && MidiNbtDataUtils.getMidiSource(instrumentStack) != null) {
                InstrumentMusicReceiver newReceiver = new InstrumentMusicReceiver(
                    tile::getBlockPos,
                    () -> tile.getLevel().dimension(),
                    TileMechanicalMaestro.MECH_SOURCE_ID,
                    instrumentStack,
                    null
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

    @SuppressWarnings("resource")
    public static void loadEntityInstrumentReceivers(LivingEntity entity) {
        if(entity == null || !(entity.level() instanceof ServerLevel)) {
            return;
        }

        List<InstrumentMusicReceiver> receivers = new ArrayList<>();

        ENTITY_INSTRUMENT_ITER.forEach(hand -> {
            ItemStack instrumentStack = hand != null ? 
                ItemInstrumentHandheld.getEntityHeldInstrumentStack(entity, hand) : 
                BlockInstrument.getTileInstrumentStackForEntity(entity);
            
            if(entity.level() != null && instrumentStack != null && MidiNbtDataUtils.getMidiSource(instrumentStack) != null) {
                InstrumentMusicReceiver newReceiver = new InstrumentMusicReceiver(
                    entity::getOnPos,
                    () -> entity.level().dimension(),
                    entity.getUUID(),
                    instrumentStack,
                    hand
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
                .filter(r -> !newRecievers.contains(r))
                .forEach(r -> ((InstrumentMusicReceiver)r).allNotesOff(ownerLevel));
        }
    }

    public static void loadReceiverTileReceiver(TileReceiver tile) { 
        List<ReceiverTileReceiver> receivers = new ArrayList<>();

        if(tile.getLevel() != null && MidiNbtDataUtils.getMidiSource(tile.getSourceStack()) != null) {
            receivers.add(new ReceiverTileReceiver(tile));
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

    public static void allInstrumentReceiverNotesOff(UUID ownerId, ServerLevel ownerLevel) {
        List<? extends AMusicReceiver> recievers = OWNED_RECEIVERS.get(ownerId);

        if(recievers != null && !recievers.isEmpty()) {
            recievers.stream()
                .filter(r -> r instanceof InstrumentMusicReceiver)
                .forEach(r -> ((InstrumentMusicReceiver)r).allNotesOff(ownerLevel));
        }
    }

    public static void onLivingEquipmentChange(ItemStack from, ItemStack to, LivingEntity entity) {
        if(from.getItem() instanceof ItemInstrumentHandheld || to.getItem() instanceof ItemInstrumentHandheld) {
            loadEntityInstrumentReceivers(entity);
        }
    }

    public static void onLivingDeath(LivingEntity entity) {
        allInstrumentReceiverNotesOff(entity.getUUID(), (ServerLevel)entity.level());
    }

    public static void onEntityTeleport(Entity entity) {
        if(entity instanceof LivingEntity) {
            allInstrumentReceiverNotesOff(entity.getUUID(), (ServerLevel)entity.level());
        }
    }

    public static void onEntityChangeDimension(Entity entity) {
        if(entity instanceof LivingEntity) {
            allInstrumentReceiverNotesOff(entity.getUUID(), (ServerLevel)entity.level());
        }
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        if(player.level() instanceof ServerLevel) {
            allInstrumentReceiverNotesOff(player.getUUID(), (ServerLevel)player.level());
            removeReceivers(player.getUUID());
        }
    }

    public static void onPlayerLoggedIn(ServerPlayer player) {
        loadEntityInstrumentReceivers(player);
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        loadEntityInstrumentReceivers(player);
    }

    public static void onServerTick() {
        if(!ServerMusicTransmitterManager.hasPlayingTransmitters()) {
            RECEIVER_LOOKUP.clear();
            return;
        }
        
        // Identify linked receivers
        RECEIVER_LOOKUP.clear();
        OWNED_RECEIVERS.values().stream().forEach(receiverList -> {
            receiverList.stream().forEach(receiver -> {
                ConcurrentHashMap<UUID, ConcurrentHashMap<Byte, List<AMusicReceiver>>> sourceMap = RECEIVER_LOOKUP.computeIfAbsent(
                    receiver.dimension.get(), rk -> new ConcurrentHashMap<>()
                );

                ConcurrentHashMap<Byte, List<AMusicReceiver>> channelMap = sourceMap.computeIfAbsent(
                    receiver.linkedId, id -> new ConcurrentHashMap<>()
                );

                // Add to ALL_CHANNELS and each enabled channel
                List<AMusicReceiver> allReceivers = channelMap.computeIfAbsent(
                    TransmitterNoteEvent.ALL_CHANNELS, k -> Collections.synchronizedList(new ArrayList<>())
                );
                allReceivers.add(receiver);

                receiver.enabledChannelsList.forEach(channel -> {
                    List<AMusicReceiver> channelReceivers = channelMap.computeIfAbsent(
                        channel, k -> Collections.synchronizedList(new ArrayList<>())
                    );
                    channelReceivers.add(receiver);
                });
            });            
        });
    }

    public static void onServerStopping() {
        OWNED_RECEIVERS.clear();
        RECEIVER_LOOKUP.clear();
    }
}
