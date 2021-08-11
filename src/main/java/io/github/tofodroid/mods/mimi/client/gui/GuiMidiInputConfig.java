package io.github.tofodroid.mods.mimi.client.gui;

import javax.sound.midi.MidiDevice.Info;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiInputManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector2f;

public class GuiMidiInputConfig extends BaseGui {    
    // Button Boxes
    private static final Vector2f REFRESH_DEVICES_BUTTON = new Vector2f(268,37);
    private static final Vector2f SHIFT_DEVICE_DOWN_BUTTON = new Vector2f(120,63);
    private static final Vector2f SHIFT_DEVICE_UP_BUTTON = new Vector2f(264,63);

    // MIDI
    private MidiInputManager midiInputManager;

    public GuiMidiInputConfig(PlayerEntity player) {
        super(300, 158, 300, "textures/gui/gui_midi_config.png",  "item.MIMIMod.gui_midi_input_config");
        this.midiInputManager = MIMIMod.proxy.getMidiInput();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(clickedBox(imouseX, imouseY, REFRESH_DEVICES_BUTTON)) {
            this.midiInputManager.inputDeviceManager.loadMidiDevices();
        } else if(clickedBox(imouseX, imouseY, SHIFT_DEVICE_UP_BUTTON)) {
            this.midiInputManager.inputDeviceManager.shiftMidiDevice(true);
        } else if(clickedBox(imouseX, imouseY, SHIFT_DEVICE_DOWN_BUTTON)) {
            this.midiInputManager.inputDeviceManager.shiftMidiDevice(false);
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected MatrixStack renderGraphics(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        Minecraft.getInstance().getTextureManager().bindTexture(guiTexture);

        // Background
        blit(matrixStack, START_X, START_Y, this.getBlitOffset(), 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Device Status Lights
        if(this.midiInputManager != null && this.midiInputManager.inputDeviceManager.getSelectedDeviceId() != null) {
            Integer statusX = START_X + 283;
            Integer statusY = START_Y + 69;
            blit(matrixStack, statusX, statusY, this.getBlitOffset(), this.midiInputManager.inputDeviceManager.isSelectedDeviceAvailable() ? 0 : 4, 159, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        
        return matrixStack;
    }

    @Override
    protected MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Num Devices
        String numDeviceString = this.midiInputManager.inputDeviceManager.getNumDevices().toString();
        font.drawString(matrixStack, numDeviceString, START_X + 224, START_Y + 41, 0xFF00E600);

        // Selected Device Name
        String deviceNameString = (this.midiInputManager.inputDeviceManager.getSelectedDeviceId() != null ? 
            this.midiInputManager.inputDeviceManager.getSelectedDeviceId() + ": " : "")
            + this.midiInputManager.inputDeviceManager.getSelectedDeviceName();
        font.drawString(matrixStack, deviceNameString, START_X + 142, START_Y + 67, 0xFF00E600);

        // Selected Device Info
        Info info = this.midiInputManager.inputDeviceManager.getSelectedDeviceInfo();
        if(info != null) {
            String descString = "Description: " + info.getDescription();
            Integer yOffset = 0;

            if(descString.length() <= 45) {
                font.drawString(matrixStack, descString, START_X + 16, START_Y + 102, 0xFF00E600);
            } else {
                yOffset = 16;
                font.drawString(matrixStack, descString.substring(0, 45), START_X + 16, START_Y + 102, 0xFF00E600);
                font.drawString(matrixStack, descString.substring(45), START_X + 16, START_Y + 118, 0xFF00E600);
            }

            font.drawString(matrixStack, "Vendor: " + info.getVendor(), START_X + 16, START_Y + yOffset + 118, 0xFF00E600);
            font.drawString(matrixStack, "Version: " + info.getVersion(), START_X + 16, START_Y + yOffset + 134, 0xFF00E600);  
        }
        
        return matrixStack;
    }
}