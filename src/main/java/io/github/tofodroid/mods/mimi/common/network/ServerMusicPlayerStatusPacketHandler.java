package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.client.gui.GuiBroadcasterContainerScreen;
import io.github.tofodroid.mods.mimi.client.gui.GuiTransmitterContainerScreen;
import io.github.tofodroid.mods.mimi.common.container.ContainerBroadcaster;
import io.github.tofodroid.mods.mimi.common.container.ContainerTransmitter;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiStatus.STATUS_CODE;
import io.github.tofodroid.mods.mimi.common.tile.TileBroadcaster;
import io.github.tofodroid.mods.mimi.server.midi.MusicPlayerMidiHandler;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class ServerMusicPlayerStatusPacketHandler {
    public static void handlePacket(final ServerMusicPlayerStatusPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final ServerMusicPlayerStatusPacket message, ServerPlayer sender) {
        ServerMusicPlayerStatusPacket returnPacket = new ServerMusicPlayerStatusPacket(
            STATUS_CODE.EMPTY,
            null,
            null
        );

        if(sender.containerMenu != null && sender.containerMenu instanceof ContainerBroadcaster) {
            TileBroadcaster playerTile = ((ContainerBroadcaster)sender.containerMenu).getBroadcasterTile();
            MusicPlayerMidiHandler tileHandler = ServerMusicPlayerMidiManager.getBroadcaster(playerTile.getMusicPlayerId());

            if(playerTile != null && tileHandler != null) {
                returnPacket = new ServerMusicPlayerStatusPacket(
                    STATUS_CODE.SUCCESS,
                    tileHandler.getPositionSeconds(),
                    tileHandler.isPlaying()
                );
            }
        } else if (sender.containerMenu != null && sender.containerMenu instanceof ContainerTransmitter) {
            Pair<Integer,MusicPlayerMidiHandler> playerHandler = ServerMusicPlayerMidiManager.getTransmitter(sender.getUUID());
            
            if(playerHandler != null && playerHandler.getLeft().equals(((ContainerTransmitter)sender.containerMenu).playerInvSlot)) {
                returnPacket = new ServerMusicPlayerStatusPacket(
                    STATUS_CODE.SUCCESS,
                    playerHandler.getRight().getPositionSeconds(),
                    playerHandler.getRight().isPlaying()
                );
            }
        }

        NetworkManager.INFO_CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), returnPacket);
    }
    
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("resource")
    public static void handlePacketClient(final ServerMusicPlayerStatusPacket message) {
        Screen screen = Minecraft.getInstance().screen;
        if(screen != null && screen instanceof GuiBroadcasterContainerScreen) {
            ((GuiBroadcasterContainerScreen)screen).handlePlayerStatusPacket(message);
        } else if(screen != null && screen instanceof GuiTransmitterContainerScreen) {
            ((GuiTransmitterContainerScreen)screen).handlePlayerStatusPacket(message);
        }
    }
}
