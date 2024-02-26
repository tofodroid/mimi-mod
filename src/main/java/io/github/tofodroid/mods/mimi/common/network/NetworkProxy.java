package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.forge.common.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public abstract class NetworkProxy {
    public static void sendToServer(Object message) {
        if(message != null) {
            NetworkManager.sendToServer(message);
        }
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        if(message != null) {
            NetworkManager.sendToPlayer(message, player);
        }
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        if(message != null) {
            NetworkManager.sendToPlayer(message, player);
        }
    }

    public static void sendToPlayersInRange(Object message, BlockPos sourcePos, ServerLevel worldIn, ServerPlayer excludePlayer, Double range) {
        if(message != null) {
            NetworkManager.sendToPlayersInRange(message, sourcePos, worldIn, excludePlayer, range);
        }
    }
}
