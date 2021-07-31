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

import io.github.tofodroid.mods.mimi.common.network.MaestroNoteOnPacket.TransmitMode;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;

public class MaestroNotePacketHandler {
    public static void handlePacket(final MaestroNoteOnPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handleOnPacketServer(message, ctx.get().getSender()));
        }

        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacket(final MaestroNoteOffPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handleOffPacketServer(message, ctx.get().getSender()));
        }
        
        ctx.get().setPacketHandled(true);
    }

    public static void handleOnPacketServer(final MaestroNoteOnPacket message, ServerPlayerEntity sender) {
        HashMap<ServerPlayerEntity, List<MidiNoteOnPacket>> notePackets = new HashMap<>();

        // Handle Players
        for(ServerPlayerEntity player : getPotentialPlayers(message.transmitMode, sender)) {
            List<MidiNoteOnPacket> playerPackets = new ArrayList<>();
            
            // Held Instruments
            handleHeldInstrumentRelayNoteOn(player, sender.getUniqueID(), Hand.MAIN_HAND, message, playerPackets);
            handleHeldInstrumentRelayNoteOn(player, sender.getUniqueID(), Hand.OFF_HAND, message, playerPackets);

            // Seated Instrument
            handleEntityInstrumentRelayNoteOn(player, sender.getUniqueID(), message, playerPackets);

            notePackets.put(player, playerPackets);
        }

        sendOnPackets(notePackets, 64.0d);

        // Handle Receivers
        for(TileReceiver receiver : getPotentialReceivers(message.transmitMode, sender)) {
            if(receiver.shouldHandleMessage(sender.getUniqueID(), message.channel, message.note, message.transmitMode == TransmitMode.PUBLIC)) {
                ModBlocks.RECEIVER.powerTarget(sender.getServerWorld(), receiver.getBlockState(), 15, receiver.getPos(), 8);
            }
        }
    }
    
    public static void handleOffPacketServer(final MaestroNoteOffPacket message, ServerPlayerEntity sender) {
        HashMap<ServerPlayerEntity, List<MidiNoteOffPacket>> notePackets = new HashMap<>();

        for(ServerPlayerEntity player : getPotentialPlayers(message.transmitMode, sender)) {
            List<MidiNoteOffPacket> playerPackets = new ArrayList<>();

            // Held Instruments
            handleHeldInstrumentRelayNoteOff(player, sender.getUniqueID(), Hand.MAIN_HAND, message, playerPackets);
            handleHeldInstrumentRelayNoteOff(player, sender.getUniqueID(), Hand.OFF_HAND, message, playerPackets);

            // Seated Instrument
            handleEntityInstrumentRelayNoteOff(player, sender.getUniqueID(), message, playerPackets);

            if(!playerPackets.isEmpty()) {
                notePackets.put(player, playerPackets);
            }
        }

        sendOffPackets(notePackets, 96.0d);
    }
    
    // Tile Entity Functions
    protected static void handleEntityInstrumentRelayNoteOn(ServerPlayerEntity target, UUID maestro, final MaestroNoteOnPacket message, List<MidiNoteOnPacket> packetList) {
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(target);

        if(instrumentEntity != null) { 
            Byte instrumentId = instrumentEntity.getInstrumentId();
            if(instrumentId != null && instrumentEntity.shouldHandleMessage(maestro, message.channel, TransmitMode.PUBLIC.equals(message.transmitMode))) {
                packetList.add(new MidiNoteOnPacket(message.channel, message.note, message.velocity, instrumentId, target.getUniqueID(), target.getPosition()));
            }
        }
    }
    
    protected static void handleEntityInstrumentRelayNoteOff(ServerPlayerEntity target, UUID maestro, final MaestroNoteOffPacket message, List<MidiNoteOffPacket> packetList) {
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(target);

        if(instrumentEntity != null) { 
            Byte instrumentId = instrumentEntity.getInstrumentId();
            if(instrumentId != null && instrumentEntity.shouldHandleMessage(maestro, message.channel, TransmitMode.PUBLIC.equals(message.transmitMode))) {
                packetList.add(new MidiNoteOffPacket(message.channel, message.note, instrumentId, target.getUniqueID()));
            }
        }
    }


    // Item Stack Functions
    protected static void handleHeldInstrumentRelayNoteOn(ServerPlayerEntity target, UUID maestro, Hand handIn, final MaestroNoteOnPacket message, List<MidiNoteOnPacket> packetList) {
        ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(target, handIn);
        Byte instrumentId = ItemInstrument.getInstrumentId(stack);
        if(instrumentId != null && stack != null && ItemInstrument.shouldHandleMessage(stack, maestro, message.channel, TransmitMode.PUBLIC.equals(message.transmitMode))) {
            packetList.add(new MidiNoteOnPacket(message.channel, message.note, message.velocity, instrumentId, target.getUniqueID(), target.getPosition()));
        }
    }
    
    protected static void handleHeldInstrumentRelayNoteOff(ServerPlayerEntity target, UUID maestro, Hand handIn, final MaestroNoteOffPacket message, List<MidiNoteOffPacket> packetList) {
        ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(target, handIn);
        Byte instrumentId = ItemInstrument.getInstrumentId(stack);
        if(instrumentId != null && stack != null && ItemInstrument.shouldHandleMessage(stack, maestro, message.channel, TransmitMode.PUBLIC.equals(message.transmitMode))) {
            packetList.add(new MidiNoteOffPacket(message.channel, message.note, instrumentId, target.getUniqueID()));
        }
    }

    // Util
    protected static List<ServerPlayerEntity> getPotentialPlayers(TransmitMode transmitMode, ServerPlayerEntity sender) {
        List<ServerPlayerEntity> potentialPlayers = Arrays.asList(sender);

        if(transmitMode != TransmitMode.SELF) {
            BlockPos maestroPos = sender.getPosition();
            AxisAlignedBB queryBox = new AxisAlignedBB(maestroPos.getX() - 16, maestroPos.getY() - 16, maestroPos.getZ() - 16, 
                                                    maestroPos.getX() + 16, maestroPos.getY() + 16, maestroPos.getZ() + 16);
            potentialPlayers = sender.getServerWorld().getEntitiesWithinAABB(ServerPlayerEntity.class, queryBox, entity -> {
                return ItemInstrument.isEntityHoldingInstrument(entity) || BlockInstrument.isEntitySittingAtInstrument(entity);
            });
        }

        return potentialPlayers;
    }

    protected static List<TileReceiver> getPotentialReceivers(TransmitMode transmitMode, ServerPlayerEntity sender) {
        List<TileReceiver> potentialReceivers = new ArrayList<>();

        if(transmitMode != TransmitMode.SELF) {
            BlockPos maestroPos = sender.getPosition();
            AxisAlignedBB queryBox = new AxisAlignedBB(maestroPos.getX() - 16, maestroPos.getY() - 16, maestroPos.getZ() - 16, 
                                                    maestroPos.getX() + 16, maestroPos.getY() + 16, maestroPos.getZ() + 16);
            potentialReceivers = BlockPos.getAllInBox(queryBox).filter(pos -> ModBlocks.RECEIVER.equals(sender.getServerWorld().getBlockState(pos).getBlock()))
                .map(b -> ModBlocks.RECEIVER.getTileForBlock(sender.getServerWorld(), b)).filter(t -> t != null).collect(Collectors.toList());
        }

        return potentialReceivers;
    }

    protected static void sendOnPackets(HashMap<ServerPlayerEntity, List<MidiNoteOnPacket>> notePackets, Double range) {
        if(!notePackets.isEmpty()) {
            for(ServerPlayerEntity player : notePackets.keySet()) {
                List<MidiNoteOnPacket> playerPackets = notePackets.get(player);

                if(playerPackets != null && !playerPackets.isEmpty()) {
                    MidiNotePacketHandler.handleOnPacketsServer(notePackets.get(player), player, true);
                }
            }
        }
    }
    
    protected static void sendOffPackets(HashMap<ServerPlayerEntity, List<MidiNoteOffPacket>> notePackets, Double range) {
        if(!notePackets.isEmpty()) {
            for(ServerPlayerEntity player : notePackets.keySet()) {
                List<MidiNoteOffPacket> playerPackets = notePackets.get(player);

                if(playerPackets != null && !playerPackets.isEmpty()) {
                    MidiNotePacketHandler.handleOffPacketsServer(notePackets.get(player), player, true);
                }
            }
        }
    }
}
