package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ContainerBroadcaster;
import io.github.tofodroid.mods.mimi.common.container.ContainerTransmitter;
import io.github.tofodroid.mods.mimi.common.item.ItemFloppyDisk;
import io.github.tofodroid.mods.mimi.common.item.ItemTransmitter;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import io.github.tofodroid.mods.mimi.common.tile.TileBroadcaster;
import io.github.tofodroid.mods.mimi.server.midi.MusicPlayerMidiHandler;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

public class BroadcastControlPacketHandler {
    public static void handlePacket(final BroadcastControlPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client received unexpected BroadcastControlPacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final BroadcastControlPacket message, ServerPlayer sender) {
        if(sender.containerMenu != null) {
            if(sender.containerMenu instanceof ContainerBroadcaster) {
                handleBroadcaster(message, sender);
            } else if(sender.containerMenu instanceof ContainerTransmitter) {
                handleTransmitter(message, sender);
            }
        }
    }

    protected static void handleBroadcaster(final BroadcastControlPacket message, ServerPlayer sender) {
        ContainerBroadcaster container = (ContainerBroadcaster)sender.containerMenu;
        TileBroadcaster tile = container.getBroadcasterTile();

        if(tile != null) {
            switch(message.control) {
                case PAUSE:
                    tile.pauseMusic();
                    break;
                case PLAY:
                    tile.playMusic();
                    break;
                case STOP:
                    tile.stopMusic();
                    break;
                case TOGGLE_PUBLIC:
                    TransmitMode oldMode = tile.getTransmitMode();
                    tile.togglePublicBroadcast();
                    sendAllNotesOff(oldMode, tile.getBlockPos(), (ServerLevel)tile.getLevel(), tile.getMusicPlayerId(), sender);
                    break;
                default:
                    break;

            }
        }
    }

    protected static void handleTransmitter(final BroadcastControlPacket message, ServerPlayer sender) {
        ContainerTransmitter container = (ContainerTransmitter)sender.containerMenu;
        ItemStack stack = container.getTransmitterStack();
        Pair<Integer,MusicPlayerMidiHandler> handler = ServerMusicPlayerMidiManager.getTransmitter(sender.getUUID());

        if(stack != null && !stack.isEmpty()) {
            switch(message.control) {
                case PAUSE:
                    if(handler != null && handler.getLeft().equals(container.playerInvSlot)) {
                        ServerMusicPlayerMidiManager.pauseTransmitter(sender.getUUID());
                    }
                    break;
                case PLAY:
                    if(handler != null && handler.getLeft().equals(container.playerInvSlot)) {
                        ServerMusicPlayerMidiManager.playTransmitter(sender.getUUID());
                        NetworkManager.INFO_CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> sender),
                            new ActiveTransmitterIdPacket(ItemTransmitter.getTransmitId(ServerMusicPlayerMidiManager.getTransmitterStack(sender.getUUID())))
                        );
                    } else if(ItemTransmitter.hasActiveFloppyDisk(stack)) {
                        if(ServerMusicPlayerMidiManager.createTransmitter(sender, container.playerInvSlot, ItemFloppyDisk.getMidiUrl(ItemTransmitter.getActiveFloppyDiskStack(stack)))) {
                            ServerMusicPlayerMidiManager.playTransmitter(sender.getUUID());
                            NetworkManager.INFO_CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> sender),
                                new ActiveTransmitterIdPacket(ItemTransmitter.getTransmitId(ServerMusicPlayerMidiManager.getTransmitterStack(sender.getUUID())))
                            );
                        }
                    }
                    break;
                case STOP:
                    if(handler != null && handler.getLeft().equals(container.playerInvSlot)) {
                        ServerMusicPlayerMidiManager.stopTransmitter(sender.getUUID());
                    }
                    break;
                case TOGGLE_PUBLIC:
                    TransmitMode oldMode = container.getTransmitMode();
                    container.toggleTransmitMode();
                    container.saveToInventory(sender);
                    container.sendAllDataToRemote();
                    ServerMusicPlayerMidiManager.forceUpdateTransmitterStack(sender.getUUID(), container.getTransmitterStack());
                    sendAllNotesOff(oldMode, sender.getOnPos(), (ServerLevel)sender.level(), sender.getUUID(), sender);
                    break;
                default:
                    break;

            }
        }
    }

    protected static void sendAllNotesOff(TransmitMode mode, BlockPos pos, ServerLevel level, UUID uuid, ServerPlayer sender) {
        TransmitterNotePacket offPacket = TransmitterNotePacket.createAllNotesOffPacket(TransmitterNotePacket.ALL_CHANNELS, mode);
        ServerLifecycleHooks.getCurrentServer().execute(() -> {
            TransmitterNotePacketHandler.handlePacketServer(offPacket, pos, level, uuid, sender);
        });
    }
}
