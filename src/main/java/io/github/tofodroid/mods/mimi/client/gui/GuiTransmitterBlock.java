package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.joml.Vector2i;

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
import com.mojang.blaze3d.vertex.PoseStack;

public class GuiTransmitterBlock extends BaseGui {
    protected static final Integer UPDATE_STATUS_EVERY_TICKS = 10;
    protected static final Integer ANIM_FRAME_EVERY_TICKS = 3;
    protected static final Integer LOADING_ANIMATION_FRAMES = 4;
    
    // Playlist Controls
    protected static final Vector2i FAVORITE_FILTER_BUTTON = new Vector2i(300,32);
    protected static final Vector2i FAVORITE_FILTER_SCREEN = new Vector2i(318,33);
    protected static final Vector2i REFRESH_SONGS_BUTTON = new Vector2i(335,32);
    protected static final Vector2i OPEN_LOCAL_FOLDER_BUTTON = new Vector2i(10,32);

    // Song Controls
    protected static final Vector2i TOGGLE_FAVORITE_BUTTON = new Vector2i(335,149);
    protected static final Vector2i PREVIOUS_BUTTON = new Vector2i(10,264);
    protected static final Vector2i STOP_BUTTON = new Vector2i(29,264);
    protected static final Vector2i PLAY_PAUSE_BUTTON = new Vector2i(48,264);
    protected static final Vector2i NEXT_BUTTON = new Vector2i(67,264);
    protected static final Vector2i LOOP_BUTTON = new Vector2i(86,264);
    protected static final Vector2i LOOP_SCREEN = new Vector2i(104,265);
    protected static final Vector2i SHUFFLE_BUTTON = new Vector2i(121,264);
    protected static final Vector2i SHUFFLE_SCREEN = new Vector2i(139,265);

    // Time Slider
    protected static final Integer SLIDE_MIN_Y = 263;
    protected static final Integer SLIDE_MAX_Y = 280;
    protected static final Integer SLIDE_MIN_X = 166;
    protected static final Integer SLIDE_MAX_X = 335;
    protected static final Integer SLIDE_WIDTH = SLIDE_MAX_X - SLIDE_MIN_X;
    protected static final Integer SLIDE_HEIGHT = SLIDE_MAX_Y - SLIDE_MIN_Y;
    protected static final Vector2i SLIDE_CLICK_START = new Vector2i(SLIDE_MIN_X-4, SLIDE_MIN_Y);
    protected static final Vector2i SLIDE_CLICK_SIZE = new Vector2i(SLIDE_WIDTH+8, SLIDE_HEIGHT);

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
        super(360, 288, 360, "textures/gui/container_transmitter.png", "item.MIMIMod.gui_transmitter");
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
            Integer percent = Double.valueOf(Math.floor(100.0 * (Double.valueOf((imouseX - 4 - SLIDE_MIN_X - START_X)) / Double.valueOf(SLIDE_WIDTH)))).intValue();
            // Clamp between 0 and 100
            percent = percent < 0 ? 0 : ( percent > 100 ? 100 : percent);
            this.sendTransmitterCommand(CONTROL.SEEK, percent);
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    

    @SuppressWarnings("resource")
    public Boolean isSinglePlayerOrLANHost() {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().hasSingleplayerServer();
    }

    @Override
    protected  PoseStack renderGraphics(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // Background
        blit(graphics, START_X, START_Y, 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Playlist badges
        if(!this.songList.infos.isEmpty() && !this.awaitRefresh) {
            Integer minSong = getMinSong();

            for(int i = 0; i < getVisibleSongs(); i++) {
                if(this.songList.infos.size() > (minSong + i)) {
                    BasicMidiInfo info = this.songList.infos.get(minSong + i);
                    this.renderPlaylistSongBadges(graphics, info, i, minSong);
                } else {
                    break;
                }            
            }
        }

        // Selected Song Box
        if(!this.songList.infos.isEmpty() && this.musicStatus.fileIndex != null && !this.awaitRefresh) {
            Integer songOffset = getSongOffset();            
            Integer boxY = (getFirstSongY() - 2) + (11 * songOffset);
            blit(graphics, START_X + 10, START_Y + boxY, 1, 290, 340, 11, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Play/Pause Button
        if(!this.musicStatus.isLoading) {
            blit(graphics, START_X + 49, START_Y + 265, this.musicStatus.isPlaying ? 14 : 1, 316, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        } else {
            blit(graphics, START_X + 49, START_Y + 265, 1 + this.loadingAnimationFrame*13, 345, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        
        // Server Folder Button
        if(this.isSinglePlayerOrLANHost()) {
            blit(graphics, START_X + 9, START_Y + 31, 173, 302, 17, 17, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Toggle Favorite Button
        blit(graphics, START_X + 336, START_Y + 150, this.musicStatus.isFileFavorite ? 40 : 27, 316, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Favorite Filter Screen
        blit(graphics, START_X + FAVORITE_FILTER_SCREEN.x(), START_Y + FAVORITE_FILTER_SCREEN.y(), 66 + (13 * this.musicStatus.favoriteMode.ordinal()), 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Loop Screen
        blit(graphics, START_X + LOOP_SCREEN.x(), START_Y + LOOP_SCREEN.y(), 1 + (13 * this.musicStatus.loopMode.ordinal()), 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Shuffle Screen    
        blit(graphics, START_X + SHUFFLE_SCREEN.x(), START_Y + SHUFFLE_SCREEN.y(), 40 + (13 * (this.musicStatus.isShuffled ? 1 : 0)), 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Time Slider
        Integer slideOffset = 0;
        if(this.musicStatus.songPositionSeconds != null && this.musicStatus.songLengthSeconds != null && this.musicStatus.songLengthSeconds > 0) {
            Integer slideLength = this.musicStatus.songLengthSeconds;
            Integer slideProgress = this.musicStatus.songPositionSeconds;
            Double slidePercentage =  Double.valueOf(slideProgress) / Double.valueOf(slideLength);
            slideOffset = Double.valueOf(Math.floor(slidePercentage * SLIDE_WIDTH)).intValue();
        }
        blit(graphics, START_X + SLIDE_MIN_X + slideOffset, START_Y + SLIDE_MIN_Y, 352, 290, 7, 17, TEXTURE_SIZE, TEXTURE_SIZE);

        return graphics;
    }

    @Override
    protected PoseStack renderText(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        // Playlist
        if(!this.songList.infos.isEmpty() && !this.awaitRefresh) {
            Integer minSong = getMinSong();

            for(int i = 0; i < getVisibleSongs(); i++) {
                if(this.songList.infos.size() > (minSong + i)) {
                    BasicMidiInfo info = this.songList.infos.get(minSong + i);
                    drawString(graphics, font, this.truncateString(font, ((minSong + i) < 9 ? "0" : "") + (minSong + i + 1) + "). " + info.fileName, this.maxPlaylistSongTitleWidth()), START_X + 12, START_Y + getFirstSongY() + i * 11, 0xFF00E600);
                } else {
                    break;
                }            
            }
        } else {
            drawString(graphics, font, this.awaitRefresh ? "Loading..." : "No songs found", START_X + 12, START_Y + getFirstSongY(), 0xFF00E600);
        }

        // Current Song
        if(!this.musicStatus.isLoading && this.musicStatus.isLoadFailed) {
            drawString(graphics, font, "Song failed to load:", START_X + 12, START_Y + 168, 0xFF00E600);
            drawString(graphics, font, "It may be invalid or may have been deleted. Refresh the", START_X + 12, START_Y + 192, 0xFF00E600);
            drawString(graphics, font, "list with the button in the top right and select a new song.", START_X + 12, START_Y + 202, 0xFF00E600);
        } else if(this.musicStatus.fileIndex != null && this.musicStatus.fileIndex < this.songList.infos.size()) {
            BasicMidiInfo info = this.songList.infos.get(this.musicStatus.fileIndex);
            drawString(graphics, font, this.truncateString(font, info.fileName, 264), START_X + 66, START_Y + 153, 0xFF00E600);

            if(this.musicStatus.isLoading) {
                drawString(graphics, font, "Channel Instrument Assignments: Loading...", START_X + 12, START_Y + 168, 0xFF00E600);
            } else if(this.musicStatus.channelMapping != null) {
                Map<Integer, String> instrumentMapping = MidiFileUtils.getInstrumentMapping(this.musicStatus.channelMapping);
                drawString(graphics, font, "Channel Instrument Assignments: ", START_X + 12, START_Y + 168, 0xFF00E600);
                
                Integer index = 0;
                for(Integer i = 0; i < 16; i+=2) {
                    String name = instrumentMapping.get(i) == null ? "None" : instrumentMapping.get(i);
                    drawString(graphics, font, (i < 9 ? "0" : "") + (i+1) + ": " + name, START_X + 12, START_Y + 182 + 10*index, 0xFF00E600);
                    index++;
                }

                index = 0;
                for(Integer i = 1; i < 16; i+=2) {
                    String name = instrumentMapping.get(i) == null ? "None" : instrumentMapping.get(i);
                    drawString(graphics, font, (i < 9 ? "0" : "") + (i+1) + ": " + name, START_X + 180, START_Y + 182 + 10*index, 0xFF00E600);
                    index++;
                }
            }
        }
        
        return graphics;
    }

    protected Integer maxPlaylistSongTitleWidth() {
        return 324;
    }

    protected PoseStack renderPlaylistSongBadges(PoseStack graphics, BasicMidiInfo info, Integer songIndex, Integer minSong) {
        // Favorite Badge
        if(this.musicStatus.favoriteMode != FavoriteMode.NOT_FAVORITE && (this.musicStatus.favoriteMode == FavoriteMode.FAVORITE || this.songList.favoriteIndicies.contains(minSong + songIndex))) {
            blit(graphics, START_X + 339, START_Y + getFirstSongY() - 1 + songIndex * 11, 145, 302, 9, 9, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        return graphics;
    }

    protected Integer getVisibleSongs() {
        return 8;
    }

    protected Integer getFirstSongY() {
        return 52;
    }

    protected Integer getMinSong() {
        if(this.songList.infos.size() <= getVisibleSongs() || this.musicStatus.fileIndex == null || this.musicStatus.fileIndex < 4) {
            return 0;
        } else if(this.musicStatus.fileIndex > this.songList.infos.size() - 4) {
            return this.songList.infos.size() - getVisibleSongs();
        } else {
            return this.musicStatus.fileIndex - 4;
        }
    }

    protected Integer getSongOffset() {
        if(this.musicStatus.fileIndex != null && (this.songList.infos.size() <= getVisibleSongs() || this.musicStatus.fileIndex < 4)) {
            return this.musicStatus.fileIndex;
        } else if(this.musicStatus.fileIndex != null && (this.musicStatus.fileIndex > this.songList.infos.size() - 4)) {
            return getVisibleSongs() - (this.songList.infos.size() - this.musicStatus.fileIndex);
        } else {
            return 4;
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