package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ContainerInstrument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncItemInstrumentSwitchboardPacketHandler {
    public static void handlePacket(final SyncItemInstrumentSwitchboardPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client recevied unexpected SyncItemInstrumentSwitchboardPacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final SyncItemInstrumentSwitchboardPacket message, ServerPlayerEntity sender) {
        if(sender.openContainer != null) {
            if(sender.openContainer instanceof ContainerInstrument) {
                ((ContainerInstrument)sender.openContainer).saveToInventory(sender);
            }
        }
    }
}
