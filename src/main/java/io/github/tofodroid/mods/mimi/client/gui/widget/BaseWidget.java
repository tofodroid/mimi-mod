package io.github.tofodroid.mods.mimi.client.gui.widget;

import org.joml.Vector2i;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseWidget {
    protected final ResourceLocation GUI_TEXTURE;
    protected final Integer TEXTURE_SIZE;
    protected final Vector2i GUI_START;
    protected final Vector2i WIDGET_START;
    protected final Vector2i ABSOLUTE_START;
    protected final Vector2i WIDGET_SIZE;
    
    public BaseWidget(String textureResource, Integer textureSize, Vector2i size, Vector2i guiStartOffset, Vector2i start) {
        this.GUI_TEXTURE = new ResourceLocation(MIMIMod.MODID, textureResource);
        this.TEXTURE_SIZE = textureSize;
        this.GUI_START = guiStartOffset;
        this.WIDGET_START = start;
        this.WIDGET_SIZE = size;
        this.ABSOLUTE_START = new Vector2i(
            GUI_START.x() + WIDGET_START.x(),
            GUI_START.y() + WIDGET_START.y()
        );
    }

    public void renderGraphics(PoseStack graphics, Integer mouseX, Integer mouseY) {
        // Background
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        Screen.blit(graphics, ABSOLUTE_START.x(), ABSOLUTE_START.y(), 0, 0, WIDGET_SIZE.x(), WIDGET_SIZE.y(), TEXTURE_SIZE, TEXTURE_SIZE);
    };

    public final Boolean mouseClicked(Integer mouseX, Integer mouseY, Integer mouseButton) {
        return this.mouseClicked(screenToLocalCoords(new Vector2i(mouseX, mouseY)), mouseButton);
    };

    // Default no-op methods
    public void renderText(PoseStack graphics, Font font, Integer mouseX, Integer mouseY) { /* No-op */ };
    public Boolean keyPressed(Integer keyCode, Integer scanCode, Integer modifiers) { /* No-op */ return false; };
    protected Boolean mouseClicked(Vector2i localMouseCoords, Integer mouseButton) { /* No-op */ return false; };


    protected Vector2i screenToLocalCoords(Vector2i screenCoords) {
        return new Vector2i(
            screenCoords.x() - ABSOLUTE_START.x(), 
            screenCoords.y() - ABSOLUTE_START.y()
        );
    }

    protected Vector2i localToScreenCoords(Vector2i localCoords) {
        return new Vector2i(
            localCoords.x() + ABSOLUTE_START.x(), 
            localCoords.y() + ABSOLUTE_START.y()
        );
    }

    protected String truncateString(Font font, String source, Integer maxWidth) {
        if(source == null || font.width(source) <= maxWidth) {
            return source;
        }
        return font.plainSubstrByWidth("..." + source, maxWidth).substring(3) + "...";
    }
}
