package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.InputConstants;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class BaseContainerGui<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected static final Integer STANDARD_BUTTON_SIZE = 15;

    protected ResourceLocation guiTexture;
    protected Integer TEXTURE_SIZE;
    protected Integer GUI_WIDTH;
    protected Integer GUI_HEIGHT;
    protected Integer START_X;
    protected Integer START_Y;

    public BaseContainerGui(T container, Inventory inv, Integer gWidth, Integer gHeight, Integer textureSize, String textureResource, Component textComponent) {
        super(container, inv, textComponent);
        this.guiTexture = new ResourceLocation(MIMIMod.MODID, textureResource);
        this.TEXTURE_SIZE = textureSize;
        this.GUI_HEIGHT = gHeight;
        this.GUI_WIDTH = gWidth;
        this.imageWidth = textureSize;
        this.imageHeight = textureSize;
    }

    @Override
    public void init() {
        START_X = (this.width - GUI_WIDTH) / 2;
        START_Y = (this.height - GUI_HEIGHT) / 2;
        this.leftPos = START_X;
        this.topPos = START_Y;
    }

    @Override
    protected void renderBg(PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
        graphics = renderGraphics(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderLabels(PoseStack graphics, int mouseX, int mouseY) {
        graphics = renderText(graphics, mouseX, mouseY);
    }

	@Override
	public void render(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        if(shouldRenderBackground()) {
            this.renderBackground(graphics);
        }
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

    protected Boolean shouldRenderBackground() {
        return true;
    }

    public boolean doCloseOnInventoryKeyPress() {
        return true;
    }

    @Override
    @SuppressWarnings("null")
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.doCloseOnInventoryKeyPress() && this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            this.onClose();
            return true;
        }
        return false;
    }

    protected void setAlpha(float alpha) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
    }

    protected abstract PoseStack renderGraphics(PoseStack graphics, int mouseX, int mouseY, float partialTicks);
    protected abstract PoseStack renderText(PoseStack graphics, int mouseX, int mouseY);

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
