package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class ServerTimeSyncPacketHandler {
    public static void handlePacket(final ServerTimeSyncPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }

    public static void handlePacketClient(final ServerTimeSyncPacket message) {
        if(MIMIMod.proxy.isClient()) ((ClientProxy)MIMIMod.proxy).getMidiSynth().handlePacket(message); 
    }
    
    public static void handlePacketServer(final ServerTimeSyncPacket message, ServerPlayer sender) {
        NetworkManager.INFO_CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new ServerTimeSyncPacket(message.clientTime, Util.getEpochMillis() - message.clientTime));
    }
}
