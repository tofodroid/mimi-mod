package io.github.tofodroid.mods.mimi.forge.server;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

public abstract class ServerAccessor {
    public static MinecraftServer getCurrentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
