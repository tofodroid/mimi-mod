package io.github.tofodroid.mods.mimi.client.gui.widget;

import io.github.tofodroid.mods.mimi.util.Vector2Int;

import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.gui.Font;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;

public class HoldTicksWidget extends BaseWidget {
    private static final Vector2Int DOWN_BUTTON_COORDS = new Vector2Int(58, 3);
    private static final Vector2Int UP_BUTTON_COORDS = new Vector2Int(98, 3);
    private static final Vector2Int VALUE_TEXT_COORDS = new Vector2Int(78, 7);

    private ItemStack midiStack;

    public HoldTicksWidget(ItemStack midiStack, Vector2Int screenOffset, Vector2Int start) {
        super("textures/gui/widget/hold_ticks.png", 116, new Vector2Int(116,21), screenOffset, start);
        this.midiStack = midiStack;
    }

    @Override
    public void renderGraphics(PoseStack graphics, Integer mouseX, Integer mouseY) {
        super.renderGraphics(graphics, mouseX, mouseY);
    }

    @Override
    public void renderText(PoseStack graphics, Font font, Integer mouseX, Integer mouseY) {
        super.renderText(graphics, font, mouseX, mouseY);

        this.drawStringAbsolute(graphics, font, MidiNbtDataUtils.getHoldTicks(midiStack).toString(), ABSOLUTE_START.x() + VALUE_TEXT_COORDS.x, ABSOLUTE_START.y() + VALUE_TEXT_COORDS.y, 0xFF00E600);
    }

    @Override
    protected Boolean mouseClicked(Vector2Int localMouseCoords, Integer mouseButton) {
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