package io.github.tofodroid.mods.mimi.client.gui.widget;

import io.github.tofodroid.mods.mimi.util.Vector2Int;

import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class AnalogModeWidget extends BaseWidget {
    private static final Vector2Int BUTTON_COORDS = new Vector2Int(0, 0);

    private ItemStack midiStack;

    public AnalogModeWidget(ItemStack midiStack, Vector2Int screenOffset, Vector2Int start) {
        super("textures/gui/widget/analog_mode.png", 24, new Vector2Int(17,17), screenOffset, start);
        this.midiStack = midiStack;
    }

    @Override
    public void renderGraphics(GuiGraphics graphics, Integer mouseX, Integer mouseY) {
        super.renderGraphics(graphics, mouseX, mouseY);

        // Analog Light
        if(MidiNbtDataUtils.getAnalogMode(midiStack)) {
            this.blitAbsolute(graphics, GUI_TEXTURE, ABSOLUTE_START.x() + 4, ABSOLUTE_START.y() + 5, 15, 17, 9, 7, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    @Override
    protected Boolean mouseClicked(Vector2Int localMouseCoords, Integer mouseButton) {
        if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), BUTTON_COORDS)) {
            MidiNbtDataUtils.setAnalogMode(midiStack, !MidiNbtDataUtils.getAnalogMode(midiStack));
            return true;
        }
        
        return false;
    }
}
