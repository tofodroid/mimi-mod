package io.github.tofodroid.mods.mimi.client.gui;

import org.joml.Vector2f;

import io.github.tofodroid.mods.mimi.common.container.ContainerReceiver;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiReceiverContainerScreen extends ASwitchboardBlockGui<ContainerReceiver> {    
    public GuiReceiverContainerScreen(ContainerReceiver container, Inventory inv, Component textComponent) {
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