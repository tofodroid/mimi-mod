package io.github.tofodroid.mods.mimi.common.network;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.network.MaestroNoteOnPacket.TransmitMode;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.instruments.EntityInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.instruments.ItemInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

public class MaestroNotePacketHandler {
    public static void handlePacket(final MaestroNoteOnPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        }

        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacket(final MaestroNoteOffPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        }
        
        ctx.get().setPacketHandled(true);
    }

    public static void handlePacketServer(final MaestroNoteOnPacket message, ServerPlayerEntity sender) {
        List<MidiNoteOnPacket> notePackets = new ArrayList<>();

        for(ServerPlayerEntity player : getPotentialPlayers(message.transmitMode, sender)) {
            // Held Instruments
            handleHeldInstrumentRelayNoteOn(player, sender.getUniqueID(), Hand.MAIN_HAND, message, notePackets);
            handleHeldInstrumentRelayNoteOn(player, sender.getUniqueID(), Hand.OFF_HAND, message, notePackets);

            // Seated Instrument
            handleEntityInstrumentRelayNoteOn(player, sender.getUniqueID(), message, notePackets);
        }

        sendPackets(notePackets, sender, 64.0d);
    }
    
    public static void handlePacketServer(final MaestroNoteOffPacket message, ServerPlayerEntity sender) {
        List<MidiNoteOffPacket> notePackets = new ArrayList<>();

        for(ServerPlayerEntity player : getPotentialPlayers(message.transmitMode, sender)) {
            // Held Instruments
            handleHeldInstrumentRelayNoteOff(player, sender.getUniqueID(), Hand.MAIN_HAND, message, notePackets);
            handleHeldInstrumentRelayNoteOff(player, sender.getUniqueID(), Hand.OFF_HAND, message, notePackets);

            // Seated Instrument
            handleEntityInstrumentRelayNoteOff(player, sender.getUniqueID(), message, notePackets);
        }

        sendPackets(notePackets, sender, 96.0d);
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

    protected static void sendPackets(List<? extends Object> notePackets, ServerPlayerEntity sender, Double range) {
        if(!notePackets.isEmpty()) {
            PacketDistributor.PacketTarget target = PacketDistributor.NEAR.with(() -> 
                new PacketDistributor.TargetPoint(sender.getPosX(), sender.getPosY(), sender.getPosZ(), range, sender.getServerWorld().getDimensionKey())
            );

            for(Object packet : notePackets) {
                NetworkManager.NET_CHANNEL.send(target, packet);
            }
        }
    }
}
