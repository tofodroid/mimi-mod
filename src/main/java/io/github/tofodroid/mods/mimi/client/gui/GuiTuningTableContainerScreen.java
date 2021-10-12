package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.tofodroid.mods.mimi.common.container.ContainerTuningTable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiTuningTableContainerScreen extends BaseContainerGui<ContainerTuningTable> {

    public GuiTuningTableContainerScreen(ContainerTuningTable container, PlayerInventory inv, ITextComponent textComponent) {
        super(container, inv, 176, 157, 176, "textures/gui/container_tuning.png", textComponent);
    }

    @Override
    protected MatrixStack renderGraphics(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        Minecraft.getInstance().getTextureManager().bindTexture(guiTexture);

        // GUI Background
        blit(matrixStack, this.guiLeft, this.guiTop, this.getBlitOffset(), 0, 0, this.xSize, this.ySize, TEXTURE_SIZE, TEXTURE_SIZE);
        
        return matrixStack;
    }

    @Override
    protected MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY) {
        return matrixStack;
    }
}
