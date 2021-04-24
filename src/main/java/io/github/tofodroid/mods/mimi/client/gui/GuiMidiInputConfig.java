package io.github.tofodroid.mods.mimi.client.gui;

import javax.sound.midi.MidiDevice.Info;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.tofodroid.mods.mimi.client.midi.MidiInputManager;
import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiMidiInputConfig extends Screen {
    // Texture
    private static final ResourceLocation guiTexture = new ResourceLocation(MIMIMod.MODID, "textures/gui/gui_midi_config.png");
    private static final Integer GUI_WIDTH = 300;
    private static final Integer GUI_HEIGHT = 173;
    private static final Integer TEXTURE_SIZE = 300;
    private static final Integer BUTTON_SIZE = 15;
    
    // GUI
    private Integer startX;
    private Integer startY;
    
    // Button Boxes
    private static final Vector2f REFRESH_DEVICES_BUTTON = new Vector2f(268,37);
    private static final Vector2f SHIFT_DEVICE_DOWN_BUTTON = new Vector2f(120,63);
    private static final Vector2f SHIFT_DEVICE_UP_BUTTON = new Vector2f(264,63);

    // MIDI
    private MidiInputManager midiInputManager;

    public GuiMidiInputConfig(PlayerEntity player) {
        super(new TranslationTextComponent("item.MIMIMod.gui_midi_input_config"));
        this.midiInputManager = MIMIMod.proxy.getMidiInput();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        startX = (this.width - GUI_WIDTH) / 2;
        startY = Math.round((this.height - GUI_HEIGHT) / 1.25f);
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

        if(clickedBox(imouseX, imouseY, REFRESH_DEVICES_BUTTON)) {
            this.midiInputManager.inputDeviceManager.loadMidiDevices();
        } else if(clickedBox(imouseX, imouseY, SHIFT_DEVICE_UP_BUTTON)) {
            this.midiInputManager.inputDeviceManager.shiftMidiDevice(true);
        } else if(clickedBox(imouseX, imouseY, SHIFT_DEVICE_DOWN_BUTTON)) {
            this.midiInputManager.inputDeviceManager.shiftMidiDevice(false);
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
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

        // Device Status Lights
        if(this.midiInputManager != null && this.midiInputManager.inputDeviceManager.getSelectedDeviceId() != null) {
            Integer statusX = startX + 283;
            Integer statusY = startY + 69;
            blit(matrixStack, statusX, statusY, this.getBlitOffset(), this.midiInputManager.inputDeviceManager.isSelectedDeviceAvailable() ? 0 : 4, 174, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        
        return matrixStack;
    }

    private MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Num Devices
        String numDeviceString = this.midiInputManager.inputDeviceManager.getNumDevices().toString();
        font.drawString(matrixStack, numDeviceString, startX + 224, startY + 41, 0xFF00E600);

        // Selected Device Name
        String deviceNameString = (this.midiInputManager.inputDeviceManager.getSelectedDeviceId() != null ? 
            this.midiInputManager.inputDeviceManager.getSelectedDeviceId() + ": " : "")
            + this.midiInputManager.inputDeviceManager.getSelectedDeviceName();
        font.drawString(matrixStack, deviceNameString, startX + 142, startY + 67, 0xFF00E600);

        // Selected Device Info
        Info info = this.midiInputManager.inputDeviceManager.getSelectedDeviceInfo();
        if(info != null) {
            String descString = "Description: " + info.getDescription();
            Integer yOffset = 0;

            if(descString.length() <= 45) {
                font.drawString(matrixStack, descString, startX + 16, startY + 102, 0xFF00E600);
            } else {
                yOffset = 16;
                font.drawString(matrixStack, descString.substring(0, 45), startX + 16, startY + 102, 0xFF00E600);
                font.drawString(matrixStack, descString.substring(45), startX + 16, startY + 118, 0xFF00E600);
            }

            font.drawString(matrixStack, "Vendor: " + info.getVendor(), startX + 16, startY + yOffset + 118, 0xFF00E600);
            font.drawString(matrixStack, "Version: " + info.getVersion(), startX + 16, startY + yOffset + 134, 0xFF00E600);  
        }
        
        return matrixStack;
    }
}