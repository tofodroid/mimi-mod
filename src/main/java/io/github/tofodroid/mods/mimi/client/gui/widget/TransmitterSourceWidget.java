package io.github.tofodroid.mods.mimi.client.gui.widget;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.util.Vector2Int;

import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class TransmitterSourceWidget extends BaseWidget {
    private static final Vector2Int SOURCE_SELF_BUTTON_COORDS = new Vector2Int(78, 14);
    private static final Vector2Int SOURCE_CLEAR_BUTTON_COORDS = new Vector2Int(97, 14);

    private ItemStack midiStack;
    private String playerName;
    private UUID playerId;

    public TransmitterSourceWidget(ItemStack midiStack, UUID playerId, String playerName, Vector2Int screenOffset, Vector2Int start) {
        super("textures/gui/widget/transmit_source.png", 116,  new Vector2Int(116,45), screenOffset, start);
        this.midiStack = midiStack;
        this.playerId = playerId;
        this.playerName = playerName;
    }

    @Override
    public void renderText(GuiGraphics graphics, Font font, Integer mouseX, Integer mouseY) {
        UUID source = MidiNbtDataUtils.getMidiSource(this.midiStack);

        if(source == null) {
            this.drawStringAbsolute(graphics, font, "None", ABSOLUTE_START.x() + 6, ABSOLUTE_START.y() + 15, 0xFF00E600);
        } else {
            String sourceName = MidiNbtDataUtils.getMidiSourceName(this.midiStack, true);
            Boolean isTransmitter = MidiNbtDataUtils.getMidiSourceIsTransmitter(this.midiStack);

            this.drawStringAbsolute(graphics, font, isTransmitter ? "Transmitter:" : "Player:", ABSOLUTE_START.x() + 6, ABSOLUTE_START.y() + 15, 0xFF00E600);

            
            if(isTransmitter) {
                this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, sourceName.substring(0, sourceName.indexOf("@")), 68), ABSOLUTE_START.x() + 6, ABSOLUTE_START.y() + 24, 0xFF00E600);
                this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, sourceName.substring(sourceName.indexOf("(") + 1, sourceName.indexOf(")")), 106), ABSOLUTE_START.x() + 6, ABSOLUTE_START.y() + 34, 0xFF00E600);
            } else {
                this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, sourceName, 106), ABSOLUTE_START.x() + 6, ABSOLUTE_START.y() + 33, 0xFF00E600);
            }
        }
    }

    @Override
    protected Boolean mouseClicked(Vector2Int localMouseCoords, Integer mouseButton) {
        if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), SOURCE_SELF_BUTTON_COORDS)) {
            MidiNbtDataUtils.setMidiSource(midiStack, playerId, playerName);
            return true;
        } else if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), SOURCE_CLEAR_BUTTON_COORDS)) {
            MidiNbtDataUtils.setMidiSource(midiStack, null, "None");
            return true;
        }
        
        return false;
    }
}
