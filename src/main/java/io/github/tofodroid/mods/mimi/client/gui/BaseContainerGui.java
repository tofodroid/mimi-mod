package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvents;

public abstract class BaseContainerGui<T extends Container> extends ContainerScreen<T> {
    protected static final Integer STANDARD_BUTTON_SIZE = 15;

    protected ResourceLocation guiTexture;
    protected Integer TEXTURE_SIZE;

    public BaseContainerGui(T container, PlayerInventory inv, Integer width, Integer height, Integer textureSize, String textureResource, ITextComponent textComponent) {
        super(container, inv, textComponent);
        this.guiTexture = new ResourceLocation(MIMIMod.MODID, textureResource);
        this.TEXTURE_SIZE = textureSize;
        this.passEvents = false;
        this.ySize = height;
        this.xSize = width;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        matrixStack = renderGraphics(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override 
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        matrixStack = renderText(matrixStack, mouseX, mouseY);
    }

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if(shouldRenderBackground()) {
            this.renderBackground(matrixStack);
        }
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
	}

    protected Boolean shouldRenderBackground() {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputMappings.Input invKey = InputMappings.getInputByCode(keyCode, scanCode);

        if (!this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(invKey) && super.keyPressed(keyCode, scanCode, modifiers)) {
           return true;
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    protected void setAlpha(float alpha) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, alpha);
    }

    protected abstract MatrixStack renderGraphics(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
    protected abstract MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY);

    protected Boolean clickedBox(Integer mouseX, Integer mouseY, Vector2f buttonPos) {
        Integer buttonMinX = this.guiLeft + new Float(buttonPos.x).intValue();
        Integer buttonMaxX = buttonMinX + STANDARD_BUTTON_SIZE;
        Integer buttonMinY = this.guiTop + new Float(buttonPos.y).intValue();
        Integer buttonMaxY = buttonMinY + STANDARD_BUTTON_SIZE;

        Boolean result = mouseX >= buttonMinX && mouseX <= buttonMaxX && mouseY >= buttonMinY && mouseY <= buttonMaxY;

        if(result) {
            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        return result;
    }
    
}
