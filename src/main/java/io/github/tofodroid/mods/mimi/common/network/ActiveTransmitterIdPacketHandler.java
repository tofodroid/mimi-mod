package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class ActiveTransmitterIdPacketHandler {
    public static void handlePacket(final ActiveTransmitterIdPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            MIMIMod.LOGGER.warn("Server received unexpected ActiveTransmitterIdPacket!");
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketClient(final ActiveTransmitterIdPacket message) {
        ((ClientProxy)MIMIMod.proxy).getMidiInput().setActiveTransmitterIdCache(message.activeTransmitterId);
    }
}
