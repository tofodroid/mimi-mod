package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.util.TimeUtils;
import net.minecraft.server.level.ServerPlayer;

public class ServerTimeSyncPacketHandler {
    public static Long lastHandleMillis = TimeUtils.getNowTime();

    public static void handlePacketClient(final ServerTimeSyncPacket message) {
        if(MIMIMod.getProxy().isClient()) {
            ((ClientProxy)MIMIMod.getProxy()).handleTimeSyncPacket(message);
        }
    }
    
    public static void handlePacketServer(final ServerTimeSyncPacket message, ServerPlayer sender) {
        NetworkProxy.sendToPlayer(sender, new ServerTimeSyncPacket(MIMIMod.getProxy().getCurrentServerMillis(), message.firstRequest));
    }
}
