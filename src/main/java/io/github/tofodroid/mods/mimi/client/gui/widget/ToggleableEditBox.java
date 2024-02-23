package io.github.tofodroid.mods.mimi.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class ToggleableEditBox extends EditBox {
    public ToggleableEditBox(Font p_94114_, int p_94115_, int p_94116_, int p_94117_, int p_94118_,
            Component p_94119_) {
        super(p_94114_, p_94115_, p_94116_, p_94117_, p_94118_, p_94119_);
    }

    protected Boolean enabled;

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    @Override
    public void onClick(double p_279417_, double p_279437_) {
        if(enabled) {
            super.onClick(p_279417_, p_279437_);
        }
    }
}
