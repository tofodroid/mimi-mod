package io.github.tofodroid.mods.mimi.client.gui;

import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2i;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.client.midi.MidiInputManager;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class GuiMidiInputConfig extends BaseGui {    
    // Button Boxes
    private static final Vector2i CLEAR_DEVICE_BUTTON = new Vector2i(272,36);
    private static final Vector2i REFRESH_DEVICES_BUTTON = new Vector2i(271,94);
    private static final Vector2i SHIFT_DEVICE_DOWN_BUTTON = new Vector2i(110,94);
    private static final Vector2i SHIFT_DEVICE_UP_BUTTON = new Vector2i(252,94);
    private static final Vector2i SAVE_DEVICE_BUTTON = new Vector2i(271,144);

    // MIDI
    private MidiInputManager midiInputManager;
    private List<MidiDevice> availableDevices;
    private Integer visibleDeviceId = 0;

    public GuiMidiInputConfig(Player player) {
        super(300, 173, 300, "textures/gui/gui_midi_config.png",  "item.MIMIMod.gui_midi_input_config");
        this.midiInputManager = ((ClientProxy)MIMIMod.proxy).getMidiInput();
        availableDevices = this.midiInputManager.inputDeviceManager.getAvailableDevices();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(REFRESH_DEVICES_BUTTON))) {
            visibleDeviceId = 0;
            availableDevices = this.midiInputManager.inputDeviceManager.getAvailableDevices();
        } else if(this.midiInputManager.inputDeviceManager.isDeviceSelected() && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(CLEAR_DEVICE_BUTTON))) {
            this.midiInputManager.inputDeviceManager.clearDeviceSelection();
        } else if(this.availableDevices != null && this.availableDevices.size() > visibleDeviceId && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SAVE_DEVICE_BUTTON))) {
            this.midiInputManager.inputDeviceManager.saveDeviceSelection(availableDevices.get(visibleDeviceId));
        } else if(this.availableDevices != null && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHIFT_DEVICE_UP_BUTTON))) {
            visibleDeviceId = visibleDeviceId < (this.availableDevices.size() - 1) ? visibleDeviceId + 1 : visibleDeviceId;
        } else if(this.availableDevices != null  && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHIFT_DEVICE_DOWN_BUTTON))) {
            visibleDeviceId = visibleDeviceId > 0 ? visibleDeviceId - 1 : visibleDeviceId;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // Background
        graphics.blit(guiTexture, START_X, START_Y, 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Device Status Light
        Integer statusX = START_X + 265;
        Integer statusY = START_Y + 42;

        if(this.midiInputManager.inputDeviceManager.isDirtyStatus()) {
                graphics.blit(guiTexture, statusX, statusY, 8, 159, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
        } else if(this.midiInputManager.inputDeviceManager.isDeviceSelected()) {
            graphics.blit(guiTexture, statusX, statusY, this.midiInputManager.inputDeviceManager.isDeviceAvailable() ? 0 : 4, 159, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        
        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Selected Device Name
        if(this.midiInputManager.inputDeviceManager.isDeviceSelected()) {
            graphics.drawString(font, this.midiInputManager.inputDeviceManager.getSelectedDeviceName(), START_X + 123, START_Y + 40, 0xFF00E600);
        }

        // Available Device Info
        if(this.availableDevices != null && this.availableDevices.size() > visibleDeviceId) {
            graphics.drawString(font, visibleDeviceId + ": " + this.availableDevices.get(visibleDeviceId).getDeviceInfo().getName(), START_X + 131, START_Y + 98, 0xFF00E600);
            Info info = this.availableDevices.get(visibleDeviceId).getDeviceInfo();
            if(info != null) {
                String descString = "Description: " + info.getDescription();
                Integer yOffset = 0;

                if(descString.length() <= 45) {
                    graphics.drawString(font, descString, START_X + 16, START_Y + 115, 0xFF00E600);
                } else {
                    yOffset = 16;
                    graphics.drawString(font, descString.substring(0, 45), START_X + 16, START_Y + 115, 0xFF00E600);
                    graphics.drawString(font, descString.substring(45), START_X + 16, START_Y + 131, 0xFF00E600);
                }

                graphics.drawString(font, "Vendor: " + info.getVendor(), START_X + 16, START_Y + yOffset + 131, 0xFF00E600);
                graphics.drawString(font, "Version: " + info.getVersion(), START_X + 16, START_Y + yOffset + 147, 0xFF00E600);  
            }
        }
        
        
        return graphics;
    }
}