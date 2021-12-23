package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.math.Vector3f;

import io.github.tofodroid.mods.mimi.common.container.ContainerReceiver;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiReceiverContainerScreen extends ASwitchboardBlockGui<ContainerReceiver> {    
    public GuiReceiverContainerScreen(ContainerReceiver container, Inventory inv, Component textComponent) {
        super(container, inv, textComponent);
    }
    
    @Override
    protected Vector3f titleBoxPos() {
        return new Vector3f(129,8,0);
    }

    @Override
    protected Vector3f titleBoxBlit() {
        return new Vector3f(95,250,0);
    }

    @Override
    protected Vector3f titleBoxSize() {
        return new Vector3f(71,16,0);
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