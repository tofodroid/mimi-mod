package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.client.network.ClientMidiUploadManager;
import io.github.tofodroid.mods.mimi.server.network.ServerMidiUploadManager;
import net.minecraft.server.level.ServerPlayer;

public class ServerMidiUploadPacketHandler {
    public static void handlePacketClient(final ServerMidiUploadPacket message) {
        ClientMidiUploadManager.handlePacket(message);
    }

    public static void handlePacketServer(final ServerMidiUploadPacket message, ServerPlayer sender) {
        ServerMidiUploadManager.handlePacket(message, sender);
    }
}
