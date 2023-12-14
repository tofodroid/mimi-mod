package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sound.midi.Sequence;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.common.midi.IMidiFileProvider.LocalMidiInfo;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;
import net.minecraft.server.level.ServerPlayer;

public class ServerMusicTransmitter implements AutoCloseable {
    private UUID id;
    private Boolean loading = false;
    private Boolean loadFailed = false;
    private Boolean shouldPlayNextLoad = false;
    private MidiHandler midiHandler;
    private APlaylistHandler playlistHandler;
    
    public ServerMusicTransmitter(TileTransmitter tile) {
        this.id = tile.getUUID();
        this.playlistHandler = new TileTransmitterPlaylistHandler(tile);
        this.midiHandler = new MidiHandler(tile, this::onSongEnd);
        this.refreshSongs();

        if(this.playlistHandler.getSelectedSongInfo() != null) {
            this.loadSong(this.playlistHandler.getSelectedSongInfo());
        }
    }

    public ServerMusicTransmitter(ServerPlayer player) {
        this.id = player.getUUID();
        this.playlistHandler = new PlayerPlaylistHandler(player);
        this.midiHandler = new MidiHandler(player, this::onSongEnd);
        this.refreshSongs();

        if(this.playlistHandler.getSelectedSongInfo() != null) {
            this.loadSong(this.playlistHandler.getSelectedSongInfo());
        }
    }

    public void refreshSongs() {
        this.playlistHandler.refreshFilteredSongs();
    }

    public void play() {
        ServerMusicTransmitterManager.addPlaying(this.id);
        this.midiHandler.play();
    }

    public void pause() {
        ServerMusicTransmitterManager.removePlaying(this.id);
        this.midiHandler.pause();
    }

    public void stop() {
        ServerMusicTransmitterManager.removePlaying(this.id);
        this.midiHandler.stop();
    }

    public void seek() {
        // TODO ?
    }

    public void next() {
        this.playlistHandler.selectNextSong();
    }

    public void previous() {
        this.playlistHandler.selectPreviousSong();
    }
    
    public void onSongEnd() {
        switch(this.playlistHandler.getLoopMode()) {
            case ALL:
                this.next();
                this.shouldPlayNextLoad = true;
            case SINGLE:
                this.play();
            default:
                this.stop();
        }
    }

    public void loadSong(BasicMidiInfo info) {
        if(info != null && (this.midiHandler.getSequenceInfo() == null || !this.midiHandler.getSequenceInfo().fileId.toString().equals(info.fileId.toString()))) {
            this.startLoadSequence(info);
        } else if(info == null) {
            this.midiHandler.unloadSong();
        }
    }

    public void toggleSongFavorite() {
        this.playlistHandler.toggleSongFavorite();
    }

    public void cycleLoopMode() {
        this.playlistHandler.cycleLoopMode();
    }

    public void cycleFavoriteMode() {
        this.playlistHandler.cycleFavoriteMode();
    }

    public void cycleSourceMode() {
        this.playlistHandler.cycleSourceMode();
    }

    public void toggleShuffled() {
        this.playlistHandler.toggleShuffled();
    }

    public List<BasicMidiInfo> getCurrentSongsSorted() {
        return this.playlistHandler.getSortedFilteredSongs();
    }

    public List<Integer> getCurrentFavoriteIndicies() {
        List<BasicMidiInfo> infos = this.getCurrentSongsSorted();
        List<UUID> favorites = this.playlistHandler.getFavoriteSongs();
        List<Integer> result = new ArrayList<>();

        for(int i = 0; i < infos.size(); i++) {
            if(favorites.contains(infos.get(i).fileId)) {
                result.add(i);
            }
        }

        return result;
    }

    @Override
    public void close() {
        this.midiHandler.close();
    }

    public ServerMusicPlayerStatusPacket getStatus() {
        UUID currentId = this.midiHandler.getSequenceId();

        return new ServerMusicPlayerStatusPacket(
            this.id,
            currentId,
            this.playlistHandler.getSelectedDisplayIndex(),
            (currentId != null ? this.playlistHandler.getFavoriteSongs().contains(currentId) : false),
            this.midiHandler.getChannelMapping(),
            this.midiHandler.getSongLengthSeconds(),
            this.midiHandler.getPositionSeconds(),
            this.midiHandler.isPlaying(),
            this.loadFailed,
            this.loading,
            this.playlistHandler.getIsShuffled(),
            this.playlistHandler.getLoopMode(),
            this.playlistHandler.getFavoriteMode(),
            this.playlistHandler.getSourceMode()
        );
    }

    public void startLoadSequence(BasicMidiInfo info) {
        this.loading = true;
        this.loadFailed = false;

        if(info.serverMidi) {
            this.loading = false;
            LocalMidiInfo localInfo = MIMIMod.proxy.serverMidiFiles().getInfoById(info.fileId);

            if(localInfo != null) {
                Sequence sequence = localInfo.loadSequenceFromFile();

                if(sequence != null) {
                    this.finishLoadSequence(info, sequence);
                    return;
                }
            }

            // Failure if it gets here
            this.onSequenceLoadFailed(info);
        } else {
            this.midiHandler.unloadSong();
            ServerMusicTransmitterManager.startLoadSequence(this.id, this.playlistHandler.getClientSourceId(), info);
        }
    }

    public void onSequenceLoadFailed(BasicMidiInfo info) {
        if(info.fileId.toString().equals(this.playlistHandler.getSelectedSongId().toString())) {
            this.loading = false;
            this.loadFailed = true;
            this.shouldPlayNextLoad = false;
        }
    }

    public void finishLoadSequence(BasicMidiInfo info, Sequence sequence) {
        if(info.fileId.toString().equals(this.playlistHandler.getSelectedSongId().toString())) {
            this.loading = false;
            this.midiHandler.load(info, sequence);

            if(this.shouldPlayNextLoad) {
                this.play();
            }
        }
    }
}
