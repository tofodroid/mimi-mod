package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.forge.common.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public abstract class NetworkProxy {
    public static <T extends CustomPacketPayload> void sendToServer(T message) {
        if(message != null) {
            NetworkManager.sendToServer(message);
        }
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(T message, ServerPlayer player) {
        if(message != null) {
            NetworkManager.sendToPlayer(message, player);
        }
    }

    public static <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T message) {
        if(message != null) {
            NetworkManager.sendToPlayer(message, player);
        }
    }

    public static <T extends CustomPacketPayload> void sendToPlayersInRange(T message, BlockPos sourcePos, ServerLevel worldIn, ServerPlayer excludePlayer, Double range) {
        if(message != null) {
            NetworkManager.sendToPlayersInRange(message, sourcePos, worldIn, excludePlayer, range);
        }
    }
}
