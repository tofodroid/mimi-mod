package io.github.tofodroid.mods.mimi.client.gui.widget;

import io.github.tofodroid.mods.mimi.util.ResourceUtils;
import io.github.tofodroid.mods.mimi.util.Vector2Int;
import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseWidget {
    protected final ResourceLocation GUI_TEXTURE;
    protected final Integer TEXTURE_SIZE;
    protected final Vector2Int GUI_START;
    protected final Vector2Int WIDGET_START;
    protected final Vector2Int ABSOLUTE_START;
    protected final Vector2Int WIDGET_SIZE;
    
    public BaseWidget(String textureResource, Integer textureSize, Vector2Int size, Vector2Int guiStartOffset, Vector2Int start) {
        this.GUI_TEXTURE = ResourceUtils.newModLocation(textureResource);
        this.TEXTURE_SIZE = textureSize;
        this.GUI_START = guiStartOffset;
        this.WIDGET_START = start;
        this.WIDGET_SIZE = size;
        this.ABSOLUTE_START = new Vector2Int(
            GUI_START.x() + WIDGET_START.x(),
            GUI_START.y() + WIDGET_START.y()
        );
    }

    public void renderGraphics(GuiGraphics graphics, Integer mouseX, Integer mouseY) {
        // Background
        this.blitAbsolute(graphics, GUI_TEXTURE, ABSOLUTE_START.x(), ABSOLUTE_START.y(), 0, 0, WIDGET_SIZE.x(), WIDGET_SIZE.y(), TEXTURE_SIZE, TEXTURE_SIZE);
    };

    public final Boolean mouseClicked(Integer mouseX, Integer mouseY, Integer mouseButton) {
        return this.mouseClicked(screenToLocalCoords(new Vector2Int(mouseX, mouseY)), mouseButton);
    };

    // Default no-op methods
    public void renderText(GuiGraphics graphics, Font font, Integer mouseX, Integer mouseY) { /* No-op */ };
    public Boolean keyPressed(Integer keyCode, Integer scanCode, Integer modifiers) { /* No-op */ return false; };
    protected Boolean mouseClicked(Vector2Int localMouseCoords, Integer mouseButton) { /* No-op */ return false; };


    protected Vector2Int screenToLocalCoords(Vector2Int screenCoords) {
        return new Vector2Int(
            screenCoords.x() - ABSOLUTE_START.x(), 
            screenCoords.y() - ABSOLUTE_START.y()
        );
    }

    protected Vector2Int localToScreenCoords(Vector2Int localCoords) {
        return new Vector2Int(
            localCoords.x() + ABSOLUTE_START.x(), 
            localCoords.y() + ABSOLUTE_START.y()
        );
    }

    // Absolute Position
    protected void blitAbsolute(GuiGraphics graphics, int renderStartX, int renderStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY, int textureSizeX, int textureSizeY) {
        CommonGuiUtils.blitAbsolute(graphics, this.GUI_TEXTURE, renderStartX, renderStartY, atlasStartX, atlasStartY, sizeX, sizeY, textureSizeX, textureSizeY);
    }

    protected void blitAbsolute(GuiGraphics graphics, int renderStartX, int renderStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY) {
        CommonGuiUtils.blitAbsolute(graphics, this.GUI_TEXTURE, renderStartX, renderStartY, atlasStartX, atlasStartY, sizeX, sizeY, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    protected void blitAbsolute(GuiGraphics graphics, ResourceLocation texture, int renderStartX, int renderStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY, int textureSizeX, int textureSizeY) {
        CommonGuiUtils.blitAbsolute(graphics, texture, renderStartX, renderStartY, atlasStartX, atlasStartY, sizeX, sizeY, textureSizeX, textureSizeY);
    }

    protected void blitAbsolute(GuiGraphics graphics, ResourceLocation texture, int renderStartX, int renderStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY) {
        CommonGuiUtils.blitAbsolute(graphics, texture, renderStartX, renderStartY, atlasStartX, atlasStartY, sizeX, sizeY, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    protected void drawStringAbsolute(GuiGraphics graphics, Font font, String string, Integer renderStartX, Integer renderStartY, Integer color) {
        CommonGuiUtils.drawStringAbsolute(graphics, font, string, renderStartX, renderStartY, color);
    }
}
