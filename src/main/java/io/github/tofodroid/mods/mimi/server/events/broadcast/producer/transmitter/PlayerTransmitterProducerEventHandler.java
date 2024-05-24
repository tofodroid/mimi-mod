package io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter;

import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class PlayerTransmitterProducerEventHandler {
    public static void onPlayerLoggedIn(ServerPlayer player) {
        ServerTransmitterManager.createTransmitter(player);
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        if(player.level() instanceof ServerLevel) {
            BroadcastManager.removeBroadcastProducer(player.getUUID());
        }
    }
}
