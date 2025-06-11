package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.server.level.ServerPlayer;

public class MIMIConfigPacketHandler {
    public static void handlePacketServer(final MIMIConfigPacket message, ServerPlayer sender) {
        MIMIMod.LOGGER.warn("Server received unexpected MIMIConfigPacket!");
    }

    public static void handlePacketClient(final MIMIConfigPacket message) {
        if(MIMIMod.getProxy().isClient()) {
            ClientGuiWrapper.openConfigGui();
        }
    }
}
