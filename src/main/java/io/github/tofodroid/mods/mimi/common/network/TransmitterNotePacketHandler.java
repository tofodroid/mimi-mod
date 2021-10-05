package io.github.tofodroid.mods.mimi.common.network;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender().getPosition(), ctx.get().getSender().getServerWorld(),  ctx.get().getSender().getUniqueID(), ctx.get().getSender()));
        }

        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final TransmitterNotePacket message, BlockPos sourcePos, ServerWorld worldIn, UUID senderId, ServerPlayerEntity sender) {
        HashMap<UUID, List<MidiNotePacket>> notePackets = new HashMap<>();

        // Handle Players
        for(ServerPlayerEntity player : getPotentialPlayers(message.transmitMode, sourcePos, worldIn, sender, getQueryBoxRange(message.velocity <= 0))) {
            List<MidiNotePacket> playerPackets = new ArrayList<>();
            
            // Held Instruments
            handleHeldInstrumentRelayNote(player, senderId, Hand.MAIN_HAND, message, playerPackets);
            handleHeldInstrumentRelayNote(player, senderId, Hand.OFF_HAND, message, playerPackets);

            // Seated Instrument
            handleEntityInstrumentRelayNote(player, senderId, message, playerPackets);

            notePackets.put(player.getUniqueID(), playerPackets);
        }

        // Handle Mechanical Maestros
        UUID mechUUID = new UUID(0,0);
        notePackets.put(mechUUID, new ArrayList<>());
        for(TileMechanicalMaestro maestro : getPotentialMechMaestros(getPotentialEntities(message.transmitMode, sourcePos, worldIn, getQueryBoxRange(message.velocity <= 0)))) {
            if(maestro.shouldHandleMessage(senderId, message.channel, message.note, message.transmitMode == TransmitMode.PUBLIC)) {
                notePackets.get(mechUUID).add(new MidiNotePacket(message.note, ItemMidiSwitchboard.applyVolume(maestro.getSwitchboardStack(), message.velocity), maestro.getInstrumentId(), maestro.getMaestroUUID(), true, maestro.getPos()));
            }
        }

        sendPlayerOnPackets(notePackets, worldIn);

        // Handle Receivers
        if(message.velocity > 0) {
            for(TileReceiver receiver : getPotentialReceivers(getPotentialEntities(message.transmitMode, sourcePos, worldIn, getQueryBoxRange(false)))) {
                if(receiver.shouldHandleMessage(senderId, message.channel, message.note, message.transmitMode == TransmitMode.PUBLIC)) {
                    ModBlocks.RECEIVER.powerTarget(worldIn, receiver.getBlockState(), 15, receiver.getPos());
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
                packetList.add(new MidiNotePacket(message.note, ItemMidiSwitchboard.applyVolume(instrumentEntity.getSwitchboardStack(), message.velocity), instrumentId, target.getUniqueID(), false, target.getPosition()));
            }
        }
    }

    // Item Stack Functions
    protected static void handleHeldInstrumentRelayNote(ServerPlayerEntity target, UUID sourceId, Hand handIn, final TransmitterNotePacket message, List<MidiNotePacket> packetList) {
        ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(target, handIn);
        Byte instrumentId = ItemInstrument.getInstrumentId(stack);
        if(instrumentId != null && stack != null && ItemInstrument.shouldHandleMessage(stack, sourceId, message.channel, TransmitMode.PUBLIC.equals(message.transmitMode))) {
            packetList.add(new MidiNotePacket(message.note, ItemMidiSwitchboard.applyVolume(ItemInstrument.getSwitchboardStack(stack), message.velocity), instrumentId, target.getUniqueID(), false, target.getPosition()));
        }
    }
    
    // Util
    protected static List<ServerPlayerEntity> getPotentialPlayers(TransmitMode transmitMode, BlockPos sourcePos, ServerWorld worldIn, ServerPlayerEntity sender, Integer range) {
        List<ServerPlayerEntity> potentialPlayers = new ArrayList<>();

        if(transmitMode != TransmitMode.SELF) {
            AxisAlignedBB queryBox = new AxisAlignedBB(sourcePos.getX() - range, sourcePos.getY() - range, sourcePos.getZ() - range, 
                                                sourcePos.getX() + range, sourcePos.getY() + range, sourcePos.getZ() + range);
            potentialPlayers = worldIn.getEntitiesWithinAABB(ServerPlayerEntity.class, queryBox, entity -> {
                return ItemInstrument.isEntityHoldingInstrument(entity) || BlockInstrument.isEntitySittingAtInstrument(entity);
            });
        } else if(sender != null) {
            potentialPlayers.add(sender);
        }

        return potentialPlayers;
    }

    protected static List<EntityNoteResponsiveTile> getPotentialEntities(TransmitMode transmitMode, BlockPos sourcePos, ServerWorld worldIn, Integer range) {
        List<EntityNoteResponsiveTile> potentialEntites = new ArrayList<>();
        
        if(transmitMode != TransmitMode.SELF) {
            AxisAlignedBB queryBox = new AxisAlignedBB(sourcePos.getX() - range, sourcePos.getY() - range, sourcePos.getZ() - range, 
                                                        sourcePos.getX() + range, sourcePos.getY() + range, sourcePos.getZ() + range);
            potentialEntites = worldIn.getEntitiesWithinAABB(EntityNoteResponsiveTile.class, queryBox, entity -> {
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

    protected static void sendPlayerOnPackets(HashMap<UUID, List<MidiNotePacket>> notePackets, ServerWorld worldIn) {
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
