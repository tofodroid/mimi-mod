package io.github.tofodroid.mods.mimi.common.network;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;

public class TransmitterNotePacketHandler {
    public static void handlePacket(final TransmitterNotePacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        }

        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final TransmitterNotePacket message, ServerPlayerEntity sender) {
        Instant startTime = Instant.now();
        HashMap<ServerPlayerEntity, List<MidiNotePacket>> notePackets = new HashMap<>();
        notePackets.put(sender, new ArrayList<>());

        // Handle Players
        for(ServerPlayerEntity player : getPotentialPlayers(message.transmitMode, sender, getQueryBoxRange(message.velocity <= 0))) {
            List<MidiNotePacket> playerPackets = new ArrayList<>();
            
            // Held Instruments
            handleHeldInstrumentRelayNote(player, sender.getUniqueID(), Hand.MAIN_HAND, message, playerPackets);
            handleHeldInstrumentRelayNote(player, sender.getUniqueID(), Hand.OFF_HAND, message, playerPackets);

            // Seated Instrument
            handleEntityInstrumentRelayNote(player, sender.getUniqueID(), message, playerPackets);

            notePackets.put(player, playerPackets);
        }

        // Handle Mechanical Maestros
        for(TileMechanicalMaestro maestro : getPotentialMechMaestros(getPotentialEntities(message.transmitMode, sender, getQueryBoxRange(message.velocity <= 0)))) {
            if(maestro.shouldHandleMessage(sender.getUniqueID(), message.channel, message.note, message.transmitMode == TransmitMode.PUBLIC)) {
                notePackets.get(sender).add(new MidiNotePacket(message.channel, message.note, message.velocity, maestro.getInstrumentId(), maestro.getMaestroUUID(), true, maestro.getPos()));
            }
        }

        sendPlayerOnPackets(notePackets);

        // Handle Receivers
        if(message.velocity > 0) {
            for(TileReceiver receiver : getPotentialReceivers(getPotentialEntities(message.transmitMode, sender, getQueryBoxRange(false)))) {
                if(receiver.shouldHandleMessage(sender.getUniqueID(), message.channel, message.note, message.transmitMode == TransmitMode.PUBLIC)) {
                    ModBlocks.RECEIVER.powerTarget(sender.getServerWorld(), receiver.getBlockState(), 15, receiver.getPos());
                }
            }
        }

        // DEBUG
        Long millis = ChronoUnit.MILLIS.between(startTime, Instant.now());
        if(millis > 1) {
            MIMIMod.LOGGER.warn("Processing transmitter packet set took " + millis + "ms");
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
    protected static List<ServerPlayerEntity> getPotentialPlayers(TransmitMode transmitMode, ServerPlayerEntity sender, Integer range) {
        List<ServerPlayerEntity> potentialPlayers = Arrays.asList(sender);

        if(transmitMode != TransmitMode.SELF) {
            BlockPos senderPos = sender.getPosition();
            AxisAlignedBB queryBox = new AxisAlignedBB(senderPos.getX() - range, senderPos.getY() - range, senderPos.getZ() - range, 
                                                    senderPos.getX() + range, senderPos.getY() + range, senderPos.getZ() + range);
            potentialPlayers = sender.getServerWorld().getEntitiesWithinAABB(ServerPlayerEntity.class, queryBox, entity -> {
                return ItemInstrument.isEntityHoldingInstrument(entity) || BlockInstrument.isEntitySittingAtInstrument(entity);
            });
        }

        return potentialPlayers;
    }

    protected static List<EntityNoteResponsiveTile> getPotentialEntities(TransmitMode transmitMode, ServerPlayerEntity sender, Integer range) {
        List<EntityNoteResponsiveTile> potentialEntites = new ArrayList<>();

        if(transmitMode != TransmitMode.SELF) {
            BlockPos senderPos = sender.getPosition();
            AxisAlignedBB queryBox = new AxisAlignedBB(senderPos.getX() - range, senderPos.getY() - range, senderPos.getZ() - range, 
                                                    senderPos.getX() + range, senderPos.getY() + range, senderPos.getZ() + range);
            potentialEntites = sender.getServerWorld().getEntitiesWithinAABB(EntityNoteResponsiveTile.class, queryBox, entity -> {
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

    protected static Integer getQueryBoxRange(Boolean off) {
        return off ? 32 : 16;
    }
}
