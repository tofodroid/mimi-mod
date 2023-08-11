package io.github.tofodroid.mods.mimi.client.gui.widget;

import org.joml.Vector2i;
import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class NoteFilterWidget extends BaseWidget {
    protected static final Vector2i FILTER_NOTE_LETTER_BUTTON_COORDS = new Vector2i(3,14);
    protected static final Vector2i FILTER_NOTE_OCTAVE_BUTTON_COORDS = new Vector2i(22,14);
    protected static final Vector2i FILTER_NOTE_INVERT_BUTTON_COORDS = new Vector2i(98,14);

    private ItemStack midiStack;
    private Integer filterNoteOctave;
    private Integer filterNoteLetter;

    public NoteFilterWidget(ItemStack midiStack, Vector2i screenOffset, Vector2i start) {
        super("textures/gui/widget/note_filter.png", 116,  new Vector2i(116,32), screenOffset, start);
        this.midiStack = midiStack;
        this.filterNoteLetter = InstrumentDataUtils.getFilterNote(midiStack).intValue();
        this.filterNoteOctave = InstrumentDataUtils.getFilterOct(midiStack).intValue();     
    }

    @Override
    public void renderGraphics(GuiGraphics graphics, Integer mouseX, Integer mouseY) {
        super.renderGraphics(graphics, mouseX, mouseY);

        if(InstrumentDataUtils.getInvertNoteOct(midiStack)) {
            graphics.blit(GUI_TEXTURE, ABSOLUTE_START.x() + 104, ABSOLUTE_START.y() + 8, 0, 32, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    @Override
    public void renderText(GuiGraphics graphics, Font font, Integer mouseX, Integer mouseY) {
        graphics.drawString(font, InstrumentDataUtils.getFilteredNotesAsString(midiStack), ABSOLUTE_START.x() + 43, ABSOLUTE_START.y() + 18, 0xFF00E600);
    }

    @Override
    protected Boolean mouseClicked(Vector2i localMouseCoords, Integer mouseButton) {
        if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), FILTER_NOTE_OCTAVE_BUTTON_COORDS)) {
            this.shiftFilterNoteOctave();
            return true;
        } else if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), FILTER_NOTE_LETTER_BUTTON_COORDS)) {
            this.shiftFilterNoteLetter();
            return true;
        } else if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), FILTER_NOTE_INVERT_BUTTON_COORDS)) {
            InstrumentDataUtils.setInvertNoteOct(midiStack, !InstrumentDataUtils.getInvertNoteOct(midiStack));
            return true;
        }
        
        return false;
    }

    protected void shiftFilterNoteLetter() {
        if(filterNoteLetter < 11) {
            filterNoteLetter++;
            if(invalidFilterNote()) {
                filterNoteLetter = -1;
            }
        } else {
            filterNoteLetter = -1;
        }

        InstrumentDataUtils.setFilterNote(midiStack, filterNoteLetter.byteValue());
    }
    
    protected void shiftFilterNoteOctave() {
        if(filterNoteOctave < 10) {
            filterNoteOctave++;
            if(invalidFilterNote()) {
                filterNoteOctave = -1;
            }
        } else {
            filterNoteOctave = -1;
        }
        
        InstrumentDataUtils.setFilterOct(midiStack, filterNoteOctave.byteValue());
    }
    
	protected Boolean invalidFilterNote() {
		return Integer.valueOf(filterNoteOctave*12+filterNoteLetter) > Byte.MAX_VALUE;
	}
}
