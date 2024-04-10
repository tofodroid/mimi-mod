package io.github.tofodroid.mods.mimi.server.events.broadcast;

import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.ServerTransmitterManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class BroadcastManagerProducerEventHooks {
    public static void onPlayerLoggedIn(ServerPlayer player) {
        ServerTransmitterManager.createTransmitter(player);
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        if(player.getLevel() instanceof ServerLevel) {
            BroadcastManager.removeBroadcastProducer(player.getUUID());
        }
    }
}
