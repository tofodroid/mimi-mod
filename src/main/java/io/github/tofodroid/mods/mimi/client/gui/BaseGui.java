package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector2f;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public abstract class BaseGui extends Screen {
    protected static final Integer STANDARD_BUTTON_SIZE = 15;

    protected ResourceLocation guiTexture;
    protected Integer TEXTURE_SIZE;
    protected Integer GUI_WIDTH;
    protected Integer GUI_HEIGHT;
    protected Integer START_X;
    protected Integer START_Y;

    public BaseGui(Integer gWidth, Integer gHeight, Integer textureSize, String textureResource, String translationKey) {
        super(Component.translatable(translationKey));
        this.guiTexture = new ResourceLocation(MIMIMod.MODID, textureResource);
        this.TEXTURE_SIZE = textureSize;
        this.GUI_HEIGHT = gHeight;
        this.GUI_WIDTH = gWidth;
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
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        stack = renderGraphics(stack, mouseX, mouseY, partialTicks);
        stack = renderText(stack, mouseX, mouseY, partialTicks);
    }
    
    protected void setAlpha(float alpha) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
    }

    protected abstract PoseStack renderGraphics(PoseStack stack, int mouseX, int mouseY, float partialTicks);
    protected abstract PoseStack renderText(PoseStack stack, int mouseX, int mouseY, float partialTicks);

    protected Boolean clickedBox(Integer mouseX, Integer mouseY, Vector2f buttonPos) {
        Integer buttonMinX = START_X + Float.valueOf(buttonPos.x()).intValue();
        Integer buttonMaxX = buttonMinX + STANDARD_BUTTON_SIZE;
        Integer buttonMinY = START_Y + Float.valueOf(buttonPos.y()).intValue();
        Integer buttonMaxY = buttonMinY + STANDARD_BUTTON_SIZE;

        Boolean result = mouseX >= buttonMinX && mouseX <= buttonMaxX && mouseY >= buttonMinY && mouseY <= buttonMaxY;

        if(result) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        return result;
    }
    
    protected Boolean clickedBox(Integer mouseX, Integer mouseY, Vector2f buttonPos, Vector2f buttonSize) {
        Integer buttonMinX = START_X + Float.valueOf(buttonPos.x()).intValue();
        Integer buttonMaxX = buttonMinX + Float.valueOf(buttonSize.x()).intValue();
        Integer buttonMinY = START_Y + Float.valueOf(buttonPos.y()).intValue();
        Integer buttonMaxY = buttonMinY + Float.valueOf(buttonSize.y()).intValue();

        Boolean result = mouseX >= buttonMinX && mouseX <= buttonMaxX && mouseY >= buttonMinY && mouseY <= buttonMaxY;

        if(result) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        return result;
    }
}
