package io.github.tofodroid.mods.mimi.client.gui.widget;

import org.joml.Vector2i;

import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class HoldTicksWidget extends BaseWidget {
    private static final Vector2i DOWN_BUTTON_COORDS = new Vector2i(58, 3);
    private static final Vector2i UP_BUTTON_COORDS = new Vector2i(98, 3);
    private static final Vector2i VALUE_TEXT_COORDS = new Vector2i(78, 7);

    private ItemStack midiStack;

    public HoldTicksWidget(ItemStack midiStack, Vector2i screenOffset, Vector2i start) {
        super("textures/gui/widget/hold_ticks.png", 116, new Vector2i(116,21), screenOffset, start);
        this.midiStack = midiStack;
    }

    @Override
    public void renderGraphics(GuiGraphics graphics, Integer mouseX, Integer mouseY) {
        super.renderGraphics(graphics, mouseX, mouseY);
    }

    @Override
    public void renderText(GuiGraphics graphics, Font font, Integer mouseX, Integer mouseY) {
        super.renderText(graphics, font, mouseX, mouseY);

        graphics.drawString(font, MidiNbtDataUtils.getHoldTicks(midiStack).toString(), ABSOLUTE_START.x() + VALUE_TEXT_COORDS.x, ABSOLUTE_START.y() + VALUE_TEXT_COORDS.y, 0xFF00E600);
    }

    @Override
    protected Boolean mouseClicked(Vector2i localMouseCoords, Integer mouseButton) {
        if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), DOWN_BUTTON_COORDS)) {
            MidiNbtDataUtils.setHoldTicks(midiStack, Integer.valueOf(MidiNbtDataUtils.getHoldTicks(midiStack)-1).byteValue());
            return true;
        } else if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), UP_BUTTON_COORDS)) {
            MidiNbtDataUtils.setHoldTicks(midiStack, Integer.valueOf(MidiNbtDataUtils.getHoldTicks(midiStack)+1).byteValue());
            return true;
        }
        
        return false;
    }
}