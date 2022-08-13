package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ContainerBroadcaster;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class BroadcasterControlPacketHandler {
    public static void handlePacket(final BroadcasterControlPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client received unexpected BroadcasterControlPacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final BroadcasterControlPacket message, ServerPlayer sender) {
        if(sender.containerMenu != null) {
            if(sender.containerMenu instanceof ContainerBroadcaster) {
                ContainerBroadcaster container = (ContainerBroadcaster)sender.containerMenu;

                switch(message.control) {
                    case PAUSE:
                        container.getBroadcasterTile().pauseMusic();
                        break;
                    case PLAY:
                        container.getBroadcasterTile().playMusic();
                        break;
                    case STOP:
                        container.getBroadcasterTile().stopMusic();
                        break;
                    case TOGGLE_PUBLIC:
                        container.getBroadcasterTile().togglePublicBroadcast();
                        break;
                    default:
                        break;

                }
            }
        }
    }
}
