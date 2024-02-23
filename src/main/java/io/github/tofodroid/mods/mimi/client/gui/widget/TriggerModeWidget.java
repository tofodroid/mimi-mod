package io.github.tofodroid.mods.mimi.client.gui.widget;

import org.joml.Vector2i;

import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class TriggerModeWidget extends BaseWidget {
    private static final Vector2i DOWN_BUTTON_COORDS = new Vector2i(77, 3);
    private static final Vector2i UP_BUTTON_COORDS = new Vector2i(155, 3);
    private static final Vector2i VALUE_TEXT_COORDS = new Vector2i(97, 7);

    private ItemStack midiStack;

    public TriggerModeWidget(ItemStack midiStack, Vector2i screenOffset, Vector2i start) {
        super("textures/gui/widget/trigger_mode.png", 173, new Vector2i(173,21), screenOffset, start);
        this.midiStack = midiStack;
    }

    @Override
    public void renderGraphics(GuiGraphics graphics, Integer mouseX, Integer mouseY) {
        super.renderGraphics(graphics, mouseX, mouseY);
    }

    @Override
    public void renderText(GuiGraphics graphics, Font font, Integer mouseX, Integer mouseY) {
        super.renderText(graphics, font, mouseX, mouseY);

        graphics.drawString(font, MidiNbtDataUtils.getTriggerNoteStart(midiStack) ? "Note Start" : "Note Held", ABSOLUTE_START.x() + VALUE_TEXT_COORDS.x, ABSOLUTE_START.y() + VALUE_TEXT_COORDS.y, 0xFF00E600);
    }

    @Override
    protected Boolean mouseClicked(Vector2i localMouseCoords, Integer mouseButton) {
        if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), DOWN_BUTTON_COORDS)) {
            MidiNbtDataUtils.setTriggerNoteStart(midiStack, !MidiNbtDataUtils.getTriggerNoteStart(midiStack));
            return true;
        } else if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), UP_BUTTON_COORDS)) {
            MidiNbtDataUtils.setTriggerNoteStart(midiStack, !MidiNbtDataUtils.getTriggerNoteStart(midiStack));
            return true;
        }
        
        return false;
    }
}
