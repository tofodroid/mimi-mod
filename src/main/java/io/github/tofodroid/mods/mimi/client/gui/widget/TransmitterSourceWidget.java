package io.github.tofodroid.mods.mimi.client.gui.widget;

import java.util.UUID;

import org.joml.Vector2i;

import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class TransmitterSourceWidget extends BaseWidget {
    private static final Vector2i SOURCE_SELF_BUTTON_COORDS = new Vector2i(32, 26);
    private static final Vector2i SOURCE_PUBLIC_BUTTON_COORDS = new Vector2i(51, 26);
    private static final Vector2i SOURCE_CLEAR_BUTTON_COORDS = new Vector2i(70, 26);

    private ItemStack midiStack;
    private String playerName;
    private UUID playerId;

    public TransmitterSourceWidget(ItemStack midiStack, UUID playerId, String playerName, Vector2i screenOffset, Vector2i start) {
        super("textures/gui/widget/transmit_source.png", 116,  new Vector2i(116,44), screenOffset, start);
        this.midiStack = midiStack;
        this.playerId = playerId;
        this.playerName = playerName;
    }

    @Override
    public void renderText(GuiGraphics graphics, Font font, Integer mouseX, Integer mouseY) {
        String selectedSourceName = InstrumentDataUtils.getMidiSourceName(this.midiStack);
        graphics.drawString(font, selectedSourceName.length() <= 22 ? selectedSourceName : selectedSourceName.substring(0,21) + "...", ABSOLUTE_START.x() + 6, ABSOLUTE_START.y() + 15, 0xFF00E600);
    }

    @Override
    protected Boolean mouseClicked(Vector2i localMouseCoords, Integer mouseButton) {
        if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), SOURCE_SELF_BUTTON_COORDS)) {
            InstrumentDataUtils.setMidiSource(midiStack, playerId, playerName);
            return true;
        } else if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), SOURCE_PUBLIC_BUTTON_COORDS)) {
            InstrumentDataUtils.setMidiSource(midiStack, InstrumentDataUtils.PUBLIC_SOURCE_ID, "Public");
            return true;
        } else if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), SOURCE_CLEAR_BUTTON_COORDS)) {
            InstrumentDataUtils.setMidiSource(midiStack, null, "None");
            return true;
        }
        
        return false;
    }
}
