package io.github.tofodroid.mods.mimi.server.midi.playlist;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.util.TagUtils;
import io.github.tofodroid.mods.mimi.server.midi.playlist.APlaylistHandler.FavoriteMode;
import io.github.tofodroid.mods.mimi.server.midi.playlist.APlaylistHandler.LoopMode;
import io.github.tofodroid.mods.mimi.server.midi.playlist.APlaylistHandler.SourceMode;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

public class PlaylistData extends SavedData {
    public ArrayList<UUID> favoriteSongs = new ArrayList<>();
    public LoopMode loopMode = LoopMode.NONE;
    public FavoriteMode favoriteMode = FavoriteMode.ALL;
    public SourceMode sourceMode = SourceMode.ALL;
    public Boolean isShuffled = false;
    public Integer shuffleSeed = 0;

    public static PlaylistData loadFromTag(CompoundTag tag, Provider registries) {
        PlaylistData data = new PlaylistData();

        if(tag.contains("favorite_songs")) {
            CompoundTag favoriteSongTag = tag.getCompound("favorite_songs");
            data.favoriteSongs = new ArrayList<>(favoriteSongTag.getAllKeys().stream().map(key -> UUID.fromString(key)).collect(Collectors.toList()));
        }

        if(tag.contains("loop_mode")) {
            data.loopMode = tag.getBoolean("loop_mode") ? LoopMode.ALL : LoopMode.SINGLE;
        }
        
        if(tag.contains("favorite_mode")) {
            data.favoriteMode = tag.getBoolean("favorite_mode") ? FavoriteMode.FAVORITE : FavoriteMode.NOT_FAVORITE;
        }
        
        if(tag.contains("source_mode")) {
            data.sourceMode = tag.getBoolean("source_mode") ? SourceMode.CLIENT : SourceMode.SERVER;
        }

        if(tag.contains("SHUFFLED")) {
            data.isShuffled = true;
            data.shuffleSeed = tag.getInt("SHUFFLED");
        }

        return data;
    }

    public static CompoundTag writeToTag(PlaylistData data, CompoundTag resultTag, Provider registries) {
        CompoundTag favoriteSongTag = new CompoundTag();

        if(!data.favoriteSongs.isEmpty()) {
            for(Integer i = 0; i < data.favoriteSongs.size(); i++) {
                favoriteSongTag.putBoolean(data.favoriteSongs.get(i).toString(), true);
            }
            resultTag.put("favorite_songs", favoriteSongTag);
        } else {
            resultTag.remove("favorite_songs");
        }

        if(data.loopMode != LoopMode.NONE) {
            resultTag.putBoolean("loop_mode", data.loopMode == LoopMode.ALL);
        } else {
            resultTag.remove("loop_mode");
        }

        if(data.favoriteMode != FavoriteMode.ALL) {
            resultTag.putBoolean("favorite_mode", data.favoriteMode == FavoriteMode.FAVORITE);
        } else {
            resultTag.remove("favorite_mode");
        }

        if(data.sourceMode != SourceMode.ALL) {
            resultTag.putBoolean("source_mode", data.sourceMode == SourceMode.CLIENT);
        } else {
            resultTag.remove("source_mode");
        }

        if(data.isShuffled) {
            resultTag.putInt("SHUFFLED", data.shuffleSeed);
        } else {
            resultTag.remove("SHUFFLED");
        }

        return resultTag;
    }

    public static PlaylistData loadFromComponents(DataComponentHolder components) {
        PlaylistData data = new PlaylistData();

        CompoundTag cval = TagUtils.getNbtOrDefault(components, "favorite_songs", null);
        if(cval != null) {
            data.favoriteSongs = new ArrayList<>(cval.getAllKeys().stream().map(key -> UUID.fromString(key)).collect(Collectors.toList()));
        }

        Boolean bval = TagUtils.getBooleanOrDefault(components, "loop_mode", null);
        if(bval != null) {
            data.loopMode = bval ? LoopMode.ALL : LoopMode.SINGLE;
        }

        bval = TagUtils.getBooleanOrDefault(components, "favorite_mode", null);
        if(bval != null) {
            data.favoriteMode = bval ? FavoriteMode.FAVORITE : FavoriteMode.FAVORITE;
        }

        bval = TagUtils.getBooleanOrDefault(components, "source_mode", null);
        if(bval != null) {
            data.sourceMode = bval ? SourceMode.CLIENT : SourceMode.SERVER;
        }

        Integer ival = TagUtils.getIntOrDefault(components, "shuffle", null);
        if(ival != null) {
            data.shuffleSeed = ival;
        }

        return data;
    }

    public static ItemStack writeToComponents(PlaylistData data, ItemStack stack) {
        CompoundTag favoriteSongTag = null;
        if(!data.favoriteSongs.isEmpty()) {
            favoriteSongTag = new CompoundTag();
            for(Integer i = 0; i < data.favoriteSongs.size(); i++) {
                favoriteSongTag.putBoolean(data.favoriteSongs.get(i).toString(), true);
            }
        }
        
        TagUtils.setOrRemoveNbt(stack, "favorite_songs", favoriteSongTag);
        TagUtils.setOrRemoveBoolean(stack, "loop_mode", data.loopMode == LoopMode.NONE ? null : data.loopMode == LoopMode.ALL);
        TagUtils.setOrRemoveBoolean(stack, "favorite_mode", data.favoriteMode == FavoriteMode.ALL ? null : data.favoriteMode == FavoriteMode.FAVORITE);
        TagUtils.setOrRemoveBoolean(stack, "source_mode", data.sourceMode == SourceMode.ALL ? null : data.sourceMode == SourceMode.CLIENT);
        TagUtils.setOrRemoveInt(stack, "SHUFFLED", data.isShuffled ? data.shuffleSeed : null);

        return stack;
    }

    @Override
    public CompoundTag save(CompoundTag tag, Provider registries) {
        return PlaylistData.writeToTag(this, tag, registries);
    }
}