package io.github.tofodroid.mods.mimi.client.gui.widget;

import io.github.tofodroid.mods.mimi.util.Vector2Int;

import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class BroadcastRangeWidget extends BaseWidget {
    private static final Vector2Int BUTTON_COORDS = new Vector2Int(0, 0);

    private ItemStack midiStack;

    public BroadcastRangeWidget(ItemStack midiStack, Vector2Int screenOffset, Vector2Int start) {
        super("textures/gui/widget/broadcast_range.png", 65, new Vector2Int(17,17), screenOffset, start);
        this.midiStack = midiStack;
    }

    @Override
    public void renderGraphics(GuiGraphics graphics, Integer mouseX, Integer mouseY) {
        super.renderGraphics(graphics, mouseX, mouseY);

        // Broadcast Range
        this.blitAbsolute(graphics, GUI_TEXTURE, ABSOLUTE_START.x() + 2, ABSOLUTE_START.y() + 2, 13*MidiNbtDataUtils.getBroadcastRange(midiStack), 17, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    protected Boolean mouseClicked(Vector2Int localMouseCoords, Integer mouseButton) {
        if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), BUTTON_COORDS)) {
            Byte broadcastRange = MidiNbtDataUtils.getBroadcastRange(midiStack);

            if(broadcastRange < MidiNbtDataUtils.MAX_BROADCAST_RANGE) {
                broadcastRange = Integer.valueOf(broadcastRange+1).byteValue();
            } else {
                broadcastRange = MidiNbtDataUtils.MIN_BROADCAST_RANGE;
            }

            MidiNbtDataUtils.setBroadcastRange(midiStack, broadcastRange);
            return true;
        }
        
        return false;
    }
}
