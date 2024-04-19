package io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.sound.midi.Sequence;

import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.common.midi.LocalMidiInfo;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.ABroadcastProducer;
import io.github.tofodroid.mods.mimi.server.midi.ServerMidiManager;
import io.github.tofodroid.mods.mimi.server.midi.playlist.APlaylistHandler;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMidiSequencer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class ATransmitterBroadcastProducer extends ABroadcastProducer {
    protected Boolean loading = false;
    protected Boolean loadFailed = false;
    protected Boolean shouldPlayNextLoad = false;
    private ServerMidiSequencer midiHandler;
    protected APlaylistHandler playlistHandler;

    public ATransmitterBroadcastProducer(UUID id, APlaylistHandler playlistHandler, Supplier<BlockPos> blockPos, Supplier<ResourceKey<Level>> dimension) {
        super(id, blockPos, dimension);
        this.playlistHandler = playlistHandler;
        this.midiHandler = new ServerMidiSequencer(this::broadcast, this::onSongEnd);
    }

    public void onLoad() {
        this.refreshSongs();

        if(this.playlistHandler.getFilterHasSongs()) {
            this.playlistHandler.selectDisplaySong(0);

            if(this.playlistHandler.getSelectedSongInfo() != null) {
                this.loadSong(this.playlistHandler.getSelectedSongInfo());
            }
        }
    }

    public void refreshSongs() {
        // If refresh can't find current song and currently playing set load failed
        Boolean wasPlaying = this.midiHandler.isPlaying();

        if(!this.playlistHandler.refreshFilteredSongs()) {
            if(wasPlaying) {
                this.loadFailed = true;
            } else {
                this.playlistHandler.selectNextSong();
            }
        }
    }

    public void play() {
        this.midiHandler.play();
    }

    public void pause() {
        this.midiHandler.pause();
        this.allNotesOff();
    }

    public void stop() {
        this.midiHandler.stop();
        this.allNotesOff();
    }

    public void seek(Integer percent) {
        this.midiHandler.setPositionPercent1000(percent);
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
                break;
            case SINGLE:
                this.play();
                break;
            default:
                this.stop();
                break;
        }
    }

    public void loadSong(BasicMidiInfo info) {
        if(info != null && (this.midiHandler.getSequenceInfo() == null || !this.midiHandler.getSequenceInfo().fileId.toString().equals(info.fileId.toString()))) {
            this.startLoadSequence(info);
        } else if(info == null) {
            this.stop();
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
        this.midiHandler.stop();
        this.allNotesOff();
        this.midiHandler.close();
    }

    @Override
    public void tickProducer() {
        if(this.midiHandler.isPlaying() && !this.isTransmitterStillValid()) {
            this.stop();
        }
    }

    public ServerMusicPlayerStatusPacket getStatus() {
        UUID currentId = this.midiHandler.getSequenceId();

        return new ServerMusicPlayerStatusPacket(
            this.ownerId,
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
        this.shouldPlayNextLoad = this.shouldPlayNextLoad || this.midiHandler.isPlaying();
        this.stop();
        this.midiHandler.unloadSong();

        if(info.serverMidi) {
            this.loading = false;
            LocalMidiInfo serverSongInfo = ServerMidiManager.getServerSongById(info.fileId);

            if(serverSongInfo != null) {
                Sequence sequence = serverSongInfo.loadSequenceFromFile();

                if(sequence != null) {
                    this.finishLoadSequence(info, sequence);
                    return;
                }
            }

            // Failure if it gets here
            this.onSequenceLoadFailed(info);
        } else {
            ServerTransmitterManager.startLoadSequence(this.ownerId, this.playlistHandler.getClientSourceId(), info);
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
                this.shouldPlayNextLoad = false;
            }
        }
    }

    protected abstract Boolean isTransmitterStillValid();
}
