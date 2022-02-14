package io.github.tofodroid.mods.mimi.common.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;

public class TransmitterNotePacketHandler {
    public static void handlePacket(final TransmitterNotePacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender().getOnPos(), ctx.get().getSender().getLevel(),  ctx.get().getSender().getUUID(), ctx.get().getSender()));
        }

        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final TransmitterNotePacket message, BlockPos sourcePos, ServerLevel worldIn, UUID senderId, ServerPlayer sender) {
        HashMap<UUID, List<MidiNotePacket>> notePackets = new HashMap<>();

        // Handle Players
        for(ServerPlayer player : getPotentialPlayers(message.transmitMode, sourcePos, worldIn, sender, getQueryBoxRange(message.velocity <= 0))) {
            List<MidiNotePacket> playerPackets = new ArrayList<>();
            
            // Held Instruments
            handleHeldInstrumentRelayNote(player, senderId, InteractionHand.MAIN_HAND, message, playerPackets);
            handleHeldInstrumentRelayNote(player, senderId, InteractionHand.OFF_HAND, message, playerPackets);

            // Seated Instrument
            handleEntityInstrumentRelayNote(player, senderId, message, playerPackets);

            notePackets.put(player.getUUID(), playerPackets);
        }

        // Handle Mechanical Maestros
        notePackets.put(TileMechanicalMaestro.MECH_UUID, new ArrayList<>());
        for(TileMechanicalMaestro maestro : getPotentialMechMaestros(getPotentialEntities(message.transmitMode, sourcePos, worldIn, getQueryBoxRange(message.velocity <= 0)))) {
            if(maestro.shouldHandleMessage(senderId, message.channel, message.note, message.transmitMode == TransmitMode.PUBLIC)) {
                if(message.isControlPacket()) {
                    notePackets.get(TileMechanicalMaestro.MECH_UUID).add(MidiNotePacket.createControlPacket(message.getControllerNumber(), message.getControllerValue(), maestro.getInstrumentId(), TileMechanicalMaestro.MECH_UUID, maestro.getBlockPos()));
                } else {
                    notePackets.get(TileMechanicalMaestro.MECH_UUID).add(new MidiNotePacket(message.note, ItemMidiSwitchboard.applyVolume(maestro.getSwitchboardStack(), message.velocity), maestro.getInstrumentId(), TileMechanicalMaestro.MECH_UUID, maestro.getBlockPos()));
                }
            }
        }

        sendPlayerOnPackets(notePackets, worldIn);

        // Handle Receivers
        if(!message.isControlPacket() && message.velocity > 0) {
            for(TileReceiver receiver : getPotentialReceivers(getPotentialEntities(message.transmitMode, sourcePos, worldIn, getQueryBoxRange(false)))) {
                if(receiver.shouldHandleMessage(senderId, message.channel, message.note, message.transmitMode == TransmitMode.PUBLIC)) {
                    ModBlocks.RECEIVER.powerTarget(worldIn, receiver.getBlockState(), 15, receiver.getBlockPos());
                }
            }
        }
    }
    
    // Tile Entity Functions
    protected static void handleEntityInstrumentRelayNote(ServerPlayer target, UUID sourceId, final TransmitterNotePacket message, List<MidiNotePacket> packetList) {
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(target);

        if(instrumentEntity != null) { 
            Byte instrumentId = instrumentEntity.getInstrumentId();
            if(instrumentId != null && instrumentEntity.shouldHandleMessage(sourceId, message.channel, TransmitMode.PUBLIC.equals(message.transmitMode))) {
                if(message.isControlPacket()) {
                    packetList.add(MidiNotePacket.createControlPacket(message.getControllerNumber(), message.getControllerValue(), instrumentId, target.getUUID(), target.getOnPos()));
                } else {
                    packetList.add(new MidiNotePacket(message.note, ItemMidiSwitchboard.applyVolume(instrumentEntity.getSwitchboardStack(), message.velocity), instrumentId, target.getUUID(), target.getOnPos()));
                }
            }
        }
    }

    // Item Stack Functions
    protected static void handleHeldInstrumentRelayNote(ServerPlayer target, UUID sourceId, InteractionHand handIn, final TransmitterNotePacket message, List<MidiNotePacket> packetList) {
        ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(target, handIn);
        Byte instrumentId = ItemInstrument.getInstrumentId(stack);
        if(instrumentId != null && stack != null && ItemInstrument.shouldHandleMessage(stack, sourceId, message.channel, TransmitMode.PUBLIC.equals(message.transmitMode))) {
            if(message.isControlPacket()) {
                packetList.add(MidiNotePacket.createControlPacket(message.getControllerNumber(), message.getControllerValue(), instrumentId, target.getUUID(), target.getOnPos()));
            } else {
                packetList.add(new MidiNotePacket(message.note, ItemMidiSwitchboard.applyVolume(ItemInstrument.getSwitchboardStack(stack), message.velocity), instrumentId, target.getUUID(), target.getOnPos()));
            }
        }
    }
    
    // Util
    protected static List<ServerPlayer> getPotentialPlayers(TransmitMode transmitMode, BlockPos sourcePos, ServerLevel worldIn, ServerPlayer sender, Integer range) {
        List<ServerPlayer> potentialPlayers = new ArrayList<>();

        if(transmitMode != TransmitMode.SELF) {
            AABB queryBox = new AABB(sourcePos.getX() - range, sourcePos.getY() - range, sourcePos.getZ() - range, 
                                                sourcePos.getX() + range, sourcePos.getY() + range, sourcePos.getZ() + range);
            potentialPlayers = worldIn.getEntitiesOfClass(ServerPlayer.class, queryBox, entity -> {
                return ItemInstrument.isEntityHoldingInstrument(entity) || BlockInstrument.isEntitySittingAtInstrument(entity);
            });
        } else if(sender != null) {
            potentialPlayers.add(sender);
        }

        return potentialPlayers;
    }

    protected static List<EntityNoteResponsiveTile> getPotentialEntities(TransmitMode transmitMode, BlockPos sourcePos, ServerLevel worldIn, Integer range) {
        List<EntityNoteResponsiveTile> potentialEntites = new ArrayList<>();
        
        if(transmitMode != TransmitMode.SELF) {
            AABB queryBox = new AABB(sourcePos.getX() - range, sourcePos.getY() - range, sourcePos.getZ() - range, 
                                                        sourcePos.getX() + range, sourcePos.getY() + range, sourcePos.getZ() + range);
            potentialEntites = worldIn.getEntitiesOfClass(EntityNoteResponsiveTile.class, queryBox, entity -> {
                return entity.getTile() != null;
            });
        }

        return potentialEntites;
    }

    protected static List<TileMechanicalMaestro> getPotentialMechMaestros(List<EntityNoteResponsiveTile> entities) {
        return entities.stream().filter(e -> e.getTile() instanceof TileMechanicalMaestro).map(e -> (TileMechanicalMaestro)e.getTile()).collect(Collectors.toList());
    }

    protected static List<TileReceiver> getPotentialReceivers(List<EntityNoteResponsiveTile> entities) {
        return entities.stream().filter(e -> e.getTile() instanceof TileReceiver).map(e -> (TileReceiver)e.getTile()).collect(Collectors.toList());
    }

    protected static void sendPlayerOnPackets(HashMap<UUID, List<MidiNotePacket>> notePackets, ServerLevel worldIn) {
        if(!notePackets.isEmpty()) {
            for(UUID playerId : notePackets.keySet()) {
                List<MidiNotePacket> playerPackets = notePackets.get(playerId);

                if(playerPackets != null && !playerPackets.isEmpty()) {
                    MidiNotePacketHandler.handlePacketsServer(notePackets.get(playerId), worldIn, null);
                }
            }
        }
    }

    protected static Integer getQueryBoxRange(Boolean off) {
        return off ? 32 : 16;
    }
}
