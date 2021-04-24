package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ContainerDiskRecorder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class WriteDiskDataPacketHandler {
    public static void handlePacket(final WriteDiskDataPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client recevied unexpected WriteDiskDataPacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final WriteDiskDataPacket message, ServerPlayerEntity sender) {
        if(sender.openContainer != null && sender.openContainer instanceof ContainerDiskRecorder) {
            ((ContainerDiskRecorder)sender.openContainer).writeDisk(message.title, message.url, sender.getDisplayName().getString());
        }
    }
}
