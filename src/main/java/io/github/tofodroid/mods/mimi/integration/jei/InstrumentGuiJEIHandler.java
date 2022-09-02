package io.github.tofodroid.mods.mimi.integration.jei;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;

import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.client.gui.GuiInstrumentContainerScreen;

public class InstrumentGuiJEIHandler implements IScreenHandler<GuiInstrumentContainerScreen> {

    public InstrumentGuiJEIHandler() {

    }

    public @Nullable IGuiProperties apply(GuiInstrumentContainerScreen guiScreen) {
        return null;
    }
}