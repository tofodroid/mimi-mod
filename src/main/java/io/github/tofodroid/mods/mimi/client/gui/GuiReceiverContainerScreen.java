package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.common.container.ContainerReceiver;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;

public class GuiReceiverContainerScreen extends ASwitchboardBlockGui<ContainerReceiver> {    
    public GuiReceiverContainerScreen(ContainerReceiver container, PlayerInventory inv, ITextComponent textComponent) {
        super(container, inv, textComponent);
    }
    
    @Override
    protected Vector2f titleBoxPos() {
        return new Vector2f(129,8);
    }

    @Override
    protected Vector2f titleBoxBlit() {
        return new Vector2f(95,250);
    }

    @Override
    protected Vector2f titleBoxSize() {
        return new Vector2f(71,16);
    }
    
    @Override
    protected Boolean noteFilterWidgetEnabled() {
        return true;
    }
    
    @Override
    protected Boolean linkedTransmitterWidgetEnabled() {
        return true;
    }

    @Override
    protected Boolean channelWidgetEnabled() {
        return true;
    }
}