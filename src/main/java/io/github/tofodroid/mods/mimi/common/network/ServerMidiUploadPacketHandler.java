package io.github.tofodroid.mods.mimi.common.network;

import java.util.function.Supplier;

import org.apache.commons.lang3.RandomUtils;

import io.github.tofodroid.mods.mimi.client.network.ClientMidiUploadManager;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.network.ServerMidiUploadManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class ServerMidiUploadPacketHandler {

    public static void handlePacket(final ServerMidiUploadPacket message, Supplier<NetworkEvent.Context> ctx) {
        if(ctx.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            ctx.get().enqueueWork(() -> handlePacketServer(message, ctx.get().getSender()));
        } else {
            ctx.get().enqueueWork(() -> handlePacketClient(message));
        }
        ctx.get().setPacketHandled(true);
    }

    public static void handlePacketClient(final ServerMidiUploadPacket message) {
        ClientMidiUploadManager.handlePacket(message);
    }

    public static void handlePacketServer(final ServerMidiUploadPacket message, ServerPlayer sender) {
        if(RandomUtils.nextInt() % 3 != 4) {
            ServerMidiUploadManager.handlePacket(message, sender);
        } else {
            MIMIMod.LOGGER.info("Debug skip packet: " + message.fileId.toString() + "_" + message.part);
        }
    }
}
