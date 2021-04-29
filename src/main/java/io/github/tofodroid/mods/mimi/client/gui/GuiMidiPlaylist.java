package io.github.tofodroid.mods.mimi.client.gui;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.tofodroid.mods.mimi.client.midi.MidiFileInfo;
import io.github.tofodroid.mods.mimi.client.midi.MidiInputManager;
import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiMidiPlaylist extends Screen {
    // Texture
    private static final ResourceLocation guiTexture = new ResourceLocation(MIMIMod.MODID, "textures/gui/gui_midi_playlist.png");
    private static final Integer GUI_WIDTH = 368;
    private static final Integer GUI_HEIGHT = 300;
    private static final Integer TEXTURE_SIZE = 400;
    private static final Integer BUTTON_SIZE = 15;
    
    // GUI
    private static final Integer DEFAULT_TEXT_FIELD_COLOR = 14737632;

    private Integer startX;
    private Integer startY;
    private TextFieldWidget folderPathField;

    // Data
    private String folderPathString;
    
    // Button Boxes
    private static final Vector2f REFRESH_FOLDER_BUTTON = new Vector2f(339,37);
    private static final Vector2f PREVIOUS_BUTTON = new Vector2f(14,271);
    private static final Vector2f RESTART_BUTTON = new Vector2f(33,271);
    private static final Vector2f STOP_BUTTON = new Vector2f(52,271);
    private static final Vector2f PAUSE_BUTTON = new Vector2f(71,271);
    private static final Vector2f PLAY_BUTTON = new Vector2f(90,271);
    private static final Vector2f NEXT_BUTTON = new Vector2f(109,271);
    private static final Vector2f LOOP_BUTTON = new Vector2f(128,271);
    private static final Vector2f SHUFFLE_BUTTON = new Vector2f(147,271);

    // Time Slider
    private static final Integer SLIDE_Y = 270;
    private static final Integer SLIDE_MIN_X = 190;
    private static final Integer SLIDE_MAX_X = 324;
    private static final Integer SLIDE_WIDTH = SLIDE_MAX_X - SLIDE_MIN_X;

    // MIDI
    private MidiInputManager midiInputManager;
    
    public GuiMidiPlaylist(PlayerEntity player) {
        super(new TranslationTextComponent("item.MIMIMod.gui_midi_playllist"));
        this.midiInputManager = MIMIMod.proxy.getMidiInput();
        this.folderPathString = this.midiInputManager.playlistManager.getPlaylistFolderPath();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        startX = (this.width - GUI_WIDTH) / 2;
        startY = (this.height - GUI_HEIGHT) / 2;

        // Fields
        folderPathField = this.addListener(new TextFieldWidget(this.font, this.startX + 118, this.startY + 40, 216, 10, StringTextComponent.EMPTY));
        folderPathField.setText(folderPathString);
        folderPathField.setMaxStringLength(256);
        folderPathField.setResponder(this::handlePathChange);
    }

    @Override
    public void closeScreen() {
        super.closeScreen();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack = renderGraphics(matrixStack, mouseX, mouseY, partialTicks);
        matrixStack = renderText(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(clickedBox(imouseX, imouseY, REFRESH_FOLDER_BUTTON)) {
            this.midiInputManager.playlistManager.loadFromFolder(this.folderPathString);
        } else if(clickedBox(imouseX, imouseY, PREVIOUS_BUTTON)) {
            this.midiInputManager.playlistManager.shiftSong(false);
        } else if(clickedBox(imouseX, imouseY, RESTART_BUTTON)) {
            this.midiInputManager.playlistManager.playFromBeginning();
        } else if(clickedBox(imouseX, imouseY, STOP_BUTTON)) {
            this.midiInputManager.playlistManager.stop();
        } else if(clickedBox(imouseX, imouseY, PAUSE_BUTTON)) {
            this.midiInputManager.playlistManager.pause();
        } else if(clickedBox(imouseX, imouseY, PLAY_BUTTON)) {
            this.midiInputManager.playlistManager.playFromLastTickPosition();
        } else if(clickedBox(imouseX, imouseY, NEXT_BUTTON)) {
            this.midiInputManager.playlistManager.shiftSong(true);
        } else if(clickedBox(imouseX, imouseY, LOOP_BUTTON)) {
            this.midiInputManager.playlistManager.shiftLoopMode();
        } else if(clickedBox(imouseX, imouseY, SHUFFLE_BUTTON)) {
            this.midiInputManager.playlistManager.toggleShuffle();
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

    private Boolean clickedBox(Integer mouseX, Integer mouseY, Vector2f buttonPos) {
        Integer buttonMinX = startX + new Float(buttonPos.x).intValue();
        Integer buttonMaxX = buttonMinX + BUTTON_SIZE;
        Integer buttonMinY = startY + new Float(buttonPos.y).intValue();
        Integer buttonMaxY = buttonMinY + BUTTON_SIZE;

        Boolean result = mouseX >= buttonMinX && mouseX <= buttonMaxX && mouseY >= buttonMinY && mouseY <= buttonMaxY;

        if(result) {
            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        return result;
    }

    private MatrixStack renderGraphics(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        Minecraft.getInstance().getTextureManager().bindTexture(guiTexture);

        // Background
        blit(matrixStack, startX, startY, this.getBlitOffset(), 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

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
            blit(matrixStack, startX + 15, startY + boxY, this.getBlitOffset(), 1, 301, 338, 11, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Loop Button
        blit(matrixStack, startX + 129, startY + 272, this.getBlitOffset(), 1 + (13 * this.midiInputManager.playlistManager.getLoopMode()), 313, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Shuffle Button    
        blit(matrixStack, startX + 148, startY + 272, this.getBlitOffset(), 1 + (13 * this.midiInputManager.playlistManager.getShuffleMode()), 327, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Time Slider
        Integer slideOffset = 0;
        if(this.midiInputManager.playlistManager.isSongLoaded()) {
            Integer slideLength = this.midiInputManager.playlistManager.getSongLengthSeconds();
            Integer slideProgress = this.midiInputManager.playlistManager.getCurrentSongPosSeconds();
            Double slidePercentage =  new Double(slideProgress) / new Double(slideLength);
            slideOffset = new Double(Math.floor(slidePercentage * SLIDE_WIDTH)).intValue();
        }

        blit(matrixStack, startX + SLIDE_MIN_X + slideOffset, startY + SLIDE_Y, this.getBlitOffset(), 43, 313, 7, 17, TEXTURE_SIZE, TEXTURE_SIZE);

        // Text Field
        this.folderPathField.render(matrixStack, mouseX, mouseY, partialTicks);
        
        return matrixStack;
    }

    private MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
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
                    this.font.drawString(matrixStack, (minSong + i + 1) + "). " + (info.fileName.length() > 50 ? info.fileName.substring(0,50) + "..." : info.fileName), startX + 18, startY + 84 + i * 10, 0xFF00E600);
                } else {
                    break;
                }            
            }
        }

        // Current Song
        MidiFileInfo info = this.midiInputManager.playlistManager.getSelectedSongInfo();
        if(info != null) {
            this.font.drawString(matrixStack, "Channel Instruments: ", startX + 16, startY + 174, 0xFF00E600);
            
            Integer index = 0;
            for(Integer i = 0; i < 16; i+=2) {
                String name = info.instrumentMapping.get(i) == null ? "None" : info.instrumentMapping.get(i);
                this.font.drawString(matrixStack, (i+1) + ": " + name, startX + 16, startY + 188 + 10*index, 0xFF00E600);
                index++;
            }

            index = 0;
            for(Integer i = 1; i < 16; i+=2) {
                String name = info.instrumentMapping.get(i) == null ? "None" : info.instrumentMapping.get(i);
                this.font.drawString(matrixStack, (i+1) + ": " + name, startX + 184, startY + 188 + 10*index, 0xFF00E600);
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