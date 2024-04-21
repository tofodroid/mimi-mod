package io.github.tofodroid.mods.mimi.server;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.neoforge.server.ServerAccessor;
import net.minecraft.server.level.ServerPlayer;

public abstract class ServerExecutorProxy {
    public static void executeOnServerThread(Runnable toRun) {
        ServerAccessor.getCurrentServer().execute(toRun);
    }

    public static ServerPlayer getServerPlayerById(UUID clientId) {
        return ServerAccessor.getCurrentServer().getPlayerList().getPlayer(clientId);
    }
}
