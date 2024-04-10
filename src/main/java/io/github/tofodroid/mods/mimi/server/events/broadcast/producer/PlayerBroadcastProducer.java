package io.github.tofodroid.mods.mimi.server.events.broadcast.producer;

import io.github.tofodroid.mods.mimi.server.ServerExecutor;
import io.github.tofodroid.mods.mimi.server.midi.playlist.PlayerPlaylistHandler;
import io.github.tofodroid.mods.mimi.util.EntityUtils;
import net.minecraft.server.level.ServerPlayer;

public class PlayerBroadcastProducer extends ATransmitterBroadcastProducer {
    protected ServerPlayer player;

    public PlayerBroadcastProducer(ServerPlayer player) {
        super(player.getUUID(), new PlayerPlaylistHandler(player), player::getOnPos, () -> player.getLevel().dimension());
        this.player = player;
    }

    @Override
    protected Boolean isTransmitterStillValid() {
        if(this.player.isDeadOrDying()) {
            this.player = ServerExecutor.getServerPlayerById(this.player.getUUID());
        }

        if(this.player != null && this.player.isAddedToWorld() && !this.player.isDeadOrDying()) {
            return EntityUtils.playerHasActiveTransmitter(this.player);
        }

        return false;
    }
}
