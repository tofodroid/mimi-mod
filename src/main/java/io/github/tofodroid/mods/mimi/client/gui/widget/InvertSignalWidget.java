package io.github.tofodroid.mods.mimi.client.gui.widget;

import org.joml.Vector2i;

import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class InvertSignalWidget extends BaseWidget {
    private static final Vector2i BUTTON_COORDS = new Vector2i(0, 0);

    private ItemStack midiStack;

    public InvertSignalWidget(ItemStack midiStack, Vector2i screenOffset, Vector2i start) {
        super("textures/gui/widget/invert_signal.png", 22, new Vector2i(17,17), screenOffset, start);
        this.midiStack = midiStack;
    }

    @Override
    public void renderGraphics(GuiGraphics graphics, Integer mouseX, Integer mouseY) {
        super.renderGraphics(graphics, mouseX, mouseY);

        // Inverted Lights
        if(MidiNbtDataUtils.getInvertSignal(midiStack)) {
            graphics.blit(GUI_TEXTURE, ABSOLUTE_START.x() + 6, ABSOLUTE_START.y() + 6, 17, 17, 5, 5, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    @Override
    protected Boolean mouseClicked(Vector2i localMouseCoords, Integer mouseButton) {
        if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), BUTTON_COORDS)) {
            MidiNbtDataUtils.setInvertSignal(midiStack, !MidiNbtDataUtils.getInvertSignal(midiStack));
            return true;
        }
        
        return false;
    }
}