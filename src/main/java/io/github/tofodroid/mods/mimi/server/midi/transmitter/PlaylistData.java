package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.server.midi.transmitter.APlaylistHandler.FavoriteMode;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.APlaylistHandler.LoopMode;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.APlaylistHandler.SourceMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class PlaylistData extends SavedData {
    public ArrayList<UUID> favoriteSongs = new ArrayList<>();
    public LoopMode loopMode = LoopMode.NONE;
    public FavoriteMode favoriteMode = FavoriteMode.ALL;
    public SourceMode sourceMode = SourceMode.ALL;
    public Boolean isShuffled = false;
    public Integer shuffleSeed = 0;

    @Override
    public CompoundTag save(CompoundTag tag) {
        return PlaylistData.writeToTag(this, tag);
    }

    public static PlaylistData loadFromTag(CompoundTag tag) {
        PlaylistData data = new PlaylistData();

        if(tag.contains("FAVORITE_SONGS")) {
            CompoundTag favoriteSongTag = tag.getCompound("FAVORITE_SONGS");
            data.favoriteSongs = new ArrayList<>(favoriteSongTag.getAllKeys().stream().map(key -> UUID.fromString(key)).collect(Collectors.toList()));
        }

        if(tag.contains("LOOP_MODE")) {
            data.loopMode = tag.getBoolean("LOOP_MODE") ? LoopMode.ALL : LoopMode.SINGLE;
        }
        
        if(tag.contains("FAVORITE_MODE")) {
            data.favoriteMode = tag.getBoolean("FAVORITE_MODE") ? FavoriteMode.FAVORITE : FavoriteMode.NOT_FAVORITE;
        }
        
        if(tag.contains("SOURCE_MODE")) {
            data.sourceMode = tag.getBoolean("SOURCE_MODE") ? SourceMode.CLIENT : SourceMode.SERVER;
        }

        if(tag.contains("SHUFFLED")) {
            data.isShuffled = true;
            data.shuffleSeed = tag.getInt("SHUFFLED");
        }

        return data;
    }

    public static CompoundTag writeToTag(PlaylistData data, CompoundTag resultTag) {
        CompoundTag favoriteSongTag = new CompoundTag();

        if(!data.favoriteSongs.isEmpty()) {
            for(Integer i = 0; i < data.favoriteSongs.size(); i++) {
                favoriteSongTag.putBoolean(data.favoriteSongs.get(i).toString(), true);
            }
            resultTag.put("FAVORITE_SONGS", favoriteSongTag);
        } else {
            resultTag.remove("FAVORITE_SONGS");
        }

        if(data.loopMode != LoopMode.NONE) {
            resultTag.putBoolean("LOOP_MODE", data.loopMode == LoopMode.ALL);
        } else {
            resultTag.remove("LOOP_MODE");
        }

        if(data.favoriteMode != FavoriteMode.ALL) {
            resultTag.putBoolean("FAVORITE_MODE", data.favoriteMode == FavoriteMode.FAVORITE);
        } else {
            resultTag.remove("FAVORITE_MODE");
        }

        if(data.sourceMode != SourceMode.ALL) {
            resultTag.putBoolean("SOURCE_MODE", data.sourceMode == SourceMode.CLIENT);
        } else {
            resultTag.remove("SOURCE_MODE");
        }

        if(data.isShuffled) {
            resultTag.putInt("SHUFFLED", data.shuffleSeed);
        } else {
            resultTag.remove("SHUFFLED");
        }

        return resultTag;
    }
}