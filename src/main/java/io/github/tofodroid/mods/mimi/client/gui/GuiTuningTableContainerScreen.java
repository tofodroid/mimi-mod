package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.tofodroid.mods.mimi.common.container.ContainerTuningTable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiTuningTableContainerScreen extends BaseContainerGui<ContainerTuningTable> {

    public GuiTuningTableContainerScreen(ContainerTuningTable container, Inventory inv, Component textComponent) {
        super(container, inv, 176, 157, 176, "textures/gui/container_tuning.png", textComponent);
    }

    @Override
    protected PoseStack renderGraphics(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // GUI Background
        blit(graphics, START_X, START_Y, 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        
        return graphics;
    }

    @Override
    protected PoseStack renderText(PoseStack graphics, int mouseX, int mouseY) {
        return graphics;
    }
}
