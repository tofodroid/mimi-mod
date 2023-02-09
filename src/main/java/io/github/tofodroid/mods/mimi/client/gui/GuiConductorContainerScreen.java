package io.github.tofodroid.mods.mimi.client.gui;

import org.joml.Vector2f;

import io.github.tofodroid.mods.mimi.common.container.ContainerConductor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiConductorContainerScreen extends ASwitchboardBlockGui<ContainerConductor> {        
    public GuiConductorContainerScreen(ContainerConductor container, Inventory inv, Component textComponent) {
        super(container, inv, textComponent);
    }

    @Override
    protected Vector2f titleBoxPos() {
        return new Vector2f(119,8);
    }

    @Override
    protected Vector2f titleBoxBlit() {
        return new Vector2f(0,250);
    }

    @Override
    protected Vector2f titleBoxSize() {
        return new Vector2f(90,16);
    }

    @Override
    protected Boolean broadcastModeWidgetEnabled() {
        return true;
    }
    
    @Override
    protected Boolean broadcastNoteWidgetEnabled() {
        return true;
    }

    @Override
    protected Boolean channelWidgetEnabled() {
        return true;
    }
}
