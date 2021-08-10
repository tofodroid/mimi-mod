package io.github.tofodroid.mods.mimi.common.network;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.UUID;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.client.gui.GuiInstrumentContainerScreen;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.container.ContainerInstrument;
import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.TileAdvListener;
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
            Instant startTime = Instant.now();

            // Forward to players
            for(MidiNotePacket packet : messages) {
                NetworkManager.NET_CHANNEL.send(getPacketTarget(packet.pos, worldIn, sender, getQueryBoxRange(packet.velocity <= 0)), packet);
            }

            // Process Redstone
            for(MidiNotePacket packet : messages) {
                if(packet.velocity > 0) {
                    List<EntityNoteResponsiveTile> entities = getPotentialEntities(worldIn, packet.pos, getQueryBoxRange(false).intValue());
                    
                    getPotentialListeners(entities).forEach(listener -> {
                        ModBlocks.LISTENER.powerTarget(worldIn, worldIn.getBlockState(listener.getPos()), 15, listener.getPos());
                    });

                    getPotentialAdvListeners(entities).forEach(listener -> {
                        if(listener.shouldAcceptNote(packet.note, packet.instrumentId)) {
                            ModBlocks.ADVLISTENER.powerTarget(worldIn, worldIn.getBlockState(listener.getPos()), 15, listener.getPos());
                        }
                    });
                }
            }
            
            // DEBUG
            Long millis = ChronoUnit.MILLIS.between(startTime, Instant.now());
            if(millis > 1) {
                MIMIMod.LOGGER.warn("Processing onfmidinote packet set took " + millis + "ms");
            }
        }
    }

    @SuppressWarnings("resource")
    public static void handlePacketClient(final MidiNotePacket message) {
        MIMIMod.proxy.getMidiSynth().handlePacket(message);

        if(Minecraft.getInstance().currentScreen instanceof GuiInstrumentContainerScreen && shouldShowOnGUI(message.player, message.channel, message.instrumentId)) {
            if(message.velocity > 0) {
                ((GuiInstrumentContainerScreen)Minecraft.getInstance().currentScreen).onMidiNoteOn(message.channel, message.note, message.velocity);
            } else {
                ((GuiInstrumentContainerScreen)Minecraft.getInstance().currentScreen).onMidiNoteOff(message.channel, message.note);
            }
        }
    }

    @SuppressWarnings("resource")
    public static Boolean shouldShowOnGUI(UUID messagePlayer, Byte channel, Byte instrument) {
        ClientPlayerEntity thisPlayer = Minecraft.getInstance().player;
    
        if(messagePlayer.equals(thisPlayer.getUniqueID()) && thisPlayer.openContainer instanceof ContainerInstrument) {
            ItemStack switchStack = ((ContainerInstrument)thisPlayer.openContainer).getSelectedSwitchboard();
            Byte guiInstrument = ((ContainerInstrument)thisPlayer.openContainer).getInstrumentId();

            if(instrument == guiInstrument && ModItems.SWITCHBOARD.equals(switchStack.getItem())) {
                UUID midiSource = ItemMidiSwitchboard.getMidiSource(switchStack);
                
                if((messagePlayer.equals(midiSource) || ItemMidiSwitchboard.PUBLIC_SOURCE_ID.equals(midiSource)) && ItemMidiSwitchboard.isChannelEnabled(switchStack, channel)) {
                    return true;
                }             
            }
        }

        return false;
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

    protected static List<TileAdvListener> getPotentialAdvListeners(List<EntityNoteResponsiveTile> entities) {
        return entities.stream().filter(e -> e.getTile() instanceof TileAdvListener).map(e -> (TileAdvListener)e.getTile()).collect(Collectors.toList());
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
