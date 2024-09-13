package io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter;

import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;
import io.github.tofodroid.mods.mimi.server.ServerExecutorProxy;
import io.github.tofodroid.mods.mimi.server.midi.playlist.TileTransmitterPlaylistHandler;

public class TileTransmitterBroadcastProducer extends ATransmitterBroadcastProducer {
    protected TileTransmitter tile;

    public TileTransmitterBroadcastProducer(TileTransmitter tile) {
        super(tile.getUUID(), new TileTransmitterPlaylistHandler(tile), tile::getBlockPos, () -> tile.getLevel().dimension());
        this.tile = tile;
    }

    @Override
    public void play() {
        super.play();
        ServerExecutorProxy.executeOnServerThread(() -> tile.setPowered());
    }

    @Override
    public void pause() {
        super.pause();
        ServerExecutorProxy.executeOnServerThread(() -> tile.setUnpowered());
    }

    @Override
    public void stop() {
        super.stop();
        ServerExecutorProxy.executeOnServerThread(() -> tile.setUnpowered());
    }

    @Override
    protected Boolean isTransmitterStillValid() {
        return true;
    }

    @Override
    public void onProducerRemoved() {
        // No-op
    }

    @Override
    public Integer getBroadcastRange() {
        return 16;
    }
}
