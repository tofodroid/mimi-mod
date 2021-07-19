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

import io.github.tofodroid.mods.mimi.common.network.MaestroNoteOnPacket.TransmitMode;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.data.EntityInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.data.ItemInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

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
            Byte instrumentId = EntityInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(instrumentEntity);
            if(instrumentId != null && EntityInstrumentDataUtil.INSTANCE.shouldHandleMessage(instrumentEntity, maestro, message.channel, message.transmitMode == TransmitMode.PUBLIC)) {
                packetList.add(new MidiNoteOnPacket(message.note, message.velocity, instrumentId, target.getUniqueID(), target.getPosition()));
            }
        }
    }
    
    protected static void handleEntityInstrumentRelayNoteOff(ServerPlayerEntity target, UUID maestro, final MaestroNoteOffPacket message, List<MidiNoteOffPacket> packetList) {
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(target);

        if(instrumentEntity != null) { 
            Byte instrumentId = EntityInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(instrumentEntity);
            if(instrumentId != null && EntityInstrumentDataUtil.INSTANCE.shouldHandleMessage(instrumentEntity, maestro, message.channel, message.transmitMode == TransmitMode.PUBLIC)) {
                packetList.add(new MidiNoteOffPacket(message.note, instrumentId, target.getUniqueID()));
            }
        }
    }


    // Item Stack Functions
    protected static void handleHeldInstrumentRelayNoteOn(ServerPlayerEntity target, UUID maestro, Hand handIn, final MaestroNoteOnPacket message, List<MidiNoteOnPacket> packetList) {
        ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(target, handIn);
        Byte instrumentId = ItemInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(stack);
        if(instrumentId != null && stack != null && ItemInstrumentDataUtil.INSTANCE.shouldHandleMessage(stack, maestro, message.channel, message.transmitMode == TransmitMode.PUBLIC)) {
            packetList.add(new MidiNoteOnPacket(message.note, message.velocity, instrumentId, target.getUniqueID(), target.getPosition()));
        }
    }
    
    protected static void handleHeldInstrumentRelayNoteOff(ServerPlayerEntity target, UUID maestro, Hand handIn, final MaestroNoteOffPacket message, List<MidiNoteOffPacket> packetList) {
        ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(target, handIn);
        Byte instrumentId = ItemInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(stack);
        if(instrumentId != null && stack != null && ItemInstrumentDataUtil.INSTANCE.shouldHandleMessage(stack, maestro, message.channel, message.transmitMode == TransmitMode.PUBLIC)) {
            packetList.add(new MidiNoteOffPacket(message.note, instrumentId, target.getUniqueID()));
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

    protected static void sendOnPackets(HashMap<ServerPlayerEntity, List<MidiNoteOnPacket>> notePackets, Double range) {
        if(!notePackets.isEmpty()) {
            for(ServerPlayerEntity player : notePackets.keySet()) {
                List<MidiNoteOnPacket> playerPackets = notePackets.get(player);

                if(playerPackets != null && !playerPackets.isEmpty()) {
                    MidiNotePacketHandler.handleOnPacketsServer(notePackets.get(player), player);
                }
            }
        }
    }
    
    protected static void sendOffPackets(HashMap<ServerPlayerEntity, List<MidiNoteOffPacket>> notePackets, Double range) {
        if(!notePackets.isEmpty()) {
            for(ServerPlayerEntity player : notePackets.keySet()) {
                List<MidiNoteOffPacket> playerPackets = notePackets.get(player);

                if(playerPackets != null && !playerPackets.isEmpty()) {
                    MidiNotePacketHandler.handleOffPacketsServer(notePackets.get(player), player);
                }
            }
        }
    }
}
