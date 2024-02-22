package io.github.tofodroid.mods.mimi.client.gui;

import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2i;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.client.midi.MidiDataManager;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;

public class GuiMidiInputConfig extends BaseGui {    
    // Button Boxes
    private static final Vector2i CLEAR_DEVICE_BUTTON = new Vector2i(266,27);
    private static final Vector2i REFRESH_DEVICES_BUTTON = new Vector2i(266,83);
    private static final Vector2i SHIFT_DEVICE_DOWN_BUTTON = new Vector2i(104,83);
    private static final Vector2i SHIFT_DEVICE_UP_BUTTON = new Vector2i(247,83);
    private static final Vector2i SAVE_DEVICE_BUTTON = new Vector2i(266,133);

    // MIDI
    private MidiDataManager midiDataManager;
    private List<MidiDevice> availableDevices;
    private Integer visibleDeviceId = 0;

    public GuiMidiInputConfig(Player player) {
        super(288, 156, 288, "textures/gui/gui_midi_config.png",  "item.MIMIMod.gui_midi_input_config");
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
        } else if(this.midiDataManager.inputDeviceManager.isDeviceSelected() && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(CLEAR_DEVICE_BUTTON))) {
            this.midiDataManager.inputDeviceManager.clearDeviceSelection();
        } else if(this.availableDevices != null && this.availableDevices.size() > visibleDeviceId && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SAVE_DEVICE_BUTTON))) {
            this.midiDataManager.inputDeviceManager.saveDeviceSelection(availableDevices.get(visibleDeviceId));
        } else if(this.availableDevices != null && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHIFT_DEVICE_UP_BUTTON))) {
            visibleDeviceId = visibleDeviceId < (this.availableDevices.size() - 1) ? visibleDeviceId + 1 : visibleDeviceId;
        } else if(this.availableDevices != null  && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHIFT_DEVICE_DOWN_BUTTON))) {
            visibleDeviceId = visibleDeviceId > 0 ? visibleDeviceId - 1 : visibleDeviceId;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected PoseStack renderGraphics(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // Background
        blit(graphics, START_X, START_Y, 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Device Status Light
        Integer statusX = START_X + 259;
        Integer statusY = START_Y + 33;

        if(this.midiDataManager.inputDeviceManager.isDirtyStatus()) {
            blit(graphics, statusX, statusY, 8, 159, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
        } else if(this.midiDataManager.inputDeviceManager.isDeviceSelected()) {
            blit(graphics, statusX, statusY, this.midiDataManager.inputDeviceManager.isDeviceAvailable() ? 0 : 4, 159, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        
        return graphics;
    }

    @Override
    protected PoseStack renderText(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        // Selected Device Name
        if(this.midiDataManager.inputDeviceManager.isDeviceSelected()) {
            drawString(graphics, font, this.midiDataManager.inputDeviceManager.getSelectedDeviceName(), START_X + 117, START_Y + 40, 0xFF00E600);
        }

        // Available Device Info
        if(this.availableDevices != null && this.availableDevices.size() > visibleDeviceId) {
            drawString(graphics, font, visibleDeviceId + ": " + this.availableDevices.get(visibleDeviceId).getDeviceInfo().getName(), START_X + 125, START_Y + 87, 0xFF00E600);
            Info info = this.availableDevices.get(visibleDeviceId).getDeviceInfo();
            if(info != null) {
                String descString = "Description: " + info.getDescription();
                Integer yOffset = 0;

                if(descString.length() <= 45) {
                    drawString(graphics, font, descString, START_X + 10, START_Y + 104, 0xFF00E600);
                } else {
                    yOffset = 16;
                    drawString(graphics, font, descString.substring(0, 45), START_X + 10, START_Y + 104, 0xFF00E600);
                    drawString(graphics, font, descString.substring(45), START_X + 10, START_Y + 120, 0xFF00E600);
                }

                drawString(graphics, font, "Vendor: " + info.getVendor(), START_X + 10, START_Y + yOffset + 120, 0xFF00E600);
                drawString(graphics, font, "Version: " + info.getVersion(), START_X + 10, START_Y + yOffset + 136, 0xFF00E600);  
            }
        }
        
        
        return graphics;
    }
}