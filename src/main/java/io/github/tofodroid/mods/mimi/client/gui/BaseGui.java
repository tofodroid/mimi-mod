package io.github.tofodroid.mods.mimi.client.gui;

import org.joml.Vector2i;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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
    public void render(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        graphics = renderGraphics(graphics, mouseX, mouseY, partialTicks);
        graphics = renderText(graphics, mouseX, mouseY, partialTicks);
    }
    
    protected void setAlpha(float alpha) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
    }

    protected abstract PoseStack renderGraphics(PoseStack graphics, int mouseX, int mouseY, float partialTicks);
    protected abstract PoseStack renderText(PoseStack graphics, int mouseX, int mouseY, float partialTicks);

    protected Vector2i screenToGuiCoords(Vector2i screenCoords) {
        return new Vector2i(
            screenCoords.x() - START_X, 
            screenCoords.y() - START_Y
        );
    }

    protected Vector2i guiToScreenCoords(Vector2i guiCoords) {
        return new Vector2i(
            guiCoords.x() + START_X, 
            guiCoords.y() + START_Y
        );
    }

    protected String truncateString(Font font, String source, Integer maxWidth) {
        if(source == null || font.width(source) <= maxWidth) {
            return source;
        }
        return font.plainSubstrByWidth("..." + source, maxWidth).substring(3) + "...";
    }

}
