package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.forge.common.network.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public abstract class NetworkProxy {
    public static void sendToServer(Object message) {
        NetworkManager.sendToServer(message);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        NetworkManager.sendToPlayer(message, player);
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        NetworkManager.sendToPlayer(message, player);
    }

    public static void sendToPlayersInRange(Object message, BlockPos sourcePos, ServerLevel worldIn, ServerPlayer excludePlayer, Double range) {
        NetworkManager.sendToPlayersInRange(message, sourcePos, worldIn, excludePlayer, range);
    }
}
