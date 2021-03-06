package io.github.tofodroid.mods.mimi.client.gui;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.tofodroid.mods.mimi.client.midi.MidiFileInfo;
import io.github.tofodroid.mods.mimi.client.midi.MidiInputManager;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.StringTextComponent;

public class GuiMidiPlaylist extends BaseGui {
    // GUI
    private static final Integer DEFAULT_TEXT_FIELD_COLOR = 14737632;
    private TextFieldWidget folderPathField;

    // Data
    private String folderPathString;
    
    // Button Boxes
    private static final Vector2f LOAD_FOLDER_BUTTON = new Vector2f(301,37);
    private static final Vector2f SAVE_DEFAULT_BUTTON = new Vector2f(320,37);
    private static final Vector2f LOAD_DEFAULT_BUTTON = new Vector2f(339,37);
    private static final Vector2f PREVIOUS_BUTTON = new Vector2f(14,271);
    private static final Vector2f STOP_BUTTON = new Vector2f(33,271);
    private static final Vector2f PLAY_PAUSE_BUTTON = new Vector2f(52,271);
    private static final Vector2f NEXT_BUTTON = new Vector2f(71,271);
    private static final Vector2f LOOP_BUTTON = new Vector2f(90,271);
    private static final Vector2f LOOP_SCREEN = new Vector2f(108,272);
    private static final Vector2f SHUFFLE_BUTTON = new Vector2f(125,271);
    private static final Vector2f SHUFFLE_SCREEN = new Vector2f(143,272);
    private static final Vector2f TRANSMIT_BUTTON = new Vector2f(160,271);
    private static final Vector2f TRANSMIT_SCREEN = new Vector2f(178,272);

    // Time Slider
    private static final Integer SLIDE_Y = 270;
    private static final Integer SLIDE_MIN_X = 205;
    private static final Integer SLIDE_MAX_X = 339;
    private static final Integer SLIDE_WIDTH = SLIDE_MAX_X - SLIDE_MIN_X;

    // MIDI
    private MidiInputManager midiInputManager;
    
    public GuiMidiPlaylist(PlayerEntity player) {
        super(368, 300, 400, "textures/gui/gui_midi_playlist.png", "item.MIMIMod.gui_midi_playlist");
        this.midiInputManager = (MidiInputManager)MIMIMod.proxy.getMidiInput();
        this.folderPathString = this.midiInputManager.playlistManager.getPlaylistFolderPath();
    }

    @Override
    public void init() {
        super.init();

        // Fields
        folderPathField = this.addListener(new TextFieldWidget(this.font, this.START_X + 90, this.START_Y + 40, 207, 10, StringTextComponent.EMPTY));
        folderPathField.setText(folderPathString);
        folderPathField.setMaxStringLength(256);
        folderPathField.setResponder(this::handlePathChange);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(this.folderPathString != null && clickedBox(imouseX, imouseY, LOAD_FOLDER_BUTTON)) {
            this.midiInputManager.playlistManager.loadFromFolder(this.folderPathString);
        } else if(this.folderPathString != null && clickedBox(imouseX, imouseY, SAVE_DEFAULT_BUTTON)) {
            ModConfigs.CLIENT.playlistFolderPath.set(this.folderPathString);
        } else if(ModConfigs.CLIENT.playlistFolderPath.get() != null && !ModConfigs.CLIENT.playlistFolderPath.get().isEmpty() && clickedBox(imouseX, imouseY, LOAD_DEFAULT_BUTTON)) {
            this.folderPathString = ModConfigs.CLIENT.playlistFolderPath.get();
            this.folderPathField.setText(this.folderPathString);
            this.midiInputManager.playlistManager.loadFromFolder(this.folderPathString);
        } else if(clickedBox(imouseX, imouseY, PREVIOUS_BUTTON)) {
            Double slidePercentage = null;

            if(this.midiInputManager.playlistManager.isSongLoaded()) {
                slidePercentage =  new Double(this.midiInputManager.playlistManager.getCurrentSongPosSeconds()) / new Double(this.midiInputManager.playlistManager.getSongLengthSeconds());
            }

            if(slidePercentage != null && slidePercentage >= 0.25) {
                this.midiInputManager.playlistManager.playFromBeginning();
            } else {
                this.midiInputManager.playlistManager.shiftSong(false);
            }
        } else if(clickedBox(imouseX, imouseY, STOP_BUTTON)) {
            this.midiInputManager.playlistManager.stop();
        } else if(clickedBox(imouseX, imouseY, PLAY_PAUSE_BUTTON)) {
            if(this.midiInputManager.playlistManager.isPlaying()) { 
                this.midiInputManager.playlistManager.pause();
            } else {
                this.midiInputManager.playlistManager.playFromLastTickPosition();
            }            
        } else if(clickedBox(imouseX, imouseY, NEXT_BUTTON)) {
            this.midiInputManager.playlistManager.shiftSong(true);
        } else if(clickedBox(imouseX, imouseY, LOOP_BUTTON)) {
            this.midiInputManager.playlistManager.shiftLoopMode();
        } else if(clickedBox(imouseX, imouseY, SHUFFLE_BUTTON)) {
            this.midiInputManager.playlistManager.toggleShuffle();
        } else if(clickedBox(imouseX, imouseY, TRANSMIT_BUTTON)) {
            this.midiInputManager.playlistManager.shiftTransmitMode();
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
           this.minecraft.player.closeScreen();
        }
  
        return !this.folderPathField.keyPressed(keyCode, scanCode, modifiers) && !this.folderPathField.canWrite() ? super.keyPressed(keyCode, scanCode, modifiers) : true;
    }

    @Override
    protected  MatrixStack renderGraphics(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        Minecraft.getInstance().getTextureManager().bindTexture(guiTexture);

        // Background
        blit(matrixStack, START_X, START_Y, this.getBlitOffset(), 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Selected Song Box
        if(this.midiInputManager.playlistManager.isSongSelected()) {
            Integer songOffset;

            if(this.midiInputManager.playlistManager.getSelectedSongIndex() < 3) {
                songOffset = this.midiInputManager.playlistManager.getSelectedSongIndex();
            } else if(this.midiInputManager.playlistManager.getSelectedSongIndex() > this.midiInputManager.playlistManager.getSongCount() - 3) {
                songOffset = 6 - (this.midiInputManager.playlistManager.getSongCount() - this.midiInputManager.playlistManager.getSelectedSongIndex());
            } else {
                songOffset = 3;
            }
            
            Integer boxY = 82 + 10 * songOffset;
            blit(matrixStack, START_X + 15, START_Y + boxY, this.getBlitOffset(), 1, 301, 338, 11, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Play/Pause Button
        blit(matrixStack, START_X + 53, START_Y + 272, this.getBlitOffset(), 1 + this.midiInputManager.playlistManager.isPlaying().compareTo(false) * 13, 355, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Loop Screen
        blit(matrixStack, START_X + new Float(LOOP_SCREEN.x).intValue(), START_Y + new Float(LOOP_SCREEN.y).intValue(), this.getBlitOffset(), 1 + (13 * this.midiInputManager.playlistManager.getLoopMode()), 313, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Shuffle Screen    
        blit(matrixStack, START_X + new Float(SHUFFLE_SCREEN.x).intValue(), START_Y + new Float(SHUFFLE_SCREEN.y).intValue(), this.getBlitOffset(), 1 + (13 * this.midiInputManager.playlistManager.getShuffleMode()), 327, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        
        // Transmit Screen    
        blit(matrixStack, START_X + new Float(TRANSMIT_SCREEN.x).intValue(), START_Y + new Float(TRANSMIT_SCREEN.y).intValue(), this.getBlitOffset(), 1 + (13 * this.midiInputManager.playlistManager.getTransmitModeInt()), 341, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Time Slider
        Integer slideOffset = 0;
        if(this.midiInputManager.playlistManager.isSongLoaded()) {
            Integer slideLength = this.midiInputManager.playlistManager.getSongLengthSeconds();
            Integer slideProgress = this.midiInputManager.playlistManager.getCurrentSongPosSeconds();
            Double slidePercentage =  new Double(slideProgress) / new Double(slideLength);
            slideOffset = new Double(Math.floor(slidePercentage * SLIDE_WIDTH)).intValue();
        }

        blit(matrixStack, START_X + SLIDE_MIN_X + slideOffset, START_Y + SLIDE_Y, this.getBlitOffset(), 43, 313, 7, 17, TEXTURE_SIZE, TEXTURE_SIZE);

        // Text Field
        this.folderPathField.render(matrixStack, mouseX, mouseY, partialTicks);
        
        return matrixStack;
    }

    @Override
    protected MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Playlist
        if(this.midiInputManager.playlistManager.getSongCount() > 0) {
            Integer minSong;
            if(this.midiInputManager.playlistManager.getSelectedSongIndex() < 3) {
                minSong = 0;
            } else if(this.midiInputManager.playlistManager.getSelectedSongIndex() > this.midiInputManager.playlistManager.getSongCount() - 3) {
                minSong = this.midiInputManager.playlistManager.getSongCount() - 6;
            } else {
                minSong = this.midiInputManager.playlistManager.getSelectedSongIndex() - 3;
            }

            for(int i = 0; i < 6; i++) {
                if(this.midiInputManager.playlistManager.getSongCount() > (minSong + i)) {
                    MidiFileInfo info = this.midiInputManager.playlistManager.getLoadedPlaylist().get(minSong + i);
                    this.font.drawString(matrixStack, (minSong + i + 1) + "). " + (info.fileName.length() > 50 ? info.fileName.substring(0,50) + "..." : info.fileName), START_X + 18, START_Y + 84 + i * 10, 0xFF00E600);
                } else {
                    break;
                }            
            }
        }

        // Current Song
        MidiFileInfo info = this.midiInputManager.playlistManager.getSelectedSongInfo();
        if(info != null) {
            this.font.drawString(matrixStack, "Channel Instruments: ", START_X + 16, START_Y + 174, 0xFF00E600);
            
            Integer index = 0;
            for(Integer i = 0; i < 16; i+=2) {
                String name = info.instrumentMapping.get(i) == null ? "None" : info.instrumentMapping.get(i);
                this.font.drawString(matrixStack, (i+1) + ": " + name, START_X + 16, START_Y + 188 + 10*index, 0xFF00E600);
                index++;
            }

            index = 0;
            for(Integer i = 1; i < 16; i+=2) {
                String name = info.instrumentMapping.get(i) == null ? "None" : info.instrumentMapping.get(i);
                this.font.drawString(matrixStack, (i+1) + ": " + name, START_X + 184, START_Y + 188 + 10*index, 0xFF00E600);
                index++;
            }
        }
        
        return matrixStack;
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