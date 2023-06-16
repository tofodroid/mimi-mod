package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.tofodroid.mods.mimi.common.container.ContainerTuningTable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiTuningTableContainerScreen extends BaseContainerGui<ContainerTuningTable> {

    public GuiTuningTableContainerScreen(ContainerTuningTable container, Inventory inv, Component textComponent) {
        super(container, inv, 176, 157, 176, "textures/gui/container_tuning.png", textComponent);
    }

    @Override
    protected PoseStack renderGraphics(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // GUI Background
        blit(matrixStack, START_X, START_Y, 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        
        return matrixStack;
    }

    @Override
    protected PoseStack renderText(PoseStack matrixStack, int mouseX, int mouseY) {
        return matrixStack;
    }
}
