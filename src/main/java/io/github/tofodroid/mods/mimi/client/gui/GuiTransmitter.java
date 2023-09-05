package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import org.joml.Vector2i;

import io.github.tofodroid.mods.mimi.common.midi.ATransmitterManager;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileInfo;
import io.github.tofodroid.mods.mimi.common.midi.ATransmitterManager.FavoriteMode;
import net.minecraft.client.gui.GuiGraphics;

public class GuiTransmitter extends BaseGui {
    private static final Integer UPDATE_STATUS_EVERY_TICKS = 10;
    private static final Integer ANIM_FRAME_EVERY_TICKS = 3;
    private static final Integer LOADING_ANIMATION_FRAMES = 4;
    
    // Playlist Controls
    private static final Vector2i FAVORITE_FILTER_BUTTON = new Vector2i(300,32);
    private static final Vector2i FAVORITE_FILTER_SCREEN = new Vector2i(318,33);
    private static final Vector2i REFRESH_SONGS_BUTTON = new Vector2i(335,32);

    // Song Controls
    private static final Vector2i TOGGLE_FAVORITE_BUTTON = new Vector2i(335,149);
    private static final Vector2i PREVIOUS_BUTTON = new Vector2i(10,264);
    private static final Vector2i STOP_BUTTON = new Vector2i(29,264);
    private static final Vector2i PLAY_PAUSE_BUTTON = new Vector2i(48,264);
    private static final Vector2i NEXT_BUTTON = new Vector2i(67,264);
    private static final Vector2i LOOP_BUTTON = new Vector2i(86,264);
    private static final Vector2i LOOP_SCREEN = new Vector2i(104,265);
    private static final Vector2i SHUFFLE_BUTTON = new Vector2i(121,264);
    private static final Vector2i SHUFFLE_SCREEN = new Vector2i(139,265);

    // Time Slider
    private static final Integer SLIDE_Y = 263;
    private static final Integer SLIDE_MIN_X = 166;
    private static final Integer SLIDE_MAX_X = 335;
    private static final Integer SLIDE_WIDTH = SLIDE_MAX_X - SLIDE_MIN_X;

    // Data
    private Integer ticksSinceUpdate = 0;
    private Integer ticksSinceAnimFrame = 0;
    private Integer loadingAnimationFrame = 0;

    // MIDI
    private ATransmitterManager transmitterManager;
    
    public GuiTransmitter(ATransmitterManager transmitterManager) {
        super(360, 288, 360, "textures/gui/container_transmitter.png", "item.MIMIMod.gui_transmitter");
        this.transmitterManager = transmitterManager;

        if(this.transmitterManager.isPlaying()) {
            this.transmitterManager.startRefreshMediaStatus();
        }
    }
    
    @Override
    public void init() {
        super.init();
    }

    @Override
    public void tick() {
        if(this.ticksSinceUpdate >= UPDATE_STATUS_EVERY_TICKS) {
            this.transmitterManager.startRefreshMediaStatus();
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(FAVORITE_FILTER_BUTTON))) {
            this.transmitterManager.shiftFavoriteMode();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(REFRESH_SONGS_BUTTON))) {
            this.transmitterManager.refreshSongs();
        } else if(this.transmitterManager.getSelectedSongInfo() != null && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(TOGGLE_FAVORITE_BUTTON))) {
            this.transmitterManager.toggleSelectedSongFavorite();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(PREVIOUS_BUTTON))) {
            Double slidePercentage = null;

            if(this.transmitterManager.songLoaded()) {
                slidePercentage =  Double.valueOf(this.transmitterManager.getSongPositionSeconds()) / Double.valueOf(this.transmitterManager.getSelectedSongInfo().songLength);
            }

            if(slidePercentage != null && slidePercentage >= 0.25) {
                this.transmitterManager.seek(0);
            } else {
                this.transmitterManager.selectPreviousSong();
            }
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(STOP_BUTTON))) {
            this.transmitterManager.stop();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(PLAY_PAUSE_BUTTON))) {
            if(this.transmitterManager.isPlaying()) {
                this.transmitterManager.pause();
            } else {
                this.transmitterManager.play();
            }
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(NEXT_BUTTON))) {
            this.transmitterManager.selectNextSong();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(LOOP_BUTTON))) {
            this.transmitterManager.shiftLoopMode();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHUFFLE_BUTTON))) {
            this.transmitterManager.toggleShuffle();
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
        graphics.blit(guiTexture, START_X + 132, START_Y + 6, 53, 330, 97, 14, TEXTURE_SIZE, TEXTURE_SIZE);

        // Selected Song Box
        if(this.transmitterManager.getDisplayHasSongs() && this.transmitterManager.getSelectedDisplayIndex() >= 0) {
            Integer songOffset = getSongOffset();            
            Integer boxY = (getFirstSongY() - 2) + (11 * songOffset);
            graphics.blit(guiTexture, START_X + 10, START_Y + boxY, 1, 290, 340, 11, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Play/Pause Button
        if(!this.transmitterManager.songLoading()) {
            graphics.blit(guiTexture, START_X + 49, START_Y + 265, this.transmitterManager.isPlaying() ? 14 : 1, 316, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        } else {
            graphics.blit(guiTexture, START_X + 49, START_Y + 265, 1 + this.loadingAnimationFrame*13, 345, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        

        // Toggle Favorite Button
        graphics.blit(guiTexture, START_X + 336, START_Y + 150, this.transmitterManager.isSelectedSongFavorite() ? 40 : 27, 316, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Favorite Filter Screen
        graphics.blit(guiTexture, START_X + FAVORITE_FILTER_SCREEN.x(), START_Y + FAVORITE_FILTER_SCREEN.y(), 66 + (13 * this.transmitterManager.getFavoriteMode()), 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Loop Screen
        graphics.blit(guiTexture, START_X + LOOP_SCREEN.x(), START_Y + LOOP_SCREEN.y(), 1 + (13 * this.transmitterManager.getLoopMode()), 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Shuffle Screen    
        graphics.blit(guiTexture, START_X + SHUFFLE_SCREEN.x(), START_Y + SHUFFLE_SCREEN.y(), 40 + (13 * this.transmitterManager.getShuffleMode()), 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Time Slider
        Integer slideOffset = 0;
        if(this.transmitterManager.songLoaded()) {
            Integer slideLength = this.transmitterManager.getSelectedSongInfo().songLength;
            Integer slideProgress = this.transmitterManager.getSongPositionSeconds();
            Double slidePercentage =  Double.valueOf(slideProgress) / Double.valueOf(slideLength);
            slideOffset = Double.valueOf(Math.floor(slidePercentage * SLIDE_WIDTH)).intValue();
        }
        graphics.blit(guiTexture, START_X + SLIDE_MIN_X + slideOffset, START_Y + SLIDE_Y, 352, 290, 7, 17, TEXTURE_SIZE, TEXTURE_SIZE);
        
        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Playlist
        this.renderPlaylistSongs(graphics);

        // Current Song
        MidiFileInfo info = this.transmitterManager.getSelectedSongInfo();

        if(info != null) {
            graphics.drawString(font, this.truncateString(font, info.file.getName(), 264), START_X + 66, START_Y + 153, 0xFF00E600);
    
            if(info.instrumentMapping != null) {
                graphics.drawString(font, "Channel Instrument Assignments: ", START_X + 12, START_Y + 168, 0xFF00E600);
                
                Integer index = 0;
                for(Integer i = 0; i < 16; i+=2) {
                    String name = info.instrumentMapping.get(i) == null ? "None" : info.instrumentMapping.get(i);
                    graphics.drawString(font, (i < 9 ? "0" : "") + (i+1) + ": " + name, START_X + 12, START_Y + 182 + 10*index, 0xFF00E600);
                    index++;
                }

                index = 0;
                for(Integer i = 1; i < 16; i+=2) {
                    String name = info.instrumentMapping.get(i) == null ? "None" : info.instrumentMapping.get(i);
                    graphics.drawString(font, (i < 9 ? "0" : "") + (i+1) + ": " + name, START_X + 180, START_Y + 182 + 10*index, 0xFF00E600);
                    index++;
                }
            }
        }
        
        return graphics;
    }

    protected GuiGraphics renderPlaylistSongs(GuiGraphics graphics) {
        if(this.transmitterManager.getDisplayHasSongs()) {
            Integer minSong = getMinSong();

            for(int i = 0; i < getVisibleSongs(); i++) {
                if(this.transmitterManager.getDisplaySongCount() > (minSong + i)) {
                    MidiFileInfo info = this.transmitterManager.getDisplaySongList().get(minSong + i);
                    graphics.drawString(font, this.truncateString(font, ((minSong + i) < 9 ? "0" : "") + (minSong + i + 1) + "). " + info.file.getName(), 324), START_X + 12, START_Y + getFirstSongY() + i * 11, 0xFF00E600);
                    
                    // Favorite Badge
                    if(this.transmitterManager.favoriteMode() != FavoriteMode.NOT_FAVORITE && (this.transmitterManager.favoriteMode() == FavoriteMode.FAVORITE || this.transmitterManager.isSongFavorite(info.toUUID()))) {
                        graphics.blit(guiTexture, START_X + 339, START_Y + getFirstSongY() - 1 + i * 11, 145, 302, 9, 9, TEXTURE_SIZE, TEXTURE_SIZE);
                    }
                } else {
                    break;
                }            
            }
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
        if(this.transmitterManager.getDisplaySongCount() <= getVisibleSongs() || this.transmitterManager.getSelectedDisplayIndex() < 4) {
            return 0;
        } else if(this.transmitterManager.getSelectedDisplayIndex() > this.transmitterManager.getDisplaySongCount() - 4) {
            return this.transmitterManager.getDisplaySongCount() - getVisibleSongs();
        } else {
            return this.transmitterManager.getSelectedDisplayIndex() - 4;
        }
    }

    protected Integer getSongOffset() {
        if(this.transmitterManager.getDisplaySongCount() <= getVisibleSongs() || this.transmitterManager.getSelectedDisplayIndex() < 4) {
            return this.transmitterManager.getSelectedDisplayIndex();
        } else if(this.transmitterManager.getSelectedDisplayIndex() > this.transmitterManager.getDisplaySongCount() - 4) {
            return getVisibleSongs() - (this.transmitterManager.getDisplaySongCount() - this.transmitterManager.getSelectedDisplayIndex());
        } else {
            return 4;
        }
    }
}