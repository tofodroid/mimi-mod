package io.github.tofodroid.mods.mimi.common.network;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;

public class TransmitterNotePacketHandler {
    public static void handlePacket(final TransmitterNotePacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handleOnPacketServer(message, ctx.get().getSender()));
        }

        ctx.get().setPacketHandled(true);
    }
    
    public static void handleOnPacketServer(final TransmitterNotePacket message, ServerPlayerEntity sender) {
        HashMap<ServerPlayerEntity, List<MidiNotePacket>> notePackets = new HashMap<>();
        notePackets.put(sender, new ArrayList<>());

        // Handle Players
        for(ServerPlayerEntity player : getPotentialPlayers(message.transmitMode, sender)) {
            List<MidiNotePacket> playerPackets = new ArrayList<>();
            
            // Held Instruments
            handleHeldInstrumentRelayNote(player, sender.getUniqueID(), Hand.MAIN_HAND, message, playerPackets);
            handleHeldInstrumentRelayNote(player, sender.getUniqueID(), Hand.OFF_HAND, message, playerPackets);

            // Seated Instrument
            handleEntityInstrumentRelayNote(player, sender.getUniqueID(), message, playerPackets);

            notePackets.put(player, playerPackets);
        }

        // Handle Mechanical Maestros
        for(TileMechanicalMaestro maestro : getPotentialMechMaestros(message.transmitMode, sender)) {
            if(maestro.shouldHandleMessage(sender.getUniqueID(), message.channel, message.note, message.transmitMode == TransmitMode.PUBLIC)) {
                notePackets.get(sender).add(new MidiNotePacket(message.channel, message.note, message.velocity, maestro.getInstrumentId(), maestro.getMaestroUUID(), true, maestro.getPos()));
            }
        }

        sendPlayerOnPackets(notePackets);

        // Handle Receivers
        if(message.velocity > 0) {
            for(TileReceiver receiver : getPotentialReceivers(message.transmitMode, sender)) {
                if(receiver.shouldHandleMessage(sender.getUniqueID(), message.channel, message.note, message.transmitMode == TransmitMode.PUBLIC)) {
                    ModBlocks.RECEIVER.powerTarget(sender.getServerWorld(), receiver.getBlockState(), 15, receiver.getPos());
                }
            }
        }
    }
    
    // Tile Entity Functions
    protected static void handleEntityInstrumentRelayNote(ServerPlayerEntity target, UUID sourceId, final TransmitterNotePacket message, List<MidiNotePacket> packetList) {
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(target);

        if(instrumentEntity != null) { 
            Byte instrumentId = instrumentEntity.getInstrumentId();
            if(instrumentId != null && instrumentEntity.shouldHandleMessage(sourceId, message.channel, TransmitMode.PUBLIC.equals(message.transmitMode))) {
                packetList.add(new MidiNotePacket(message.channel, message.note, message.velocity, instrumentId, target.getUniqueID(), false, target.getPosition()));
            }
        }
    }

    // Item Stack Functions
    protected static void handleHeldInstrumentRelayNote(ServerPlayerEntity target, UUID sourceId, Hand handIn, final TransmitterNotePacket message, List<MidiNotePacket> packetList) {
        ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(target, handIn);
        Byte instrumentId = ItemInstrument.getInstrumentId(stack);
        if(instrumentId != null && stack != null && ItemInstrument.shouldHandleMessage(stack, sourceId, message.channel, TransmitMode.PUBLIC.equals(message.transmitMode))) {
            packetList.add(new MidiNotePacket(message.channel, message.note, message.velocity, instrumentId, target.getUniqueID(), false, target.getPosition()));
        }
    }
    
    // Util
    protected static List<ServerPlayerEntity> getPotentialPlayers(TransmitMode transmitMode, ServerPlayerEntity sender) {
        List<ServerPlayerEntity> potentialPlayers = Arrays.asList(sender);

        if(transmitMode != TransmitMode.SELF) {
            BlockPos senderPos = sender.getPosition();
            AxisAlignedBB queryBox = new AxisAlignedBB(senderPos.getX() - 16, senderPos.getY() - 16, senderPos.getZ() - 16, 
                                                    senderPos.getX() + 16, senderPos.getY() + 16, senderPos.getZ() + 16);
            potentialPlayers = sender.getServerWorld().getEntitiesWithinAABB(ServerPlayerEntity.class, queryBox, entity -> {
                return ItemInstrument.isEntityHoldingInstrument(entity) || BlockInstrument.isEntitySittingAtInstrument(entity);
            });
        }

        return potentialPlayers;
    }

    protected static List<TileMechanicalMaestro> getPotentialMechMaestros(TransmitMode transmitMode, ServerPlayerEntity sender) {
        List<TileMechanicalMaestro> potentialMaestros = new ArrayList<>();

        if(transmitMode != TransmitMode.SELF) {
            BlockPos senderPos = sender.getPosition();
            AxisAlignedBB queryBox = new AxisAlignedBB(senderPos.getX() - 16, senderPos.getY() - 16, senderPos.getZ() - 16, 
                                                    senderPos.getX() + 16, senderPos.getY() + 16, senderPos.getZ() + 16);
            potentialMaestros = BlockPos.getAllInBox(queryBox).filter(pos -> ModBlocks.MECHANICALMAESTRO.equals(sender.getServerWorld().getBlockState(pos).getBlock()))
                .map(b -> ModBlocks.MECHANICALMAESTRO.getTileForBlock(sender.getServerWorld(), b)).filter(t -> t != null).collect(Collectors.toList());
        }

        return potentialMaestros;
    }

    protected static List<TileReceiver> getPotentialReceivers(TransmitMode transmitMode, ServerPlayerEntity sender) {
        List<TileReceiver> potentialReceivers = new ArrayList<>();

        if(transmitMode != TransmitMode.SELF) {
            BlockPos senderPos = sender.getPosition();
            AxisAlignedBB queryBox = new AxisAlignedBB(senderPos.getX() - 16, senderPos.getY() - 16, senderPos.getZ() - 16, 
                                                    senderPos.getX() + 16, senderPos.getY() + 16, senderPos.getZ() + 16);
            potentialReceivers = BlockPos.getAllInBox(queryBox).filter(pos -> ModBlocks.RECEIVER.equals(sender.getServerWorld().getBlockState(pos).getBlock()))
                .map(b -> ModBlocks.RECEIVER.getTileForBlock(sender.getServerWorld(), b)).filter(t -> t != null).collect(Collectors.toList());
        }

        return potentialReceivers;
    }

    protected static void sendPlayerOnPackets(HashMap<ServerPlayerEntity, List<MidiNotePacket>> notePackets) {
        if(!notePackets.isEmpty()) {
            for(ServerPlayerEntity player : notePackets.keySet()) {
                List<MidiNotePacket> playerPackets = notePackets.get(player);

                if(playerPackets != null && !playerPackets.isEmpty()) {
                    MidiNotePacketHandler.handlePacketsServer(notePackets.get(player), player.getServerWorld(), null);
                }
            }
        }
    }
}
