package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class BaseGui extends Screen {
    protected static final Integer STANDARD_BUTTON_SIZE = 15;

    protected ResourceLocation guiTexture;
    protected Integer TEXTURE_SIZE;
    protected Integer GUI_WIDTH;
    protected Integer GUI_HEIGHT;
    protected Integer START_X;
    protected Integer START_Y;

    public BaseGui(Integer width, Integer height, Integer textureSize, String textureResource, String translastionKey) {
        super(new TranslationTextComponent(translastionKey));
        this.guiTexture = new ResourceLocation(MIMIMod.MODID, textureResource);
        this.TEXTURE_SIZE = textureSize;
        this.GUI_HEIGHT = height;
        this.GUI_WIDTH = width;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        START_X = (this.width - GUI_WIDTH) / 2;
        START_Y = Math.round((this.height - GUI_HEIGHT) / 1.25f);
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack = renderGraphics(matrixStack, mouseX, mouseY, partialTicks);
        matrixStack = renderText(matrixStack, mouseX, mouseY, partialTicks);
    }

    protected abstract MatrixStack renderGraphics(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
    protected abstract MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);

    protected Boolean clickedBox(Integer mouseX, Integer mouseY, Vector2f buttonPos) {
        Integer buttonMinX = START_X + new Float(buttonPos.x).intValue();
        Integer buttonMaxX = buttonMinX + STANDARD_BUTTON_SIZE;
        Integer buttonMinY = START_Y + new Float(buttonPos.y).intValue();
        Integer buttonMaxY = buttonMinY + STANDARD_BUTTON_SIZE;

        Boolean result = mouseX >= buttonMinX && mouseX <= buttonMaxX && mouseY >= buttonMinY && mouseY <= buttonMaxY;

        if(result) {
            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        return result;
    }
}
