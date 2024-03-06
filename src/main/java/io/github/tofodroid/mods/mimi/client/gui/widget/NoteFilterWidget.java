package io.github.tofodroid.mods.mimi.client.gui.widget;

import io.github.tofodroid.mods.mimi.util.Vector2Int;
import io.github.tofodroid.mods.mimi.client.gui.CommonGuiUtils;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.gui.Font;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;

public class NoteFilterWidget extends BaseWidget {
    protected static final Vector2Int FILTER_NOTE_LETTER_BUTTON_COORDS = new Vector2Int(3,14);
    protected static final Vector2Int FILTER_NOTE_OCTAVE_BUTTON_COORDS = new Vector2Int(22,14);
    protected static final Vector2Int FILTER_NOTE_INVERT_BUTTON_COORDS = new Vector2Int(98,14);

    private ItemStack midiStack;
    private Integer filterNoteOctave;
    private Integer filterNoteLetter;

    public NoteFilterWidget(ItemStack midiStack, Vector2Int screenOffset, Vector2Int start) {
        super("textures/gui/widget/note_filter.png", 116,  new Vector2Int(116,32), screenOffset, start);
        this.midiStack = midiStack;
        this.filterNoteLetter = MidiNbtDataUtils.getFilterNote(midiStack).intValue();
        this.filterNoteOctave = MidiNbtDataUtils.getFilterOct(midiStack).intValue();     
    }

    @Override
    public void renderGraphics(PoseStack graphics, Integer mouseX, Integer mouseY) {
        super.renderGraphics(graphics, mouseX, mouseY);

        if(MidiNbtDataUtils.getInvertNoteOct(midiStack)) {
            this.blitAbsolute(graphics, GUI_TEXTURE, ABSOLUTE_START.x() + 104, ABSOLUTE_START.y() + 8, 0, 32, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    @Override
    public void renderText(PoseStack graphics, Font font, Integer mouseX, Integer mouseY) {
        this.drawStringAbsolute(graphics, font, MidiNbtDataUtils.getFilteredNotesAsString(midiStack), ABSOLUTE_START.x() + 43, ABSOLUTE_START.y() + 18, 0xFF00E600);
    }

    @Override
    protected Boolean mouseClicked(Vector2Int localMouseCoords, Integer mouseButton) {
        if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), FILTER_NOTE_OCTAVE_BUTTON_COORDS)) {
            this.shiftFilterNoteOctave();
            return true;
        } else if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), FILTER_NOTE_LETTER_BUTTON_COORDS)) {
            this.shiftFilterNoteLetter();
            return true;
        } else if(CommonGuiUtils.clickedBox(localMouseCoords.x(), localMouseCoords.y(), FILTER_NOTE_INVERT_BUTTON_COORDS)) {
            MidiNbtDataUtils.setInvertNoteOct(midiStack, !MidiNbtDataUtils.getInvertNoteOct(midiStack));
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

        MidiNbtDataUtils.setFilterNote(midiStack, filterNoteLetter.byteValue());
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
        
        MidiNbtDataUtils.setFilterOct(midiStack, filterNoteOctave.byteValue());
    }
    
	protected Boolean invalidFilterNote() {
		return Integer.valueOf(filterNoteOctave*12+filterNoteLetter) > Byte.MAX_VALUE;
	}
}
