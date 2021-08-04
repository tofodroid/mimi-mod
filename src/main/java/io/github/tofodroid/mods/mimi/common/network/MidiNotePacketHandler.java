package io.github.tofodroid.mods.mimi.common.network;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.UUID;
import java.util.Arrays;
import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.client.gui.GuiInstrumentContainerScreen;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.container.ContainerInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.TileAdvListener;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;

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
                NetworkManager.NET_CHANNEL.send(getPacketTarget(packet.pos, worldIn, sender), packet);
            }

            // Process Redstone
            for(MidiNotePacket packet : messages) {
                if(packet.velocity > 0) {
                    AxisAlignedBB queryBox = new AxisAlignedBB(packet.pos.getX() - 16, packet.pos.getY() - 16, packet.pos.getZ() - 16, 
                    packet.pos.getX() + 16, packet.pos.getY() + 16, packet.pos.getZ() + 16);

                    // Check Listeners
                    BlockPos.getAllInBox(queryBox).filter(pos -> ModBlocks.LISTENER.equals(worldIn.getBlockState(pos).getBlock())).forEach(pos -> {
                        ModBlocks.LISTENER.powerTarget(worldIn, worldIn.getBlockState(pos), 15, pos);
                    });

                    // Check ADV Listeners
                    BlockPos.getAllInBox(queryBox).map(pos -> {TileEntity tile = worldIn.getTileEntity(pos); return tile != null && ModTiles.ADVLISTENER.equals(tile.getType()) ? (TileAdvListener)tile : null;}).forEach(tile -> {
                        if(tile != null && messages.stream().anyMatch(message -> tile.shouldAcceptNote(message.note, message.instrumentId))) {
                            ModBlocks.ADVLISTENER.powerTarget(worldIn, worldIn.getBlockState(tile.getPos()), 15, tile.getPos());
                        }
                    });
                }
            }
        }
    }

    @SuppressWarnings("resource")
    public static void handlePacketClient(final MidiNotePacket message) {
        MIMIMod.proxy.getMidiSynth().handlePacket(message);

        if(Minecraft.getInstance().currentScreen instanceof GuiInstrumentContainerScreen && shouldShowOnGUI(message.player, message.channel, message.instrumentId)) {
            ((GuiInstrumentContainerScreen)Minecraft.getInstance().currentScreen).onMidiNoteOn(message.channel, message.note, message.velocity);
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
    
    protected static PacketDistributor.PacketTarget getPacketTarget(BlockPos targetPos, ServerWorld worldIn, ServerPlayerEntity excludePlayer) {
        return PacketDistributor.NEAR.with(() -> {
            if(excludePlayer == null) {
                return new PacketDistributor.TargetPoint(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 64.0D, worldIn.getDimensionKey());
            } else {
                return new PacketDistributor.TargetPoint(excludePlayer, targetPos.getX(), targetPos.getY(), targetPos.getZ(), 64.0D, worldIn.getDimensionKey());
            }
        });
    }
}
