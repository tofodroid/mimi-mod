package io.github.tofodroid.mods.mimi.client.gui.widget;

import java.util.UUID;

import org.joml.Vector2i;

import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class TransmitterSourceWidget extends BaseWidget {
    private static final Vector2i SOURCE_SELF_BUTTON_COORDS = new Vector2i(78, 14);
    private static final Vector2i SOURCE_CLEAR_BUTTON_COORDS = new Vector2i(97, 14);

    private ItemStack midiStack;
    private String playerName;
    private UUID playerId;

    public TransmitterSourceWidget(ItemStack midiStack, UUID playerId, String playerName, Vector2i screenOffset, Vector2i start) {
        super("textures/gui/widget/transmit_source.png", 116,  new Vector2i(116,45), screenOffset, start);
        this.midiStack = midiStack;
        this.playerId = playerId;
        this.playerName = playerName;
    }

    @Override
    public void renderText(GuiGraphics graphics, Font font, Integer mouseX, Integer mouseY) {
        UUID source = InstrumentDataUtils.getMidiSource(this.midiStack);

        if(source == null) {
            graphics.drawString(font, "None", ABSOLUTE_START.x() + 6, ABSOLUTE_START.y() + 15, 0xFF00E600);
        } else {
            String sourceName = InstrumentDataUtils.getMidiSourceName(this.midiStack, true);
            Boolean isTransmitter = InstrumentDataUtils.getMidiSourceIsTransmitter(this.midiStack);

            graphics.drawString(font, isTransmitter ? "Transmitter:" : "Player:", ABSOLUTE_START.x() + 6, ABSOLUTE_START.y() + 15, 0xFF00E600);

            
            if(isTransmitter) {
                graphics.drawString(font, this.truncateString(font, sourceName.substring(0, sourceName.indexOf("@")), 68), ABSOLUTE_START.x() + 6, ABSOLUTE_START.y() + 24, 0xFF00E600);
                graphics.drawString(font, this.truncateString(font, sourceName.substring(sourceName.indexOf("(") + 1, sourceName.indexOf(")")), 106), ABSOLUTE_START.x() + 6, ABSOLUTE_START.y() + 34, 0xFF00E600);
            } else {
                graphics.drawString(font, this.truncateString(font, sourceName, 106), ABSOLUTE_START.x() + 6, ABSOLUTE_START.y() + 33, 0xFF00E600);
            }
        }
    }

    @Override
    protected Boolean mouseClicked(Vector2i localMouseCoords, Integer mouseButton) {
        if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), SOURCE_SELF_BUTTON_COORDS)) {
            InstrumentDataUtils.setMidiSource(midiStack, playerId, playerName);
            return true;
        } else if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), SOURCE_CLEAR_BUTTON_COORDS)) {
            InstrumentDataUtils.setMidiSource(midiStack, null, "None");
            return true;
        }
        
        return false;
    }
}
