package io.github.tofodroid.mods.mimi.client.gui;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.util.Vector2Int;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerSongListPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacket.CONTROL;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.APlaylistHandler.FavoriteMode;
import io.github.tofodroid.mods.mimi.util.MidiFileUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class GuiTransmitterBlock extends BaseGui {
    protected static final Integer UPDATE_STATUS_EVERY_TICKS = 10;
    protected static final Integer ANIM_FRAME_EVERY_TICKS = 3;
    protected static final Integer LOADING_ANIMATION_FRAMES = 4;
    
    // Playlist Controls
    protected static final Vector2Int FAVORITE_FILTER_BUTTON = new Vector2Int(300,32);
    protected static final Vector2Int FAVORITE_FILTER_SCREEN = new Vector2Int(318,33);
    protected static final Vector2Int REFRESH_SONGS_BUTTON = new Vector2Int(335,32);
    protected static final Vector2Int OPEN_LOCAL_FOLDER_BUTTON = new Vector2Int(10,32);

    // Song Controls
    protected static final Vector2Int TOGGLE_FAVORITE_BUTTON = new Vector2Int(335,118);
    protected static final Vector2Int PREVIOUS_BUTTON = new Vector2Int(10,231);
    protected static final Vector2Int STOP_BUTTON = new Vector2Int(29,231);
    protected static final Vector2Int PLAY_PAUSE_BUTTON = new Vector2Int(48,231);
    protected static final Vector2Int NEXT_BUTTON = new Vector2Int(67,231);
    protected static final Vector2Int LOOP_BUTTON = new Vector2Int(86,231);
    protected static final Vector2Int LOOP_SCREEN = new Vector2Int(104,232);
    protected static final Vector2Int SHUFFLE_BUTTON = new Vector2Int(121,231);
    protected static final Vector2Int SHUFFLE_SCREEN = new Vector2Int(139,232);

    // Time Slider
    protected static final Integer SLIDE_MIN_Y = 230;
    protected static final Integer SLIDE_MAX_Y = 247;
    protected static final Integer SLIDE_MIN_X = 166;
    protected static final Integer SLIDE_MAX_X = 335;
    protected static final Integer SLIDE_WIDTH = SLIDE_MAX_X - SLIDE_MIN_X;
    protected static final Integer SLIDE_HEIGHT = SLIDE_MAX_Y - SLIDE_MIN_Y;
    protected static final Vector2Int SLIDE_CLICK_START = new Vector2Int(SLIDE_MIN_X-4, SLIDE_MIN_Y);
    protected static final Vector2Int SLIDE_CLICK_SIZE = new Vector2Int(SLIDE_WIDTH+8, SLIDE_HEIGHT);

    // Animation
    protected Integer ticksSinceUpdate = 0;
    protected Integer ticksSinceAnimFrame = 0;
    protected Integer loadingAnimationFrame = 0;

    // Data
    protected UUID musicPlayerId;
    protected ServerMusicPlayerStatusPacket musicStatus;
    protected ServerMusicPlayerSongListPacket songList;
    protected Boolean awaitRefresh = false;

    public GuiTransmitterBlock(UUID musicPlayerId) {
        super(360, 255, 360, "textures/gui/container_transmitter.png", "item.MIMIMod.gui_transmitter");
        this.musicPlayerId = musicPlayerId;
        this.musicStatus = new ServerMusicPlayerStatusPacket(musicPlayerId);
        this.songList = new ServerMusicPlayerSongListPacket(musicPlayerId);
    }

    public UUID getMusicPlayerId() {
        return this.musicPlayerId;
    }
    
    @Override
    public void init() {
        super.init();
        this.startRefreshPlayerStatus();
        this.startRefreshSongList();
    }

    @Override
    public void tick() {
        if(this.ticksSinceUpdate >= UPDATE_STATUS_EVERY_TICKS) {
            this.startRefreshPlayerStatus();
            this.ticksSinceUpdate = 0;
        } else {
            this.ticksSinceUpdate++;
        }

        if(this.ticksSinceAnimFrame >= ANIM_FRAME_EVERY_TICKS) {
            this.ticksSinceAnimFrame = 0;

            if(this.loadingAnimationFrame < (LOADING_ANIMATION_FRAMES-1)) {
                this.loadingAnimationFrame++;
            } else {
                this.loadingAnimationFrame = 0;
            }
        } else {
            this.ticksSinceAnimFrame++;
        }
    }

    public void handleMusicPlayerStatusPacket(ServerMusicPlayerStatusPacket packet) {
        this.musicStatus = packet;
    }

    public void handleMusicplayerSongListPacket(ServerMusicPlayerSongListPacket packet) {
        this.songList = packet;
        this.awaitRefresh = false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(FAVORITE_FILTER_BUTTON))) {
            this.sendTransmitterCommand(CONTROL.FAVE_M);
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(OPEN_LOCAL_FOLDER_BUTTON)) && this.isSinglePlayerOrLANHost()) {
            Util.getPlatform().openUri(Path.of(MIMIMod.getProxy().serverMidiFiles().getCurrentFolderPath()).toUri());
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(REFRESH_SONGS_BUTTON))) {
            this.startRefreshSongList();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(TOGGLE_FAVORITE_BUTTON))) {
            this.sendTransmitterCommand(CONTROL.MARKFAVE);
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(PREVIOUS_BUTTON))) {
            Double slidePercentage = null;

            if(this.musicStatus.songPositionSeconds != null && this.musicStatus.songLengthSeconds != null && this.musicStatus.songLengthSeconds > 0) {
                slidePercentage =  Double.valueOf(this.musicStatus.songPositionSeconds) / Double.valueOf(this.musicStatus.songLengthSeconds);
            }

            if(slidePercentage != null && slidePercentage >= 0.25) {
                this.sendTransmitterCommand(CONTROL.RESTART);
            } else {
                this.sendTransmitterCommand(CONTROL.PREV);
            }
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(STOP_BUTTON))) {
            this.sendTransmitterCommand(CONTROL.STOP);
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(PLAY_PAUSE_BUTTON))) {
            if(this.musicStatus.isPlaying) {
                this.sendTransmitterCommand(CONTROL.PAUSE);
            } else {
                this.sendTransmitterCommand(CONTROL.PLAY);
            }
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(NEXT_BUTTON))) {
            this.sendTransmitterCommand(CONTROL.NEXT);
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(LOOP_BUTTON))) {
            this.sendTransmitterCommand(CONTROL.LOOP_M);
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHUFFLE_BUTTON))) {
            this.sendTransmitterCommand(CONTROL.SHUFFLE);
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SLIDE_CLICK_START), SLIDE_CLICK_SIZE)) {
            Integer percent = Double.valueOf(Math.floor(1000.0 * (Double.valueOf((imouseX - 4 - SLIDE_MIN_X - START_X)) / Double.valueOf(SLIDE_WIDTH)))).intValue();
            // Clamp between 0 and 1000
            percent = percent < 0 ? 0 : ( percent > 1000 ? 1000 : percent);
            MIMIMod.LOGGER.info("CLICKED SEEK");
            this.sendTransmitterCommand(CONTROL.SEEK, percent);
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    

    @SuppressWarnings("resource")
    public Boolean isSinglePlayerOrLANHost() {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().hasSingleplayerServer();
    }

    @Override
    protected  GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Background
        this.blitAbsolute(graphics, guiTexture, START_X, START_Y, 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Selected Song Box
        if(!this.songList.infos.isEmpty() && this.musicStatus.fileIndex != null && !this.awaitRefresh) {
            Integer songOffset = getSongBoxOffset();            
            Integer boxY = (getFirstSongY() - 2) + (11 * songOffset);
            this.blitAbsolute(graphics, guiTexture, START_X + 10, START_Y + boxY, 1, 257, 340, 11, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Play/Pause Button
        if(!this.musicStatus.isLoading) {
            this.blitAbsolute(graphics, guiTexture, START_X + 49, START_Y + 232, this.musicStatus.isPlaying ? 14 : 1, 283, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        } else {
            this.blitAbsolute(graphics, guiTexture, START_X + 49, START_Y + 232, 1 + this.loadingAnimationFrame*13, 312, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        
        // Server Folder Button
        if(this.isSinglePlayerOrLANHost()) {
            this.blitAbsolute(graphics, guiTexture, START_X + 9, START_Y + 31, 173, 269, 17, 17, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Toggle Favorite Button
        this.blitAbsolute(graphics, guiTexture, START_X + 336, START_Y + 117, this.musicStatus.isFileFavorite ? 40 : 27, 283, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Favorite Filter Screen
        this.blitAbsolute(graphics, guiTexture, START_X + FAVORITE_FILTER_SCREEN.x(), START_Y + FAVORITE_FILTER_SCREEN.y(), 66 + (13 * this.musicStatus.favoriteMode.ordinal()), 269, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Loop Screen
        this.blitAbsolute(graphics, guiTexture, START_X + LOOP_SCREEN.x(), START_Y + LOOP_SCREEN.y(), 1 + (13 * this.musicStatus.loopMode.ordinal()), 269, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Shuffle Screen    
        this.blitAbsolute(graphics, guiTexture, START_X + SHUFFLE_SCREEN.x(), START_Y + SHUFFLE_SCREEN.y(), 40 + (13 * (this.musicStatus.isShuffled ? 1 : 0)), 269, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Time Slider
        Integer slideOffset = 0;
        if(this.musicStatus.songPositionSeconds != null && this.musicStatus.songLengthSeconds != null && this.musicStatus.songLengthSeconds > 0) {
            Integer slideLength = this.musicStatus.songLengthSeconds;
            Integer slideProgress = this.musicStatus.songPositionSeconds;
            Double slidePercentage =  Double.valueOf(slideProgress) / Double.valueOf(slideLength);
            slideOffset = Double.valueOf(Math.floor(slidePercentage * SLIDE_WIDTH)).intValue();
        }
        this.blitAbsolute(graphics, guiTexture, START_X + SLIDE_MIN_X + slideOffset, START_Y + SLIDE_MIN_Y, 352, 257, 7, 17, TEXTURE_SIZE, TEXTURE_SIZE);
        
        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Playlist
        if(!this.songList.infos.isEmpty() && !this.awaitRefresh) {
            Integer minSong = getMinSong();

            for(int i = 0; i < getVisibleSongs(); i++) {
                if(this.songList.infos.size() > (minSong + i)) {
                    BasicMidiInfo info = this.songList.infos.get(minSong + i);
                    this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, ((minSong + i) < 9 ? "0" : "") + (minSong + i + 1) + "). " + info.fileName, this.maxPlaylistSongTitleWidth()), START_X + 12, START_Y + getFirstSongY() + i * 11, 0xFF00E600);
                    this.renderPlaylistSongBadges(graphics, info, i, minSong);
                } else {
                    break;
                }            
            }
        } else {
            this.drawStringAbsolute(graphics, font, this.awaitRefresh ? "Loading..." : "No songs found", START_X + 12, START_Y + getFirstSongY(), 0xFF00E600);
        }

        // Current Song
        if(!this.musicStatus.isLoading && this.musicStatus.isLoadFailed) {
            this.drawStringAbsolute(graphics, font, "Song failed to load:", START_X + 12, START_Y + 153, 0xFF00E600);
            this.drawStringAbsolute(graphics, font, "It may be invalid or may have been deleted. Refresh the", START_X + 12, START_Y + 159, 0xFF00E600);
            this.drawStringAbsolute(graphics, font, "list with the button in the top right and select a new song.", START_X + 12, START_Y + 169, 0xFF00E600);
        } else if(this.musicStatus.fileIndex != null && this.musicStatus.fileIndex < this.songList.infos.size()) {
            BasicMidiInfo info = this.songList.infos.get(this.musicStatus.fileIndex);
            this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, info.fileName, 264), START_X + 66, START_Y + 120, 0xFF00E600);

            if(this.musicStatus.isLoading) {
                this.drawStringAbsolute(graphics, font, "Channel Instrument Assignments: Loading...", START_X + 12, START_Y + 135, 0xFF00E600);
            } else if(this.musicStatus.channelMapping != null) {
                Map<Integer, String> instrumentMapping = MidiFileUtils.getInstrumentMapping(this.musicStatus.channelMapping);
                this.drawStringAbsolute(graphics, font, "Channel Instrument Assignments: ", START_X + 12, START_Y + 135, 0xFF00E600);
                
                Integer index = 0;
                for(Integer i = 0; i < 16; i+=2) {
                    String name = instrumentMapping.get(i) == null ? "None" : instrumentMapping.get(i);
                    this.drawStringAbsolute(graphics, font, (i < 9 ? "0" : "") + (i+1) + ": " + name, START_X + 12, START_Y + 149 + 10*index, 0xFF00E600);
                    index++;
                }

                index = 0;
                for(Integer i = 1; i < 16; i+=2) {
                    String name = instrumentMapping.get(i) == null ? "None" : instrumentMapping.get(i);
                    this.drawStringAbsolute(graphics, font, (i < 9 ? "0" : "") + (i+1) + ": " + name, START_X + 180, START_Y + 149 + 10*index, 0xFF00E600);
                    index++;
                }
            }
        }
        
        return graphics;
    }

    protected Integer maxPlaylistSongTitleWidth() {
        return 324;
    }

    protected GuiGraphics renderPlaylistSongBadges(GuiGraphics graphics, BasicMidiInfo info, Integer songIndex, Integer minSong) {
        // Favorite Badge
        if(this.musicStatus.favoriteMode != FavoriteMode.NOT_FAVORITE && (this.musicStatus.favoriteMode == FavoriteMode.FAVORITE || this.songList.favoriteIndicies.contains(minSong + songIndex))) {
            this.blitAbsolute(graphics, guiTexture, START_X + 339, START_Y + getFirstSongY() - 1 + songIndex * 11, 145, 269, 9, 9, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        return graphics;
    }

    protected Integer getVisibleSongs() {
        return 5;
    }

    protected Integer getFirstSongY() {
        return 52;
    }

    protected Integer getMinSong() {
        if(this.songList.infos.size() <= getVisibleSongs() || this.musicStatus.fileIndex == null || this.musicStatus.fileIndex < 2) {
            return 0;
        } else if(this.musicStatus.fileIndex >= this.songList.infos.size() - 2) {
            return this.songList.infos.size() - getVisibleSongs();
        } else {
            return this.musicStatus.fileIndex - 2;
        }
    }

    protected Integer getSongBoxOffset() {
        if(this.musicStatus.fileIndex != null && (this.songList.infos.size() <= getVisibleSongs() || this.musicStatus.fileIndex < 2)) {
            return this.musicStatus.fileIndex;
        } else if(this.musicStatus.fileIndex != null && (this.musicStatus.fileIndex >= this.songList.infos.size() - 2)) {
            return getVisibleSongs() - (this.songList.infos.size() - this.musicStatus.fileIndex);
        } else {
            return 2;
        }
    }

    protected void sendTransmitterCommand(CONTROL control, Integer data) {
        NetworkProxy.sendToServer(new TransmitterControlPacket(musicPlayerId, control, data));
    }

    protected void sendTransmitterCommand(CONTROL control) {
        NetworkProxy.sendToServer(new TransmitterControlPacket(musicPlayerId, control));
    }

    protected void startRefreshPlayerStatus() {
        NetworkProxy.sendToServer(new ServerMusicPlayerStatusPacket(this.musicPlayerId));
    }

    protected void startRefreshSongList() {
        NetworkProxy.sendToServer(new ServerMusicPlayerSongListPacket(this.musicPlayerId));
        this.awaitRefresh = true;
    }
}