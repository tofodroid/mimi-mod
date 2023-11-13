package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.client.gui.GuiTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayer;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerManager;
import net.minecraft.client.Minecraft;
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
    
    public static void handlePacketServer(ServerMusicPlayerStatusPacket request, ServerPlayer sender) {
        ServerMusicPlayer player = ServerMusicPlayerManager.getMusicPlayer(request.musicPlayerId);
        
        if(player != null) {
            NetworkManager.INFO_CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), player.getStatus());
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings({"resource", "null"})
    public static void handlePacketClient(final ServerMusicPlayerStatusPacket message) {
        if(Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof GuiTransmitter) {
            ((GuiTransmitter)Minecraft.getInstance().screen).handleMusicPlayerStatusPacket(message);
        }
    }
}
