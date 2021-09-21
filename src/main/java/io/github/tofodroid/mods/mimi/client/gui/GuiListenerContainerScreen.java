package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.common.container.ContainerListener;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiListenerContainerScreen extends ASwitchboardBlockGui<ContainerListener> {    
    public GuiListenerContainerScreen(ContainerListener container, PlayerInventory inv, ITextComponent textComponent) {
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