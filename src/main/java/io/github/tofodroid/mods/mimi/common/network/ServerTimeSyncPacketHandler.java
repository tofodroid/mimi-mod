package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class ServerTimeSyncPacketHandler {
    public static Long lastHandleMillis = Util.getEpochMillis();

    public static void handlePacket(final ServerTimeSyncPacket message, Supplier<NetworkEvent.Context> ctx) {
        lastHandleMillis = Util.getEpochMillis();
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }

    public static void handlePacketClient(final ServerTimeSyncPacket message) {
        if(MIMIMod.proxy.isClient()) {
            ((ClientProxy)MIMIMod.proxy).handleTimeSyncPacket(message);
        }
    }
    
    public static void handlePacketServer(final ServerTimeSyncPacket message, ServerPlayer sender) {
        NetworkProxy.sendToPlayer(sender, new ServerTimeSyncPacket(MIMIMod.proxy.getCurrentServerMillis(), message.firstRequest));
    }
}
