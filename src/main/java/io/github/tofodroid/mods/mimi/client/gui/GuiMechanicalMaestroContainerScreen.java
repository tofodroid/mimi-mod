package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.common.container.ContainerMechanicalMaestro;
import com.mojang.blaze3d.vertex.PoseStack;
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
    protected PoseStack renderGraphics(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        // GUI Background
        this.blitAbsolute(graphics, guiTexture, START_X, START_Y, 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        return graphics;
    }

    @Override
    protected PoseStack renderText(PoseStack graphics, int mouseX, int mouseY) {
        return graphics;
    }
}
