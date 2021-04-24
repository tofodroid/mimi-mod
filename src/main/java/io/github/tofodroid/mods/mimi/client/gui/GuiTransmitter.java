package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.TransmitterDataUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiTransmitter extends Screen {
    // Texture
    private static final ResourceLocation guiTexture = new ResourceLocation(MIMIMod.MODID, "textures/gui/gui_transmitter.png");
    private static final Integer GUI_WIDTH = 184;
    private static final Integer GUI_HEIGHT = 91;
    private static final Integer TEXTURE_SIZE = 184;
    private static final Integer BUTTON_SIZE = 15;
    
    // GUI
    private Integer startX;
    private Integer startY;
    
    // Button Boxes
    private static final Vector2f ENABLED_BUTTON = new Vector2f(148,37);
    private static final Vector2f MODE_BUTTON = new Vector2f(49,63);

    // Data
    private final ItemStack transmitterStack;

    public GuiTransmitter(PlayerEntity player, ItemStack transmitterStack) {
        super(new TranslationTextComponent("item.MIMIMod.gui_transmitter"));
        this.transmitterStack = transmitterStack;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        startX = (this.width - GUI_WIDTH) / 2;
        startY = (this.height - GUI_HEIGHT) / 2;
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

        if(clickedBox(imouseX, imouseY, ENABLED_BUTTON)) {
            ModItems.TRANSMITTER.toggleEnabled(transmitterStack);
            this.syncTransmitterToServer();
        } else if(clickedBox(imouseX, imouseY, MODE_BUTTON)) {
            ModItems.TRANSMITTER.toggleMode(transmitterStack);
            this.syncTransmitterToServer();
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

        // Enabled Status Light
        if(ModItems.TRANSMITTER.isEnabled(transmitterStack)) {
            blit(matrixStack, startX + 167, startY + 43, this.getBlitOffset(), 0, 92, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        
        return matrixStack;
    }

    private MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Selected Input Name
        String inputName = ModItems.TRANSMITTER.isMidiMode(transmitterStack) ? 
            "System MIDI Device" : "Hotbar Disk Drive";
        font.drawString(matrixStack, inputName, startX + 70, startY + 67, 0xFF00E600);
        
        return matrixStack;
    }

    private void syncTransmitterToServer() {
        if(this.transmitterStack != null && !this.transmitterStack.isEmpty()) {
            TransmitterDataUpdatePacket pkt = new TransmitterDataUpdatePacket(ModItems.TRANSMITTER.isEnabled(transmitterStack), ModItems.TRANSMITTER.isMidiMode(transmitterStack));
            NetworkManager.NET_CHANNEL.sendToServer(pkt);
        }
    }
}