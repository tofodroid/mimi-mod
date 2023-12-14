package io.github.tofodroid.mods.mimi.common.network;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.tile.TileListener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import net.minecraft.world.level.gameevent.GameEvent;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MidiNotePacketHandler {
    private static final ConcurrentHashMap<BlockPos, List<EntityNoteResponsiveTile>> ENTITY_CACHE_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<BlockPos, List<ServerPlayer>> PLAYER_CACHE_MAP = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.SERVER) {
            return;
        }
        
        ENTITY_CACHE_MAP.clear();
        PLAYER_CACHE_MAP.clear();
    }

    public static void handlePacket(final MidiNotePacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message,(ServerLevel)ctx.get().getSender().level(), ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final MidiNotePacket message, ServerLevel worldIn, ServerPlayer sender) {
        if(message != null) {
            // Forward to nearby players
            List<ServerPlayer> potentialPlayers = PLAYER_CACHE_MAP.computeIfAbsent(
                message.pos,
                (key) -> getPotentialPlayers(worldIn, message.pos, getQueryBoxRange(message.velocity <= 0))
            );
            potentialPlayers.forEach(player -> {
                if(player != sender) {
                    NetworkManager.NOTE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
                }
            });

            // Process Listeners and Sculk
            if(!message.isControlPacket() && message.velocity > 0) {
                List<EntityNoteResponsiveTile> potentialEntities = ENTITY_CACHE_MAP.computeIfAbsent(
                    message.pos,
                    (key) -> {
                        List<EntityNoteResponsiveTile> newEntities = getPotentialEntities(worldIn, message.pos, getQueryBoxRange(false));
                        worldIn.gameEvent(GameEvent.INSTRUMENT_PLAY, message.pos, GameEvent.Context.of(worldIn.getBlockState(message.pos)));
                        return newEntities;
                    }
                );
                
                for(TileListener listener : filterToListeners(potentialEntities)) {
                    if(listener.shouldTriggerFromMidiEvent(null, message.note, message.velocity, message.instrumentId)) {
                        listener.onTrigger(null, message.note, message.velocity, message.instrumentId);
                    }
                };
            }
        }
    }

    public static void handlePacketClient(final MidiNotePacket message) {
        if(MIMIMod.proxy.isClient()) ((ClientProxy)MIMIMod.proxy).getMidiSynth().handlePacket(message); 
    }

    protected static List<ServerPlayer> getPotentialPlayers(ServerLevel worldIn, BlockPos notePos, Integer range) {
        List<ServerPlayer> potentialEntites = new ArrayList<>();

        AABB queryBox = new AABB(notePos.getX() - range, notePos.getY() - range, notePos.getZ() - range, 
                                                    notePos.getX() + range, notePos.getY() + range, notePos.getZ() + range);
        potentialEntites = worldIn.getEntitiesOfClass(ServerPlayer.class, queryBox, entity -> {
            return entity.isAlive();
        });

        return potentialEntites;
    }

    protected static List<EntityNoteResponsiveTile> getPotentialEntities(ServerLevel worldIn, BlockPos notePos, Integer range) {
        List<EntityNoteResponsiveTile> potentialEntites = new ArrayList<>();

        AABB queryBox = new AABB(notePos.getX() - range, notePos.getY() - range, notePos.getZ() - range, 
                                                    notePos.getX() + range, notePos.getY() + range, notePos.getZ() + range);
        potentialEntites = worldIn.getEntitiesOfClass(EntityNoteResponsiveTile.class, queryBox, entity -> {
            return entity.getTile() != null;
        });

        return potentialEntites;
    }

    protected static List<TileListener> filterToListeners(List<EntityNoteResponsiveTile> entities) {
        return entities.stream().filter(e -> e.getTile() instanceof TileListener).map(e -> (TileListener)e.getTile()).collect(Collectors.toList());
    }
    
    protected static PacketDistributor.PacketTarget getPacketTarget(BlockPos targetPos, ServerLevel worldIn, ServerPlayer excludePlayer, Double range) {
        return PacketDistributor.NEAR.with(() -> {
            if(excludePlayer == null) {
                return new PacketDistributor.TargetPoint(targetPos.getX(), targetPos.getY(), targetPos.getZ(), range, worldIn.dimension());
            } else {
                return new PacketDistributor.TargetPoint(excludePlayer, targetPos.getX(), targetPos.getY(), targetPos.getZ(), range, worldIn.dimension());
            }
        });
    }

    protected static Integer getQueryBoxRange(Boolean off) {
        return off ? 128 : 64;
    }
}
