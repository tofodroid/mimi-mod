package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.client.gui.GuiBroadcasterContainerScreen;
import io.github.tofodroid.mods.mimi.common.container.ContainerBroadcaster;
import io.github.tofodroid.mods.mimi.common.tile.TileBroadcaster;
import io.github.tofodroid.mods.mimi.server.midi.MusicPlayerMidiHandler;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class ServerMidiInfoPacketHandler {
    public static void handlePacket(final ServerMidiInfoPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final ServerMidiInfoPacket message, ServerPlayer sender) {
        ServerMidiInfoPacket returnPacket = new ServerMidiInfoPacket(
            ServerMidiInfoPacket.STATUS_CODE.EMPTY,
            null,
            null, 
            null,
            null
        );

        if(sender.containerMenu != null && sender.containerMenu instanceof ContainerBroadcaster) {
            TileBroadcaster playerTile = ((ContainerBroadcaster)sender.containerMenu).getBroadcasterTile();
            MusicPlayerMidiHandler tileHandler = ServerMusicPlayerMidiManager.getMusicPlayer(playerTile);

            if(playerTile != null && tileHandler != null) {
                if(!tileHandler.isInError()) {
                    returnPacket = new ServerMidiInfoPacket(
                        ServerMidiInfoPacket.STATUS_CODE.INFO,
                        tileHandler.getMidiFileInfo().byteChannelMapping, 
                        tileHandler.getMidiFileInfo().songLength, 
                        tileHandler.getPositionSeconds(),
                        tileHandler.isPlaying()
                    );
                } else {
                    returnPacket = new ServerMidiInfoPacket(
                        tileHandler.getErrorStatus(),
                        null, 
                        null, 
                        null,
                        null
                    );
                }
            }
        }

        NetworkManager.NET_CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), returnPacket);
    }
    
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("resource")
    public static void handlePacketClient(final ServerMidiInfoPacket message) {
        if(Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof GuiBroadcasterContainerScreen) {
            ((GuiBroadcasterContainerScreen)Minecraft.getInstance().screen).handleMidiInfoPacket(message);
        }
    }
}
