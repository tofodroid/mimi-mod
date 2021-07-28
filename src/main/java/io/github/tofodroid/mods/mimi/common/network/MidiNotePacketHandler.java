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

public class MidiNotePacketHandler {
    public static void handlePacket(final MidiNoteOnPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handleOnPacketsServer(Arrays.asList(message), ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handleOnPacketClient(message, ctx.get().getSender()));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacket(final MidiNoteOffPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handleOffPacketsServer(Arrays.asList(message), ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handleOffPacketClient(message, ctx.get().getSender()));
        }
        
        ctx.get().setPacketHandled(true);
    }

    public static void handleOnPacketsServer(final List<MidiNoteOnPacket> messages, ServerPlayerEntity sender) {
        if(messages != null && !messages.isEmpty()) {
            // Forward to players
            PacketDistributor.PacketTarget target = PacketDistributor.NEAR.with(() -> 
                new PacketDistributor.TargetPoint(sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ(), 64.0D, sender.getServerWorld().getDimensionKey())
            );

            // Check redstone listeners
            AxisAlignedBB queryBox = new AxisAlignedBB(sender.getPosition().getX() - 16, sender.getPosition().getY() - 16, sender.getPosition().getZ() - 16, 
                                                    sender.getPosition().getX() + 16, sender.getPosition().getY() + 16, sender.getPosition().getZ() + 16);

            BlockPos.getAllInBox(queryBox).filter(pos -> ModBlocks.LISTENER.equals(sender.getServerWorld().getBlockState(pos).getBlock())).forEach(pos -> {
                ModBlocks.LISTENER.powerTarget(sender.getServerWorld(), sender.getServerWorld().getBlockState(pos), 15, pos, 0);
            });

            for(MidiNoteOnPacket packet : messages) {
                NetworkManager.NET_CHANNEL.send(target, packet);
            }
        }
    }
    
    public static void handleOffPacketsServer(final List<MidiNoteOffPacket> messages, ServerPlayerEntity sender) {
        if(messages != null && !messages.isEmpty()) {
            PacketDistributor.PacketTarget target = PacketDistributor.NEAR.with(() -> 
                new PacketDistributor.TargetPoint(sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ(), 64.0D, sender.getServerWorld().getDimensionKey())
            );

            for(MidiNoteOffPacket packet : messages) {
                NetworkManager.NET_CHANNEL.send(target, packet);
            }
        }
    }

    @SuppressWarnings("resource")
    public static void handleOnPacketClient(final MidiNoteOnPacket message, ServerPlayerEntity sender) {
        MIMIMod.proxy.getMidiSynth().handleNoteOn(message);

        if(Minecraft.getInstance().currentScreen instanceof GuiInstrumentContainerScreen && shouldShowOnGUI(message.player, message.channel)) {
            ((GuiInstrumentContainerScreen)Minecraft.getInstance().currentScreen).onMidiNoteOn(message.channel, message.note, message.velocity);
        }
    }
    
    @SuppressWarnings("resource")
    public static void handleOffPacketClient(final MidiNoteOffPacket message, ServerPlayerEntity sender) {
        MIMIMod.proxy.getMidiSynth().handleNoteOff(message);

        if(Minecraft.getInstance().currentScreen instanceof GuiInstrumentContainerScreen &&  shouldShowOnGUI(message.player, message.channel)) {
            ((GuiInstrumentContainerScreen)Minecraft.getInstance().currentScreen).onMidiNoteOff(message.channel, message.note);
        }
    }

    @SuppressWarnings("resource")
    public static Boolean shouldShowOnGUI(UUID messagePlayer, Byte channel) {
        ClientPlayerEntity thisPlayer = Minecraft.getInstance().player;

        if(messagePlayer.equals(thisPlayer.getUniqueID()) && thisPlayer.openContainer instanceof ContainerInstrument) {
            ItemStack guiSwitch = ((ContainerInstrument)thisPlayer.openContainer).getSelectedSwitchboard();

            if(channel.equals(MidiNoteOnPacket.NO_CHANNEL) || (ModItems.SWITCHBOARD.equals(guiSwitch.getItem()) && ItemMidiSwitchboard.isChannelEnabled(guiSwitch, channel)) && 
                ( ItemMidiSwitchboard.getMidiSource(guiSwitch).equals(messagePlayer) 
                    || ItemMidiSwitchboard.getMidiSource(guiSwitch).equals(ItemMidiSwitchboard.PUBLIC_SOURCE_ID)
                )
            ) {
                return true;
            }
        }

        return false;
    }
}
