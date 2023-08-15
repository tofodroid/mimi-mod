package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.tofodroid.mods.mimi.common.container.ContainerMechanicalMaestro;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiMechanicalMaestroContainerScreen extends BaseContainerGui<ContainerMechanicalMaestro> {
    public GuiMechanicalMaestroContainerScreen(ContainerMechanicalMaestro container, Inventory inv, Component textComponent) {
        super(container, inv, 180, 148, 180, "textures/gui/container_mech_maestro.png", textComponent);
    }
    
    @Override
    public void init() {
        super.init();
    }

    @Override
    protected GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // GUI Background
        graphics.blit(guiTexture, START_X, START_Y, 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY) {
        return graphics;
    }
}
