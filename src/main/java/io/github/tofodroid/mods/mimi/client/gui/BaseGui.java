package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.util.Vector2Int;
import io.github.tofodroid.mods.mimi.common.MIMIMod;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseGui extends Screen {
    protected static final Integer STANDARD_BUTTON_SIZE = 15;

    protected final ResourceLocation guiTexture;
    protected final Integer TEXTURE_SIZE;
    protected final Integer GUI_WIDTH;
    protected final Integer GUI_HEIGHT;

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

    public boolean doCloseOnInventoryKeyPress() {
        return true;
    }

    @Override
    public void init() {
        START_X = (this.width - GUI_WIDTH) / 2;
        START_Y = (this.height - GUI_HEIGHT) / 2;
    }
    
    @Override
    public void render(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        graphics = renderGraphics(graphics, mouseX, mouseY, partialTicks);
        graphics = renderText(graphics, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.doCloseOnInventoryKeyPress() && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            this.onClose();
            return true;
        }
        return false;
    }

    protected abstract PoseStack renderGraphics(PoseStack graphics, int mouseX, int mouseY, float partialTicks);
    protected abstract PoseStack renderText(PoseStack graphics, int mouseX, int mouseY, float partialTicks);

    protected Vector2Int screenToGuiCoords(Vector2Int screenCoords) {
        return new Vector2Int(
            screenCoords.x() - START_X, 
            screenCoords.y() - START_Y
        );
    }

    protected Vector2Int guiToScreenCoords(Vector2Int guiCoords) {
        return new Vector2Int(
            guiCoords.x() + START_X, 
            guiCoords.y() + START_Y
        );
    }

    // Absolute Position
    protected void blitAbsolute(PoseStack graphics, int renderStartX, int renderStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY, int textureSizeX, int textureSizeY) {
        CommonGuiUtils.blitAbsolute(graphics, this.guiTexture, renderStartX, renderStartY, atlasStartX, atlasStartY, sizeX, sizeY, textureSizeX, textureSizeY);
    }

    protected void blitAbsolute(PoseStack graphics, int renderStartX, int renderStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY) {
        CommonGuiUtils.blitAbsolute(graphics, this.guiTexture, renderStartX, renderStartY, atlasStartX, atlasStartY, sizeX, sizeY, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    protected void blitAbsolute(PoseStack graphics, ResourceLocation texture, int renderStartX, int renderStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY, int textureSizeX, int textureSizeY) {
        CommonGuiUtils.blitAbsolute(graphics, texture, renderStartX, renderStartY, atlasStartX, atlasStartY, sizeX, sizeY, textureSizeX, textureSizeY);
    }

    protected void blitAbsolute(PoseStack graphics, ResourceLocation texture, int renderStartX, int renderStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY) {
        CommonGuiUtils.blitAbsolute(graphics, texture, renderStartX, renderStartY, atlasStartX, atlasStartY, sizeX, sizeY, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    protected void drawStringAbsolute(PoseStack graphics, Font font, String string, Integer renderStartX, Integer renderStartY, Integer color) {
        CommonGuiUtils.drawStringAbsolute(graphics, font, string, renderStartX, renderStartY, color);
    }

    protected void drawStringAbsolute(PoseStack graphics, String string, Integer renderStartX, Integer renderStartY, Integer color) {
        CommonGuiUtils.drawStringAbsolute(graphics, font, string, renderStartX, renderStartY, color);
    }
    
    // Relative Position
    protected void blitRelative(PoseStack graphics, int relativeStartX, int relativeStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY, int textureSizeX, int textureSizeY) {
        CommonGuiUtils.blitAbsolute(graphics, this.guiTexture, START_X + relativeStartX, START_Y + relativeStartY, atlasStartX, atlasStartY, sizeX, sizeY, textureSizeX, textureSizeY);
    }

    protected void blitRelative(PoseStack graphics, int relativeStartX, int relativeStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY) {
        CommonGuiUtils.blitAbsolute(graphics, this.guiTexture, START_X + relativeStartX, START_Y + relativeStartY, atlasStartX, atlasStartY, sizeX, sizeY, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    protected void blitRelative(PoseStack graphics, ResourceLocation texture, int relativeStartX, int relativeStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY, int textureSizeX, int textureSizeY) {
        CommonGuiUtils.blitAbsolute(graphics, texture, START_X + relativeStartX, START_Y + relativeStartY, atlasStartX, atlasStartY, sizeX, sizeY, textureSizeX, textureSizeY);
    }

    protected void blitRelative(PoseStack graphics, ResourceLocation texture, int relativeStartX, int relativeStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY) {
        CommonGuiUtils.blitAbsolute(graphics, texture, START_X + relativeStartX, START_Y + relativeStartY, atlasStartX, atlasStartY, sizeX, sizeY, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    protected void drawStringRelative(PoseStack graphics, Font font, String string, Integer relativeStartX, Integer relativeStartY, Integer color) {
        CommonGuiUtils.drawStringAbsolute(graphics, font, string, START_X + relativeStartX, START_Y + relativeStartY, color);
    }

    protected void drawStringRelative(PoseStack graphics, String string, Integer relativeStartX, Integer relativeStartY, Integer color) {
        CommonGuiUtils.drawStringAbsolute(graphics, font, string, START_X + relativeStartX, START_Y + relativeStartY, color);
    }
}
