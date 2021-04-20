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

public class SpeakerNotePacketHandler {
    public static void handlePacket(final SpeakerNoteOnPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        }

        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacket(final SpeakerNoteOffPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        }
        
        ctx.get().setPacketHandled(true);
    }

    public static void handlePacketServer(final SpeakerNoteOnPacket message, ServerPlayerEntity sender) {
        BlockPos maestroPos = sender.getServerWorld().getPlayerByUuid(message.maestro).getPosition();

        AxisAlignedBB queryBox = new AxisAlignedBB(maestroPos.getX() - 16, maestroPos.getY() - 16, maestroPos.getZ() - 16, 
                                                    maestroPos.getX() + 16, maestroPos.getY() + 16, maestroPos.getZ() + 16);
        List<ServerPlayerEntity> potentialPlayers = sender.getServerWorld().getEntitiesWithinAABB(ServerPlayerEntity.class, queryBox, entity -> {
            return ItemInstrument.isEntityHoldingInstrument(entity) || BlockInstrument.isEntitySittingAtInstrument(entity);
        });

        List<MidiNoteOnPacket> notePackets = new ArrayList<>();

        for(ServerPlayerEntity player : potentialPlayers) {
            // Held Instruments
            handleHeldInstrumentRelayNoteOn(player, Hand.MAIN_HAND, message, notePackets);
            handleHeldInstrumentRelayNoteOn(player, Hand.OFF_HAND, message, notePackets);

            // Seateed Instrument
            handleEntityInstrumentRelayNoteOn(player, message, notePackets);
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
    
    public static void handlePacketServer(final SpeakerNoteOffPacket message, ServerPlayerEntity sender) {
        BlockPos maestroPos = sender.getServerWorld().getPlayerByUuid(message.maestro).getPosition();

        AxisAlignedBB queryBox = new AxisAlignedBB(maestroPos.getX() - 16, maestroPos.getY() - 16, maestroPos.getZ() - 16, 
                                                    maestroPos.getX() + 16, maestroPos.getY() + 16, maestroPos.getZ() + 16);
        List<ServerPlayerEntity> potentialPlayers = sender.getServerWorld().getEntitiesWithinAABB(ServerPlayerEntity.class, queryBox, entity -> {
            return ItemInstrument.isEntityHoldingInstrument(entity) || BlockInstrument.isEntitySittingAtInstrument(entity);
        });

        List<MidiNoteOffPacket> notePackets = new ArrayList<>();

        for(ServerPlayerEntity player : potentialPlayers) {
            // Held Instruments
            handleHeldInstrumentRelayNoteOff(player, Hand.MAIN_HAND, message, notePackets);
            handleHeldInstrumentRelayNoteOff(player, Hand.OFF_HAND, message, notePackets);

            // Seateed Instrument
            handleEntityInstrumentRelayNoteOff(player, message, notePackets);
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
    protected static Boolean instrumentEntityShouldHandleMessage(UUID sender, TileInstrument instrumentEntity, Byte channel) {
        return EntityInstrumentDataUtil.INSTANCE.isMidiEnabled(instrumentEntity) && sender.equals(EntityInstrumentDataUtil.INSTANCE.getLinkedMaestro(instrumentEntity)) && EntityInstrumentDataUtil.INSTANCE.doesAcceptChannel(instrumentEntity, channel);
    }

    protected static void handleEntityInstrumentRelayNoteOn(ServerPlayerEntity player, final SpeakerNoteOnPacket message, List<MidiNoteOnPacket> packetList) {
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(player);

        if(instrumentEntity != null) { 
            Byte instrumentId = EntityInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(instrumentEntity);
            if(instrumentId != null && instrumentEntityShouldHandleMessage(message.maestro, instrumentEntity, message.channel)) {
                packetList.add(new MidiNoteOnPacket(message.note, message.velocity, instrumentId, player.getUniqueID(), player.getPosition()));
            }
        }
    }
    
    protected static void handleEntityInstrumentRelayNoteOff(ServerPlayerEntity player, final SpeakerNoteOffPacket message, List<MidiNoteOffPacket> packetList) {
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(player);

        if(instrumentEntity != null) { 
            Byte instrumentId = EntityInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(instrumentEntity);
            if(instrumentId != null && instrumentEntityShouldHandleMessage(message.maestro, instrumentEntity, message.channel)) {
                packetList.add(new MidiNoteOffPacket(message.note, instrumentId, player.getUniqueID()));
            }
        }
    }


    // Item Stack Functions
    protected static Boolean instrumentStackShouldHandleMessage(UUID sender, ItemStack instrumentStack, Byte channel) {
        return ItemInstrumentDataUtil.INSTANCE.isMidiEnabled(instrumentStack) && sender.equals(ItemInstrumentDataUtil.INSTANCE.getLinkedMaestro(instrumentStack)) && ItemInstrumentDataUtil.INSTANCE.doesAcceptChannel(instrumentStack, channel);
    }

    protected static void handleHeldInstrumentRelayNoteOn(ServerPlayerEntity player, Hand handIn, final SpeakerNoteOnPacket message, List<MidiNoteOnPacket> packetList) {
        ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(player, handIn);
        Byte instrumentId = ItemInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(stack);
        if(instrumentId != null && stack != null && instrumentStackShouldHandleMessage(message.maestro, stack, message.channel)) {
            packetList.add(new MidiNoteOnPacket(message.note, message.velocity, instrumentId, player.getUniqueID(), player.getPosition()));
        }
    }
    
    protected static void handleHeldInstrumentRelayNoteOff(ServerPlayerEntity player, Hand handIn, final SpeakerNoteOffPacket message, List<MidiNoteOffPacket> packetList) {
        ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(player, handIn);
        Byte instrumentId = ItemInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(stack);
        if(instrumentId != null && stack != null && instrumentStackShouldHandleMessage(message.maestro, stack, message.channel)) {
            packetList.add(new MidiNoteOffPacket(message.note, instrumentId, player.getUniqueID()));
        }
    }
}
