package io.github.tofodroid.mods.mimi.client.gui;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2i;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.client.midi.MidiInputManager;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileInfo;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;

public class GuiTransmitter extends BaseGui {
    private static final Integer UPDATE_STATUS_EVERY_TICKS = 10;

    // GUI
    private static final Integer DEFAULT_TEXT_FIELD_COLOR = 14737632;
    private EditBox folderPathField;
    
    // Button Boxes
    private static final Vector2i TOGGLE_MODE_BUTTON = new Vector2i(335,10);

    // Local Folder
    private static final Vector2i LOAD_FOLDER_BUTTON = new Vector2i(297,32);
    private static final Vector2i SAVE_DEFAULT_BUTTON = new Vector2i(316,32);
    private static final Vector2i LOAD_DEFAULT_BUTTON = new Vector2i(335,32);

    // Media Controls
    private static final Vector2i PREVIOUS_BUTTON = new Vector2i(10,265);
    private static final Vector2i STOP_BUTTON = new Vector2i(29,265);
    private static final Vector2i PLAY_PAUSE_BUTTON = new Vector2i(48,265);
    private static final Vector2i NEXT_BUTTON = new Vector2i(67,265);
    private static final Vector2i LOOP_BUTTON = new Vector2i(86,265);
    private static final Vector2i LOOP_SCREEN = new Vector2i(104,266);
    private static final Vector2i SHUFFLE_BUTTON = new Vector2i(121,265);
    private static final Vector2i SHUFFLE_SCREEN = new Vector2i(139,266);

    // Time Slider
    private static final Integer SLIDE_Y = 264;
    private static final Integer SLIDE_MIN_X = 201;
    private static final Integer SLIDE_MAX_X = 335;
    private static final Integer SLIDE_WIDTH = SLIDE_MAX_X - SLIDE_MIN_X;

    // Data
    private String folderPathString;
    private Integer ticksSinceUpdate = 0;

    // MIDI
    private MidiInputManager midiInputManager;
    
    public GuiTransmitter(Player player) {
        super(360, 290, 360, "textures/gui/container_transmitter.png", "item.MIMIMod.gui_transmitter");
        this.midiInputManager = ((ClientProxy)MIMIMod.proxy).getMidiInput();
        this.folderPathString = ModConfigs.CLIENT.playlistFolderPath.get();

        if(this.midiInputManager.enderTransmitterManager.isPlaying()) {
            this.midiInputManager.enderTransmitterManager.startRefreshMediaStatus();
        }
    }

    @Override
    public void init() {
        super.init();

        // Fields
        folderPathField = this.addWidget(new EditBox(this.font, this.START_X + 86, this.START_Y + 35, 207, 10, CommonComponents.EMPTY));
        folderPathField.setValue(folderPathString);
        folderPathField.setMaxLength(256);
        folderPathField.setResponder(this::handlePathChange);
    }

    @Override
    public void tick() {
        if(this.ticksSinceUpdate >= UPDATE_STATUS_EVERY_TICKS) {
            this.midiInputManager.enderTransmitterManager.startRefreshMediaStatus();
            this.ticksSinceUpdate = 0;
        } else {
            this.ticksSinceUpdate++;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(this.midiInputManager.enderTransmitterManager.isLocalMode()) {
            if(this.folderPathString != null && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(LOAD_FOLDER_BUTTON))) {
                this.midiInputManager.enderTransmitterManager.loadLocalSongsFromFolder(this.folderPathString);
            } else if(this.folderPathString != null && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SAVE_DEFAULT_BUTTON))) {
                ModConfigs.CLIENT.playlistFolderPath.set(this.folderPathString);
            } else if(ModConfigs.CLIENT.playlistFolderPath.get() != null && !ModConfigs.CLIENT.playlistFolderPath.get().isEmpty() && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(LOAD_DEFAULT_BUTTON))) {
                this.folderPathString = ModConfigs.CLIENT.playlistFolderPath.get();
                this.folderPathField.setValue(this.folderPathString);
                this.midiInputManager.enderTransmitterManager.loadLocalSongsFromFolder(this.folderPathString);
            }
        }
        
        if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(TOGGLE_MODE_BUTTON))) {
            this.midiInputManager.enderTransmitterManager.toggleLocalMode();
            this.folderPathField.setVisible(this.midiInputManager.enderTransmitterManager.isLocalMode());
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(PREVIOUS_BUTTON))) {
            Double slidePercentage = null;

            if(this.midiInputManager.enderTransmitterManager.songLoaded()) {
                slidePercentage =  Double.valueOf(this.midiInputManager.enderTransmitterManager.getSongPositionSeconds()) / Double.valueOf(this.midiInputManager.enderTransmitterManager.getCurrentModeSelectedSongInfo().songLength);
            }

            if(slidePercentage != null && slidePercentage >= 0.25) {
                this.midiInputManager.enderTransmitterManager.seek(0);
            } else {
                this.midiInputManager.enderTransmitterManager.selectPreviousSong();
            }
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(STOP_BUTTON))) {
                this.midiInputManager.enderTransmitterManager.stop();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(PLAY_PAUSE_BUTTON))) {
            if(this.midiInputManager.enderTransmitterManager.isPlaying()) {
                this.midiInputManager.enderTransmitterManager.pause();
            } else {
                this.midiInputManager.enderTransmitterManager.play();
            }
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(NEXT_BUTTON))) {
            this.midiInputManager.enderTransmitterManager.selectNextSong();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(LOOP_BUTTON))) {
            this.midiInputManager.enderTransmitterManager.shiftLoopMode();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHUFFLE_BUTTON))) {
            this.midiInputManager.enderTransmitterManager.toggleShuffle();
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @SuppressWarnings("null")
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
           this.minecraft.player.closeContainer();
        }
  
        return !this.folderPathField.keyPressed(keyCode, scanCode, modifiers) && !this.folderPathField.canConsumeInput() ? super.keyPressed(keyCode, scanCode, modifiers) : true;
    }

    @Override
    protected  GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // Background
        graphics.blit(guiTexture, START_X, START_Y, 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Local Mode Overlay
        if(this.midiInputManager.enderTransmitterManager.isLocalMode()) {
            graphics.blit(guiTexture, START_X + 6, START_Y + 29, 1, 316, 348, 44, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Selected Song Box
        if(this.midiInputManager.enderTransmitterManager.getCurrentModeHasSongs()) {
            Integer songOffset = getSongOffset();            
            Integer boxY = (getFirstSongY() - 2) + (11 * songOffset);
            graphics.blit(guiTexture, START_X + 10, START_Y + boxY, 1, 290, 340, 11, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Play/Pause Button
        graphics.blit(guiTexture, START_X + 49, START_Y + 266, this.midiInputManager.enderTransmitterManager.isPlaying() ? 105 : 92, 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Loop Screen
        graphics.blit(guiTexture, START_X + Float.valueOf(LOOP_SCREEN.x()).intValue(), START_Y + Float.valueOf(LOOP_SCREEN.y()).intValue(), 1 + (13 * this.midiInputManager.enderTransmitterManager.getLoopMode()), 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Shuffle Screen    
        graphics.blit(guiTexture, START_X + Float.valueOf(SHUFFLE_SCREEN.x()).intValue(), START_Y + Float.valueOf(SHUFFLE_SCREEN.y()).intValue(), 40 + (13 * this.midiInputManager.enderTransmitterManager.getShuffleMode()), 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Time Slider
        Integer slideOffset = 0;
        if(this.midiInputManager.enderTransmitterManager.songLoaded()) {
            Integer slideLength = this.midiInputManager.enderTransmitterManager.getCurrentModeSelectedSongInfo().songLength;
            Integer slideProgress = this.midiInputManager.enderTransmitterManager.getSongPositionSeconds();
            Double slidePercentage =  Double.valueOf(slideProgress) / Double.valueOf(slideLength);
            slideOffset = Double.valueOf(Math.floor(slidePercentage * SLIDE_WIDTH)).intValue();
        }

        graphics.blit(guiTexture, START_X + SLIDE_MIN_X + slideOffset, START_Y + SLIDE_Y, 352, 290, 7, 17, TEXTURE_SIZE, TEXTURE_SIZE);

        // Text Field
        this.folderPathField.render(graphics, mouseX, mouseY, partialTicks);
        
        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Playlist
        if(this.midiInputManager.enderTransmitterManager.getCurrentModeHasSongs()) {
            Integer minSong = getMinSong();

            for(int i = 0; i < getVisibleSongs(); i++) {
                if(this.midiInputManager.enderTransmitterManager.getCurrentModeSongCount() > (minSong + i)) {
                    MidiFileInfo info = this.midiInputManager.enderTransmitterManager.getCurrentModeSongList().get(minSong + i);
                    graphics.drawString(font, (minSong + i + 1) + "). " + (info.file.getName().length() > 50 ? info.file.getName().substring(0,50) + "..." : info.file.getName()), START_X + 12, START_Y + getFirstSongY() + i * 11, 0xFF00E600);
                } else {
                    break;
                }            
            }
        }

        // Current Song
        MidiFileInfo info = this.midiInputManager.enderTransmitterManager.getCurrentModeSelectedSongInfo();

        if(info != null) {
            graphics.drawString(font, "Channel Instruments: ", START_X + 12, START_Y + 168, 0xFF00E600);
            
            Integer index = 0;
            for(Integer i = 0; i < 16; i+=2) {
                String name = info.instrumentMapping.get(i) == null ? "None" : info.instrumentMapping.get(i);
                graphics.drawString(font, (i+1) + ": " + name, START_X + 12, START_Y + 182 + 10*index, 0xFF00E600);
                index++;
            }

            index = 0;
            for(Integer i = 1; i < 16; i+=2) {
                String name = info.instrumentMapping.get(i) == null ? "None" : info.instrumentMapping.get(i);
                graphics.drawString(font, (i+1) + ": " + name, START_X + 180, START_Y + 182 + 10*index, 0xFF00E600);
                index++;
            }
        }
        
        return graphics;
    }

    private Integer getVisibleSongs() {
        return this.midiInputManager.enderTransmitterManager.isLocalMode() ? 6 : 8;
    }

    private Integer getFirstSongY() {
        return this.midiInputManager.enderTransmitterManager.isLocalMode() ? 73 : 51;
    }

    private Integer getMinSong() {
        if(this.midiInputManager.enderTransmitterManager.getCurrentModeSongCount() <= getVisibleSongs() || this.midiInputManager.enderTransmitterManager.getCurrentModeSelectedSong() < 3) {
            return 0;
        } else if(this.midiInputManager.enderTransmitterManager.getCurrentModeSelectedSong() > this.midiInputManager.enderTransmitterManager.getCurrentModeSongCount() - 3) {
            return this.midiInputManager.enderTransmitterManager.getCurrentModeSongCount() - getVisibleSongs();
        } else {
            return this.midiInputManager.enderTransmitterManager.getCurrentModeSelectedSong() - 3;
        }
    }

    private Integer getSongOffset() {
        if(this.midiInputManager.enderTransmitterManager.getCurrentModeSongCount() <= getVisibleSongs() || this.midiInputManager.enderTransmitterManager.getCurrentModeSelectedSong() < 3) {
            return this.midiInputManager.enderTransmitterManager.getCurrentModeSelectedSong();
        } else if(this.midiInputManager.enderTransmitterManager.getCurrentModeSelectedSong() > this.midiInputManager.enderTransmitterManager.getCurrentModeSongCount() - 3) {
            return getVisibleSongs() - (this.midiInputManager.enderTransmitterManager.getCurrentModeSongCount() - this.midiInputManager.enderTransmitterManager.getCurrentModeSelectedSong());
        } else {
            return 3;
        }
    }

    protected void handlePathChange(String folderPath) {
        if(folderPath != null && !folderPath.trim().isEmpty()) {
            try {
                if(Files.isDirectory(Paths.get(folderPath), LinkOption.NOFOLLOW_LINKS)) {
                    this.folderPathString = folderPath.trim();
                    this.folderPathField.setTextColor(DEFAULT_TEXT_FIELD_COLOR);
                } else {
                    throw new RuntimeException("Folder not found: " + folderPath);
                }
            } catch(Exception e) {
                this.folderPathString = null;
                this.folderPathField.setTextColor(13112340);
            }
        } else {
            this.folderPathString = null;
            this.folderPathField.setTextColor(13112340);
        }
    }
}