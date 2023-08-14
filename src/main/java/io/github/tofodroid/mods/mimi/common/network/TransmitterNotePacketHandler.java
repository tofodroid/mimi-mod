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

import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentHandheld;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;

public class TransmitterNotePacketHandler {
    public static void handlePacket(final TransmitterNotePacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender().getOnPos(), (ServerLevel)ctx.get().getSender().level(), ctx.get().getSender().getUUID()));
        }

        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final TransmitterNotePacket message, BlockPos sourcePos, ServerLevel worldIn, UUID senderId) {
        HashMap<UUID, List<MidiNotePacket>> notePackets = new HashMap<>();

        // Override senderId for public
        if(message.pub) {
            senderId = InstrumentDataUtils.PUBLIC_SOURCE_ID;
        }

        // Handle Players
        for(ServerPlayer player : getPotentialPlayers(sourcePos, worldIn, getQueryBoxRange(message.velocity <= 0))) {
            List<MidiNotePacket> playerPackets = new ArrayList<>();
            
            // Held Instruments
            handleHeldInstrumentRelayNote(player, senderId, InteractionHand.MAIN_HAND, message, playerPackets);
            handleHeldInstrumentRelayNote(player, senderId, InteractionHand.OFF_HAND, message, playerPackets);

            // Seated Instrument
            handleEntityInstrumentRelayNote(player, senderId, message, playerPackets);

            notePackets.put(player.getUUID(), playerPackets);
        }

        List<EntityNoteResponsiveTile> potentialNoteEntities = getPotentialEntities(sourcePos, worldIn, getQueryBoxRange(false));

        // Handle Mechanical Maestros
        for(TileMechanicalMaestro maestro : filterToMechanicalMaestros(potentialNoteEntities)) {
            maestro.onMidiEvent(senderId, message.channel, message.note, message.velocity, null);
        }

        sendPlayerOnPackets(notePackets, worldIn);

        // Handle Receivers
        if(!message.isControlPacket() && message.velocity > 0) {
            for(TileReceiver receiver : filterToReceivers(potentialNoteEntities) ){
                receiver.onMidiEvent(senderId, message.channel, message.note, message.velocity, null);
            }
        }
    }
    
    // Tile Entity Functions
    protected static void handleEntityInstrumentRelayNote(ServerPlayer target, UUID sourceId, final TransmitterNotePacket message, List<MidiNotePacket> packetList) {
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(target);

        if(instrumentEntity != null) { 
            Byte instrumentId = instrumentEntity.getInstrumentId();
            if(instrumentId != null && InstrumentDataUtils.shouldInstrumentRespondToMessage(instrumentEntity.getInstrumentStack(), sourceId, message.channel)) {
                if(message.isControlPacket()) {
                    packetList.add(MidiNotePacket.createControlPacket(message.getControllerNumber(), message.getControllerValue(), instrumentId, target.getUUID(), target.getOnPos(), message.noteServerTime));
                } else {
                    packetList.add(MidiNotePacket.createNotePacket(message.note, InstrumentDataUtils.applyVolume(instrumentEntity.getInstrumentStack(), message.velocity), instrumentId, target.getUUID(), target.getOnPos(), message.noteServerTime));
                }
            }
        }
    }
    
    protected static List<TileMechanicalMaestro> filterToMechanicalMaestros(List<EntityNoteResponsiveTile> entities) {
        return entities.stream().filter(e -> e.getTile() instanceof TileMechanicalMaestro).map(e -> (TileMechanicalMaestro)e.getTile()).collect(Collectors.toList());
    }

    protected static List<TileReceiver> filterToReceivers(List<EntityNoteResponsiveTile> entities) {
        return entities.stream().filter(e -> e.getTile() instanceof TileReceiver).map(e -> (TileReceiver)e.getTile()).collect(Collectors.toList());
    }

    // Item Stack Functions
    protected static void handleHeldInstrumentRelayNote(ServerPlayer target, UUID sourceId, InteractionHand handIn, final TransmitterNotePacket message, List<MidiNotePacket> packetList) {
        ItemStack stack = ItemInstrumentHandheld.getEntityHeldInstrumentStack(target, handIn);
        if(stack != null && InstrumentDataUtils.shouldInstrumentRespondToMessage(stack, sourceId, message.channel)) {
            if(message.isControlPacket()) {
                packetList.add(MidiNotePacket.createControlPacket(message.getControllerNumber(), message.getControllerValue(), InstrumentDataUtils.getInstrumentId(stack), target.getUUID(), target.getOnPos(), message.noteServerTime));
            } else {
                packetList.add(MidiNotePacket.createNotePacket(message.note, InstrumentDataUtils.applyVolume(stack, message.velocity), InstrumentDataUtils.getInstrumentId(stack), target.getUUID(), target.getOnPos(), message.noteServerTime));
            }
        }
    }
    
    // Util
    protected static List<ServerPlayer> getPotentialPlayers(BlockPos sourcePos, ServerLevel worldIn, Integer range) {
        List<ServerPlayer> potentialPlayers = new ArrayList<>();

        AABB queryBox = new AABB(sourcePos.getX() - range, sourcePos.getY() - range, sourcePos.getZ() - range, 
                                            sourcePos.getX() + range, sourcePos.getY() + range, sourcePos.getZ() + range);
        potentialPlayers = worldIn.getEntitiesOfClass(ServerPlayer.class, queryBox, entity -> {
            return ItemInstrumentHandheld.isEntityHoldingInstrument(entity) || BlockInstrument.isEntitySittingAtInstrument(entity);
        });

        return potentialPlayers;
    }

    protected static List<EntityNoteResponsiveTile> getPotentialEntities(BlockPos sourcePos, ServerLevel worldIn, Integer range) {
        List<EntityNoteResponsiveTile> potentialEntites = new ArrayList<>();
        
        AABB queryBox = new AABB(sourcePos.getX() - range, sourcePos.getY() - range, sourcePos.getZ() - range, 
                                                    sourcePos.getX() + range, sourcePos.getY() + range, sourcePos.getZ() + range);
        potentialEntites = worldIn.getEntitiesOfClass(EntityNoteResponsiveTile.class, queryBox, entity -> {
            return entity.getTile() != null;
        });

        return potentialEntites;
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
