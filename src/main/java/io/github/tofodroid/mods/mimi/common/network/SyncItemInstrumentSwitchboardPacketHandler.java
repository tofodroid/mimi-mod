package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ContainerInstrument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class SyncItemInstrumentSwitchboardPacketHandler {
    public static void handlePacket(final SyncItemInstrumentSwitchboardPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client received unexpected SyncItemInstrumentSwitchboardPacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final SyncItemInstrumentSwitchboardPacket message, ServerPlayer sender) {
        if(sender.containerMenu != null) {
            if(sender.containerMenu instanceof ContainerInstrument) {
                ((ContainerInstrument)sender.containerMenu).saveToInventory(sender);
            }
        }
    }
}
