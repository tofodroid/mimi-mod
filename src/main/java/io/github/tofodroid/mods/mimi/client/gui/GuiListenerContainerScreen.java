package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.math.Vector3f;

import io.github.tofodroid.mods.mimi.common.container.ContainerListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiListenerContainerScreen extends ASwitchboardBlockGui<ContainerListener> {    
    public GuiListenerContainerScreen(ContainerListener container, Inventory inv, Component textComponent) {
        super(container, inv, textComponent);
    }

    @Override
    protected Vector3f titleBoxPos() {
        return new Vector3f(131,8,0);
    }

    @Override
    protected Vector3f titleBoxBlit() {
        return new Vector3f(171,250,0);
    }

    @Override
    protected Vector3f titleBoxSize() {
        return new Vector3f(66,16,0);
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