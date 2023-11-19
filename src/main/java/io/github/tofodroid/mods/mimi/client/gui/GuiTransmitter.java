package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Map;
import java.util.UUID;

import org.joml.Vector2i;

import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerSongListPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacket.CONTROL;
import io.github.tofodroid.mods.mimi.server.midi.AMusicPlayerPlaylistHandler.FavoriteMode;
import io.github.tofodroid.mods.mimi.util.MidiFileUtils;
import net.minecraft.client.gui.GuiGraphics;

public class GuiTransmitter extends BaseGui {
    protected static final Integer UPDATE_STATUS_EVERY_TICKS = 10;
    protected static final Integer ANIM_FRAME_EVERY_TICKS = 3;
    protected static final Integer LOADING_ANIMATION_FRAMES = 4;
    
    // Playlist Controls
    protected static final Vector2i FAVORITE_FILTER_BUTTON = new Vector2i(300,32);
    protected static final Vector2i FAVORITE_FILTER_SCREEN = new Vector2i(318,33);
    protected static final Vector2i REFRESH_SONGS_BUTTON = new Vector2i(335,32);

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
    protected static final Integer SLIDE_Y = 263;
    protected static final Integer SLIDE_MIN_X = 166;
    protected static final Integer SLIDE_MAX_X = 335;
    protected static final Integer SLIDE_WIDTH = SLIDE_MAX_X - SLIDE_MIN_X;

    // Animation
    protected Integer ticksSinceUpdate = 0;
    protected Integer ticksSinceAnimFrame = 0;
    protected Integer loadingAnimationFrame = 0;

    // Data
    protected UUID musicPlayerId;
    protected ServerMusicPlayerStatusPacket musicStatus;
    protected ServerMusicPlayerSongListPacket songList;
    
    public GuiTransmitter(UUID musicPlayerId) {
        super(360, 288, 360, "textures/gui/container_transmitter.png", "item.MIMIMod.gui_transmitter");
        this.musicPlayerId = musicPlayerId;
        this.musicStatus = new ServerMusicPlayerStatusPacket(musicPlayerId);
        this.songList = new ServerMusicPlayerSongListPacket(musicPlayerId);
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
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(FAVORITE_FILTER_BUTTON))) {
            this.sendTransmitterCommand(CONTROL.FAVE_M);
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
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected  GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // Background
        graphics.blit(guiTexture, START_X, START_Y, 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Title
        this.renderTitle(graphics);

        // Selected Song Box
        if(!this.songList.infos.isEmpty() && this.musicStatus.fileIndex != null) {
            Integer songOffset = getSongOffset();            
            Integer boxY = (getFirstSongY() - 2) + (11 * songOffset);
            graphics.blit(guiTexture, START_X + 10, START_Y + boxY, 1, 290, 340, 11, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Play/Pause Button
        if(!this.musicStatus.isLoading) {
            graphics.blit(guiTexture, START_X + 49, START_Y + 265, this.musicStatus.isPlaying ? 14 : 1, 316, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        } else {
            graphics.blit(guiTexture, START_X + 49, START_Y + 265, 1 + this.loadingAnimationFrame*13, 345, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        

        // Toggle Favorite Button
        graphics.blit(guiTexture, START_X + 336, START_Y + 150, this.musicStatus.isFileFavorite ? 40 : 27, 316, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Favorite Filter Screen
        graphics.blit(guiTexture, START_X + FAVORITE_FILTER_SCREEN.x(), START_Y + FAVORITE_FILTER_SCREEN.y(), 66 + (13 * this.musicStatus.favoriteMode.ordinal()), 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Loop Screen
        graphics.blit(guiTexture, START_X + LOOP_SCREEN.x(), START_Y + LOOP_SCREEN.y(), 1 + (13 * this.musicStatus.loopMode.ordinal()), 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Shuffle Screen    
        graphics.blit(guiTexture, START_X + SHUFFLE_SCREEN.x(), START_Y + SHUFFLE_SCREEN.y(), 40 + (13 * (this.musicStatus.isShuffled ? 1 : 0)), 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Time Slider
        Integer slideOffset = 0;
        if(this.musicStatus.songPositionSeconds != null && this.musicStatus.songLengthSeconds != null && this.musicStatus.songLengthSeconds > 0) {
            Integer slideLength = this.musicStatus.songLengthSeconds;
            Integer slideProgress = this.musicStatus.songPositionSeconds;
            Double slidePercentage =  Double.valueOf(slideProgress) / Double.valueOf(slideLength);
            slideOffset = Double.valueOf(Math.floor(slidePercentage * SLIDE_WIDTH)).intValue();
        }
        graphics.blit(guiTexture, START_X + SLIDE_MIN_X + slideOffset, START_Y + SLIDE_Y, 352, 290, 7, 17, TEXTURE_SIZE, TEXTURE_SIZE);
        
        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Playlist
        if(!this.songList.infos.isEmpty()) {
            Integer minSong = getMinSong();

            for(int i = 0; i < getVisibleSongs(); i++) {
                if(this.songList.infos.size() > (minSong + i)) {
                    BasicMidiInfo info = this.songList.infos.get(minSong + i);
                    graphics.drawString(font, this.truncateString(font, ((minSong + i) < 9 ? "0" : "") + (minSong + i + 1) + "). " + info.fileName, this.maxPlaylistSongTitleWidth()), START_X + 12, START_Y + getFirstSongY() + i * 11, 0xFF00E600);
                    this.renderPlaylistSongBadges(graphics, info, i, minSong);
                } else {
                    break;
                }            
            }
        }

        // Current Song
        if(this.musicStatus.fileIndex != null && this.musicStatus.fileIndex < this.songList.infos.size()) {
            BasicMidiInfo info = this.songList.infos.get(this.musicStatus.fileIndex);
            graphics.drawString(font, this.truncateString(font, info.fileName, 264), START_X + 66, START_Y + 153, 0xFF00E600);

            if(this.musicStatus.isLoading) {
                graphics.drawString(font, "Channel Instrument Assignments: Loading...", START_X + 12, START_Y + 168, 0xFF00E600);
            } else if(!this.musicStatus.isLoading && this.musicStatus.isLoadFailed) {
                graphics.drawString(font, "Failed to load selected song:", START_X + 12, START_Y + 168, 0xFF00E600);

                if(info.serverMidi) {
                    graphics.drawString(font, "It may be invalid or may have been deleted from the server.", START_X + 12, START_Y + 192, 0xFF00E600);
                } else {
                    graphics.drawString(font, "It may be invalid or may have been deleted from your MIDI folder.", START_X + 12, START_Y + 192, 0xFF00E600);
                }

                graphics.drawString(font, "Try refreshing the the MIDI list with the button in the top right.", START_X + 12, START_Y + 202, 0xFF00E600);
            } else if(this.musicStatus.channelMapping != null) {
                Map<Integer, String> instrumentMapping = MidiFileUtils.getInstrumentMapping(this.musicStatus.channelMapping);
                graphics.drawString(font, "Channel Instrument Assignments: ", START_X + 12, START_Y + 168, 0xFF00E600);
                
                Integer index = 0;
                for(Integer i = 0; i < 16; i+=2) {
                    String name = instrumentMapping.get(i) == null ? "None" : instrumentMapping.get(i);
                    graphics.drawString(font, (i < 9 ? "0" : "") + (i+1) + ": " + name, START_X + 12, START_Y + 182 + 10*index, 0xFF00E600);
                    index++;
                }

                index = 0;
                for(Integer i = 1; i < 16; i+=2) {
                    String name = instrumentMapping.get(i) == null ? "None" : instrumentMapping.get(i);
                    graphics.drawString(font, (i < 9 ? "0" : "") + (i+1) + ": " + name, START_X + 180, START_Y + 182 + 10*index, 0xFF00E600);
                    index++;
                }
            }
        }
        
        return graphics;
    }

    protected Integer maxPlaylistSongTitleWidth() {
        return 324;
    }

    protected GuiGraphics renderTitle(GuiGraphics graphics) {
        graphics.blit(guiTexture, START_X + 132, START_Y + 6, 53, 330, 97, 14, TEXTURE_SIZE, TEXTURE_SIZE);
        return graphics;
    }

    protected GuiGraphics renderPlaylistSongBadges(GuiGraphics graphics, BasicMidiInfo info, Integer songIndex, Integer minSong) {
        // Favorite Badge
        if(this.musicStatus.favoriteMode != FavoriteMode.NOT_FAVORITE && (this.musicStatus.favoriteMode == FavoriteMode.FAVORITE || this.songList.favoriteIndicies.contains(minSong + songIndex))) {
            graphics.blit(guiTexture, START_X + 339, START_Y + getFirstSongY() - 1 + songIndex * 11, 145, 302, 9, 9, TEXTURE_SIZE, TEXTURE_SIZE);
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

    protected void sendTransmitterCommand(CONTROL control) {
        NetworkManager.INFO_CHANNEL.sendToServer(new TransmitterControlPacket(musicPlayerId, control));
    }

    protected void startRefreshPlayerStatus() {
        NetworkManager.INFO_CHANNEL.sendToServer(new ServerMusicPlayerStatusPacket(this.musicPlayerId));
    }

    protected void startRefreshSongList() {
        NetworkManager.INFO_CHANNEL.sendToServer(new ServerMusicPlayerSongListPacket(this.musicPlayerId));
    }
}