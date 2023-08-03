package io.github.tofodroid.mods.mimi.client.gui;

import org.joml.Vector2f;

import io.github.tofodroid.mods.mimi.common.container.ContainerListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiListenerContainerScreen extends ASwitchboardBlockGui<ContainerListener> {    
    public GuiListenerContainerScreen(ContainerListener container, Inventory inv, Component textComponent) {
        super(container, inv, textComponent);
    }

    @Override
    protected Vector2f titleBoxPos() {
        return new Vector2f(131,8);
    }

    @Override
    protected Vector2f titleBoxBlit() {
        return new Vector2f(171,250);
    }

    @Override
    protected Vector2f titleBoxSize() {
        return new Vector2f(66,16);
    }
    
    @Override
    protected Boolean noteFilterWidgetEnabled() {
        return true;
    }
    
    @Override
    protected Boolean instrumentFilterWidgetEnabled() {
        return true;
    }
}