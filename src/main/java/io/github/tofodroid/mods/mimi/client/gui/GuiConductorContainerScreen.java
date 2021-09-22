package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.common.container.ContainerConductor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;

public class GuiConductorContainerScreen extends ASwitchboardBlockGui<ContainerConductor> {        
    public GuiConductorContainerScreen(ContainerConductor container, PlayerInventory inv, ITextComponent textComponent) {
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
