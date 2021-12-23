package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.math.Vector3f;

import io.github.tofodroid.mods.mimi.common.container.ContainerConductor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiConductorContainerScreen extends ASwitchboardBlockGui<ContainerConductor> {        
    public GuiConductorContainerScreen(ContainerConductor container, Inventory inv, Component textComponent) {
        super(container, inv, textComponent);
    }

    @Override
    protected Vector3f titleBoxPos() {
        return new Vector3f(119,8,0);
    }

    @Override
    protected Vector3f titleBoxBlit() {
        return new Vector3f(0,250,0);
    }

    @Override
    protected Vector3f titleBoxSize() {
        return new Vector3f(90,16,0);
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
