package io.github.tofodroid.mods.mimi.client.gui.widget;

import org.joml.Vector2i;

import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.item.ItemStack;

public class MidiChannelToggleWidget extends BaseWidget {
    private static final Vector2i ALL_MIDI_BUTTON_COORDS = new Vector2i(3, 17);
    private static final Vector2i CLEAR_MIDI_BUTTON_COORDS = new Vector2i(3, 42);
    private static final Vector2i GEN_MIDI_BUTTON_COORDS = new Vector2i(22, 17);

    private ItemStack midiStack;

    public MidiChannelToggleWidget(ItemStack midiStack, Vector2i screenOffset, Vector2i start) {
        super("textures/gui/widget/midi_channel_toggle.png", 173, new Vector2i(173,67), screenOffset, start);
        this.midiStack = midiStack;
    }

    @Override
    public void renderGraphics(GuiGraphics graphics, Integer mouseX, Integer mouseY) {
        super.renderGraphics(graphics, mouseX, mouseY);

        // Channel Output Status Lights
        SortedArraySet<Byte> acceptedChannels = InstrumentDataUtils.getEnabledChannelsSet(this.midiStack);

        if(acceptedChannels != null && !acceptedChannels.isEmpty()) {
            for(Byte channelId : acceptedChannels) {
                graphics.blit(GUI_TEXTURE, ABSOLUTE_START.x() + 28 + (channelId % 8) * 19, ABSOLUTE_START.y() + 35 + (channelId / 8) * 25, 0, 67, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }
    }

    @Override
    protected Boolean mouseClicked(Vector2i localMouseCoords, Integer mouseButton) {
        if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), CLEAR_MIDI_BUTTON_COORDS)) {
            InstrumentDataUtils.clearEnabledChannels(midiStack);
            return true;
        } else if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), ALL_MIDI_BUTTON_COORDS)) {
            InstrumentDataUtils.setEnableAllChannels(midiStack);
            return true;
        } else {
            // Individual Midi Channel Buttons
            for(byte i = 0; i < 16; i++) {
                Vector2i buttonCoords = new Vector2i(
                    GEN_MIDI_BUTTON_COORDS.x() + (i % 8) * 19,
                    GEN_MIDI_BUTTON_COORDS.y() + (i / 8) * 25
                );

                if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), buttonCoords)) {
                    InstrumentDataUtils.toggleChannel(midiStack, i);
                    return true;
                }
            }
        }
        
        return false;
    }
}
