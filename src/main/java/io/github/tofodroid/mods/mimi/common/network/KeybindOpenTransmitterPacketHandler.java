package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class KeybindOpenTransmitterPacketHandler {
    public static void handlePacket(final KeybindOpenTransmitterPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            MIMIMod.LOGGER.warn("Client received unexpected KeybindOpenTransmitterPacket!");
        }
        ctx.get().setPacketHandled(true);
    }
    
    public static void handlePacketServer(final KeybindOpenTransmitterPacket message, ServerPlayer sender) {
        if(ServerMusicPlayerMidiManager.getTransmitter(sender.getUUID()) != null) {
            Integer invSlot = ServerMusicPlayerMidiManager.getTransmitter(sender.getUUID()).getLeft();
            NetworkHooks.openGui(sender, ModItems.TRANSMITTER.generateContainerProvider(
                invSlot
            ), buffer -> {
                buffer.writeInt(invSlot);
            });
        }
    }
}
