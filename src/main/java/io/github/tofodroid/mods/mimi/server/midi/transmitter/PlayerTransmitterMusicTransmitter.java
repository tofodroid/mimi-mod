package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import net.minecraft.server.level.ServerPlayer;

public class PlayerTransmitterMusicTransmitter extends AServerMusicTransmitter {
    public PlayerTransmitterMusicTransmitter(ServerPlayer player) {
        super(player.getUUID());
        this.midiHandler = new MidiHandler(player, this::onSongEnd);
        this.playlistHandler = new PlayerPlaylistHandler(player);
    }
}
