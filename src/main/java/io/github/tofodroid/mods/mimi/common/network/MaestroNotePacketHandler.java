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
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

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
        BlockPos maestroPos = sender.getPosition();

        AxisAlignedBB queryBox = new AxisAlignedBB(maestroPos.getX() - 16, maestroPos.getY() - 16, maestroPos.getZ() - 16, 
                                                    maestroPos.getX() + 16, maestroPos.getY() + 16, maestroPos.getZ() + 16);
        List<ServerPlayerEntity> potentialPlayers = sender.getServerWorld().getEntitiesWithinAABB(ServerPlayerEntity.class, queryBox, entity -> {
            return ItemInstrument.isEntityHoldingInstrument(entity) || BlockInstrument.isEntitySittingAtInstrument(entity);
        });

        List<MidiNoteOnPacket> notePackets = new ArrayList<>();

        for(ServerPlayerEntity player : potentialPlayers) {
            // Held Instruments
            handleHeldInstrumentRelayNoteOn(player, sender.getUniqueID(), Hand.MAIN_HAND, message, notePackets);
            handleHeldInstrumentRelayNoteOn(player, sender.getUniqueID(), Hand.OFF_HAND, message, notePackets);

            // Seated Instrument
            handleEntityInstrumentRelayNoteOn(player, sender.getUniqueID(), message, notePackets);
        }

        if(!notePackets.isEmpty()) {
            PacketDistributor.PacketTarget target = PacketDistributor.NEAR.with(() -> 
                new PacketDistributor.TargetPoint(sender.getPosX(), sender.getPosY(), sender.getPosZ(), 68.0D, sender.getServerWorld().getDimensionKey())
            );

            for(MidiNoteOnPacket packet : notePackets) {
                NetworkManager.NET_CHANNEL.send(target, packet);
            }
        }
    }
    
    public static void handlePacketServer(final MaestroNoteOffPacket message, ServerPlayerEntity sender) {
        BlockPos maestroPos = sender.getPosition();

        AxisAlignedBB queryBox = new AxisAlignedBB(maestroPos.getX() - 16, maestroPos.getY() - 16, maestroPos.getZ() - 16, 
                                                    maestroPos.getX() + 16, maestroPos.getY() + 16, maestroPos.getZ() + 16);
        List<ServerPlayerEntity> potentialPlayers = sender.getServerWorld().getEntitiesWithinAABB(ServerPlayerEntity.class, queryBox, entity -> {
            return ItemInstrument.isEntityHoldingInstrument(entity) || BlockInstrument.isEntitySittingAtInstrument(entity);
        });

        List<MidiNoteOffPacket> notePackets = new ArrayList<>();

        for(ServerPlayerEntity player : potentialPlayers) {
            // Held Instruments
            handleHeldInstrumentRelayNoteOff(player, sender.getUniqueID(), Hand.MAIN_HAND, message, notePackets);
            handleHeldInstrumentRelayNoteOff(player, sender.getUniqueID(), Hand.OFF_HAND, message, notePackets);

            // Seated Instrument
            handleEntityInstrumentRelayNoteOff(player, sender.getUniqueID(), message, notePackets);
        }

        if(!notePackets.isEmpty()) {
            PacketDistributor.PacketTarget target = PacketDistributor.NEAR.with(() -> 
                new PacketDistributor.TargetPoint(sender.getPosX(), sender.getPosY(), sender.getPosZ(), 68.0D, sender.getServerWorld().getDimensionKey())
            );

            for(MidiNoteOffPacket packet : notePackets) {
                NetworkManager.NET_CHANNEL.send(target, packet);
            }
        }
    }
    
    // Tile Entity Functions
    protected static void handleEntityInstrumentRelayNoteOn(ServerPlayerEntity target, UUID maestro, final MaestroNoteOnPacket message, List<MidiNoteOnPacket> packetList) {
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(target);

        if(instrumentEntity != null) { 
            Byte instrumentId = EntityInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(instrumentEntity);
            if(instrumentId != null && EntityInstrumentDataUtil.INSTANCE.shouldHandleMessage(instrumentEntity, maestro, message.channel)) {
                packetList.add(new MidiNoteOnPacket(message.note, message.velocity, instrumentId, target.getUniqueID(), target.getPosition()));
            }
        }
    }
    
    protected static void handleEntityInstrumentRelayNoteOff(ServerPlayerEntity target, UUID maestro, final MaestroNoteOffPacket message, List<MidiNoteOffPacket> packetList) {
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(target);

        if(instrumentEntity != null) { 
            Byte instrumentId = EntityInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(instrumentEntity);
            if(instrumentId != null && EntityInstrumentDataUtil.INSTANCE.shouldHandleMessage(instrumentEntity, maestro, message.channel)) {
                packetList.add(new MidiNoteOffPacket(message.note, instrumentId, target.getUniqueID()));
            }
        }
    }


    // Item Stack Functions
    protected static void handleHeldInstrumentRelayNoteOn(ServerPlayerEntity target, UUID maestro, Hand handIn, final MaestroNoteOnPacket message, List<MidiNoteOnPacket> packetList) {
        ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(target, handIn);
        Byte instrumentId = ItemInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(stack);
        if(instrumentId != null && stack != null && ItemInstrumentDataUtil.INSTANCE.shouldHandleMessage(stack, maestro, message.channel)) {
            packetList.add(new MidiNoteOnPacket(message.note, message.velocity, instrumentId, target.getUniqueID(), target.getPosition()));
        }
    }
    
    protected static void handleHeldInstrumentRelayNoteOff(ServerPlayerEntity target, UUID maestro, Hand handIn, final MaestroNoteOffPacket message, List<MidiNoteOffPacket> packetList) {
        ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(target, handIn);
        Byte instrumentId = ItemInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(stack);
        if(instrumentId != null && stack != null && ItemInstrumentDataUtil.INSTANCE.shouldHandleMessage(stack, maestro, message.channel)) {
            packetList.add(new MidiNoteOffPacket(message.note, instrumentId, target.getUniqueID()));
        }
    }
}
