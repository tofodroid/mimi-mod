package io.github.tofodroid.mods.mimi.client.gui;

import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;

import io.github.tofodroid.mods.mimi.util.Vector2Int;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.client.midi.MidiDataManager;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class GuiMidiInputConfig extends BaseGui {    
    // Button Boxes
    private static final Vector2Int REFRESH_DEVICES_BUTTON = new Vector2Int(266,96);
    private static final Vector2Int SHIFT_DEVICE_DOWN_BUTTON = new Vector2Int(8,96);
    private static final Vector2Int SHIFT_DEVICE_UP_BUTTON = new Vector2Int(247,96);
    private static final Vector2Int SAVE_DEVICE_BUTTON = new Vector2Int(266,146);

    // MIDI
    private MidiDataManager midiDataManager;
    private List<MidiDevice> availableDevices;
    private Integer visibleDeviceId = 0;

    public GuiMidiInputConfig(Player player) {
        super(288, 169, 288, "textures/gui/gui_midi_config.png",  "item.MIMIMod.gui_midi_input_config");
        this.midiDataManager = ((ClientProxy)MIMIMod.getProxy()).getMidiData();
        availableDevices = this.midiDataManager.inputDeviceManager.getAvailableDevices();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(REFRESH_DEVICES_BUTTON))) {
            visibleDeviceId = 0;
            availableDevices = this.midiDataManager.inputDeviceManager.getAvailableDevices();
        } else if(!this.midiDataManager.inputDeviceManager.isDeviceSelected() && this.availableDevices != null && this.availableDevices.size() > visibleDeviceId && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SAVE_DEVICE_BUTTON))) {
            this.midiDataManager.inputDeviceManager.saveDeviceSelection(availableDevices.get(visibleDeviceId));
        } else if(this.availableDevices != null && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHIFT_DEVICE_UP_BUTTON))) {
            visibleDeviceId = visibleDeviceId < (this.availableDevices.size() - 1) ? visibleDeviceId + 1 : visibleDeviceId;
        } else if(this.availableDevices != null  && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHIFT_DEVICE_DOWN_BUTTON))) {
            visibleDeviceId = visibleDeviceId > 0 ? visibleDeviceId - 1 : visibleDeviceId;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Background
        this.blitAbsolute(graphics, guiTexture, START_X, START_Y, 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Device Status Light
        Integer statusX = START_X + 277;
        Integer statusY = START_Y + 33;

        if(this.midiDataManager.inputDeviceManager.isDeviceSelected()) {
            if(this.midiDataManager.inputDeviceManager.isDeviceAvailable()) {
                this.blitAbsolute(graphics, guiTexture, statusX, statusY, 0, 169, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
            } else {
                this.blitAbsolute(graphics, guiTexture, statusX, statusY, 0, 175, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }
        
        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Selected Device Name / Error
        if(this.midiDataManager.inputDeviceManager.isDeviceSelected()) {
            this.drawStringAbsolute(graphics, font, this.midiDataManager.inputDeviceManager.getSelectedDeviceName(), START_X + 116, START_Y + 31, 0xFF00E600);

            if(this.midiDataManager.inputDeviceManager.isDeviceAvailable()) {
                this.drawStringAbsolute(graphics, font, "Connected", START_X + 101, START_Y + 46, 0xFF00E600);
            } else if(this.midiDataManager.inputDeviceManager.isDeviceError()) {
                this.drawStringAbsolute(graphics, font, "Error: " + this.midiDataManager.inputDeviceManager.getSelectedDeviceError(), START_X + 101, START_Y + 46, 0xFF00E600);
            } else {
                this.drawStringAbsolute(graphics, font, "Error: Unavailable", START_X + 101, START_Y + 46, 0xFF00E600);
            }
        }

        // Available Device Info
        if(this.availableDevices != null && this.availableDevices.size() > visibleDeviceId) {
            this.drawStringAbsolute(graphics, font, visibleDeviceId + ": " + this.availableDevices.get(visibleDeviceId).getDeviceInfo().getName(), START_X + 29, START_Y + 100, 0xFF00E600);
            Info info = this.availableDevices.get(visibleDeviceId).getDeviceInfo();
            if(info != null) {
                String descString = "Description: " + info.getDescription();
                Integer yOffset = 0;

                if(descString.length() <= 45) {
                    this.drawStringAbsolute(graphics, font, descString, START_X + 10, START_Y + 117, 0xFF00E600);
                } else {
                    yOffset = 16;
                    this.drawStringAbsolute(graphics, font, descString.substring(0, 45), START_X + 10, START_Y + 117, 0xFF00E600);
                    this.drawStringAbsolute(graphics, font, descString.substring(45), START_X + 10, START_Y + 133, 0xFF00E600);
                }

                this.drawStringAbsolute(graphics, font, "Vendor: " + info.getVendor(), START_X + 10, START_Y + yOffset + 133, 0xFF00E600);
                this.drawStringAbsolute(graphics, font, "Version: " + info.getVersion(), START_X + 10, START_Y + yOffset + 149, 0xFF00E600);  
            }
        }
        
        
        return graphics;
    }
}