package io.github.tofodroid.mods.mimi.client.gui;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.util.Vector2Int;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ConfigProxy;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.common.network.ClientMidiListPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacket.CONTROL;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerSongListPacket;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;

public class GuiTransmitterItem extends GuiTransmitterBlock {
    private static final Integer DEFAULT_TEXT_FIELD_COLOR = 14737632;

    // System MIDI Input Control
    protected static final Vector2Int SYSTEM_MIDI_DEVICE_BUTTON = new Vector2Int(330,7);
    protected static final Vector2Int SYSTEM_MIDI_DEVICE_LIGHT = new Vector2Int(349,13);

    // Local File Controls
    protected static final Vector2Int SOURCE_FILTER_BUTTON = new Vector2Int(265,32);
    protected static final Vector2Int SOURCE_FILTER_SCREEN = new Vector2Int(283,33);
    protected static final Vector2Int OPEN_LOCAL_FOLDER_BUTTON = new Vector2Int(10,32);
    protected static final Vector2Int EDIT_LOCAL_FOLDER_BUTTON = new Vector2Int(28, 32);
    
    // Widgets
    private EditBox folderPathField;

    // Data
    private Boolean editMode = false;
    private String folderPathString;
    

    public GuiTransmitterItem(UUID musicPlayerId) {
        super(musicPlayerId);
        this.folderPathString = ConfigProxy.getTransmitterMidiPath();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
           this.minecraft.player.closeContainer();
        }

        return !this.folderPathField.keyPressed(keyCode, scanCode, modifiers) && !this.folderPathField.canConsumeInput() ? super.keyPressed(keyCode, scanCode, modifiers) : true;
    }

    @Override
    public void init() {
        super.init();
        
        // Fields
        folderPathField = this.addWidget(new EditBox(this.font, this.START_X + 45, this.START_Y + 32, 217, 15, CommonComponents.EMPTY));
        folderPathField.setValue(folderPathString);
        folderPathField.setMaxLength(256);
        folderPathField.setResponder(this::handlePathChange);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);
        
        if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SYSTEM_MIDI_DEVICE_BUTTON))) {
            ((ClientProxy)MIMIMod.getProxy()).getMidiData().inputDeviceManager.toggleTransmitMidiInput();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SOURCE_FILTER_BUTTON))) {
            this.sendTransmitterCommand(CONTROL.SOURCE_M);
        } else if(!editMode) {
            if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(OPEN_LOCAL_FOLDER_BUTTON))) {
                Util.getPlatform().openUri(Path.of(MIMIMod.getProxy().clientMidiFiles().getCurrentFolderPath()).toUri());
            } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(EDIT_LOCAL_FOLDER_BUTTON))) {
                this.editMode = true;
            }
        } else {
            if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(OPEN_LOCAL_FOLDER_BUTTON)) /* Becomes Cancel */) {
                this.folderPathString = ConfigProxy.getTransmitterMidiPath();
                this.editMode = false;
            } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(EDIT_LOCAL_FOLDER_BUTTON)) /* Becomes Save */) {
                if(this.folderPathString != null) {
                    ConfigProxy.setTransmitterMidiPath(this.folderPathString);
                    MIMIMod.getProxy().clientMidiFiles().setDirectory(this.folderPathString);
                    MIMIMod.getProxy().clientMidiFiles().refresh(true);
                    NetworkProxy.sendToServer(new ClientMidiListPacket(MIMIMod.getProxy().clientMidiFiles().getSortedSongInfos()));
                } else {
                    ConfigProxy.setTransmitterMidiPath("");
                    MIMIMod.getProxy().clientMidiFiles().init();
                    MIMIMod.getProxy().clientMidiFiles().refresh(true);
                    NetworkProxy.sendToServer(new ClientMidiListPacket(MIMIMod.getProxy().clientMidiFiles().getSortedSongInfos()));
                }
                this.editMode = false;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected  GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderGraphics(graphics, mouseX, mouseY, partialTicks);

        // System MIDI Input Device Background and Light
        this.blitAbsolute(graphics, guiTexture, START_X + 240, START_Y + 4, 1, 327, 116, 21, TEXTURE_SIZE, TEXTURE_SIZE);

        if(((ClientProxy)MIMIMod.getProxy()).getMidiData().inputDeviceManager.getTransmitMidiInput()) {
            this.blitAbsolute(graphics, guiTexture, START_X + SYSTEM_MIDI_DEVICE_LIGHT.x(), START_Y + SYSTEM_MIDI_DEVICE_LIGHT.y(), 1, 349, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        
        if(!this.editMode) {
            // Local Folder Button
            this.blitAbsolute(graphics, guiTexture, START_X + 9, START_Y + 31, 173, 269, 17, 17, TEXTURE_SIZE, TEXTURE_SIZE);

            // Edit Local Folder Button
            this.blitAbsolute(graphics, guiTexture, START_X + 27, START_Y + 31, 225, 269, 17, 17, TEXTURE_SIZE, TEXTURE_SIZE);
        } else {
            // Cancel Button
            this.blitAbsolute(graphics, guiTexture, START_X + 9, START_Y + 31, 243, 269, 17, 17, TEXTURE_SIZE, TEXTURE_SIZE);

            // Save Button
            this.blitAbsolute(graphics, guiTexture, START_X + 27, START_Y + 31, 261, 269, 17, 17, TEXTURE_SIZE, TEXTURE_SIZE);

            // Input
            this.folderPathField.render(graphics, mouseX, mouseY, partialTicks);
        }

        // Source Filter Button & Screen Background
        this.blitAbsolute(graphics, guiTexture, START_X + 264, START_Y + 31, 191, 269, 33, 17, TEXTURE_SIZE, TEXTURE_SIZE);

        // Source Filter Screen
        this.blitAbsolute(graphics, guiTexture, START_X + 283, START_Y + 33, 105 + (this.musicStatus.sourceMode.ordinal() * 13), 269, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        
        return graphics;
    }

    @Override
    protected Integer maxPlaylistSongTitleWidth() {
        return 318;
    }
    
    @Override
    public Boolean isSinglePlayerOrLANHost() {
        // Always return false because this GUI always shows the local folder button in the same spot
        return false;
    }
    
    @Override
    protected GuiGraphics renderPlaylistSongBadges(GuiGraphics graphics, BasicMidiInfo info, Integer songIndex, Integer minSong) {
        super.renderPlaylistSongBadges(graphics, info, songIndex, minSong);

        // Source Badge
        this.blitAbsolute(graphics, guiTexture, START_X + 329, START_Y + getFirstSongY() - 1 + songIndex * 11, !info.serverMidi ? 163 : 154, 269, 9, 9, TEXTURE_SIZE, TEXTURE_SIZE);

        return graphics;
    }
    
    @Override
    protected void startRefreshSongList() {
        MIMIMod.getProxy().clientMidiFiles().refresh(true);
        NetworkProxy.sendToServer(new ClientMidiListPacket(MIMIMod.getProxy().clientMidiFiles().getSortedSongInfos()));
        NetworkProxy.sendToServer(new ServerMusicPlayerSongListPacket(musicPlayerId));
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