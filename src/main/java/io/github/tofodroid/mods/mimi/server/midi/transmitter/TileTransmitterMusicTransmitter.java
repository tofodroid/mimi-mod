package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;

public class TileTransmitterMusicTransmitter extends AServerMusicTransmitter {
    protected TileTransmitter tile;

    public TileTransmitterMusicTransmitter(TileTransmitter tile) {
        super(tile.getUUID());
        this.midiHandler = new MidiHandler(tile, this::onSongEnd);
        this.playlistHandler = new TileTransmitterPlaylistHandler(tile);
        this.tile = tile;
    }

    @Override
    public void play() {
        super.play();
        tile.setPowered();
    }

    @Override
    public void pause() {
        super.pause();
        tile.setUnpowered();
    }

    @Override
    public void stop() {
        super.stop();
        tile.setUnpowered();
    }
}
