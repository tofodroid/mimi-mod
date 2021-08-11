package io.github.tofodroid.mods.mimi.common.network;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.tile.TileListener;

public class MidiNotePacketHandler {
    public static void handlePacket(final MidiNotePacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketsServer(Arrays.asList(message),ctx.get().getSender().getServerWorld(), ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }
    
    protected static void handlePacketsServer(final List<MidiNotePacket> messages, ServerWorld worldIn, ServerPlayerEntity sender) {
        if(messages != null && !messages.isEmpty()) {
            // Forward to players
            for(MidiNotePacket packet : messages) {
                NetworkManager.NET_CHANNEL.send(getPacketTarget(packet.pos, worldIn, sender, getQueryBoxRange(packet.velocity <= 0)), packet);
            }

            // Process Redstone
            List<EntityNoteResponsiveTile> entities = new ArrayList<>();
            BlockPos lastPacketPos = null;

            for(MidiNotePacket packet : messages) {
                if(packet.velocity > 0) {
                    if(lastPacketPos != packet.pos) {  
                        lastPacketPos = packet.pos;
                        entities = getPotentialEntities(worldIn, packet.pos, getQueryBoxRange(false).intValue());
                    }
                    
                    getPotentialListeners(entities).forEach(listener -> {
                        if(listener.shouldAcceptNote(packet.note, packet.instrumentId)) {
                            ModBlocks.LISTENER.powerTarget(worldIn, worldIn.getBlockState(listener.getPos()), 15, listener.getPos());
                        }
                    });
                }
            }
        }
    }

    public static void handlePacketClient(final MidiNotePacket message) {
        MIMIMod.proxy.getMidiSynth().handlePacket(message); 
    }

    protected static List<EntityNoteResponsiveTile> getPotentialEntities(ServerWorld worldIn, BlockPos notePos, Integer range) {
        List<EntityNoteResponsiveTile> potentialEntites = new ArrayList<>();

        AxisAlignedBB queryBox = new AxisAlignedBB(notePos.getX() - range, notePos.getY() - range, notePos.getZ() - range, 
                                                    notePos.getX() + range, notePos.getY() + range, notePos.getZ() + range);
        potentialEntites = worldIn.getEntitiesWithinAABB(EntityNoteResponsiveTile.class, queryBox, entity -> {
            return entity.getTile() != null;
        });

        return potentialEntites;
    }

    protected static List<TileListener> getPotentialListeners(List<EntityNoteResponsiveTile> entities) {
        return entities.stream().filter(e -> e.getTile() instanceof TileListener).map(e -> (TileListener)e.getTile()).collect(Collectors.toList());
    }
    
    protected static PacketDistributor.PacketTarget getPacketTarget(BlockPos targetPos, ServerWorld worldIn, ServerPlayerEntity excludePlayer, Double range) {
        return PacketDistributor.NEAR.with(() -> {
            if(excludePlayer == null) {
                return new PacketDistributor.TargetPoint(targetPos.getX(), targetPos.getY(), targetPos.getZ(), range, worldIn.getDimensionKey());
            } else {
                return new PacketDistributor.TargetPoint(excludePlayer, targetPos.getX(), targetPos.getY(), targetPos.getZ(), range, worldIn.getDimensionKey());
            }
        });
    }

    protected static Double getQueryBoxRange(Boolean off) {
        return off ? 64d : 48d;
    }
}
