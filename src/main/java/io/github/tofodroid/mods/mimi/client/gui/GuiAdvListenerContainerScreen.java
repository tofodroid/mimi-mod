package io.github.tofodroid.mods.mimi.client.gui;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.tofodroid.mods.mimi.common.container.ContainerAdvListener;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;

public class GuiAdvListenerContainerScreen extends ASwitchboardGui<ContainerAdvListener> {
    // Button Boxes
    private static final Vector2f NOTE_LETTER_BUTTON_COORDS = new Vector2f(14,107);
    private static final Vector2f NOTE_OCTAVE_BUTTON_COORDS = new Vector2f(33,107);
    private static final Vector2f NOTE_INVERT_BUTTON_COORDS = new Vector2f(109,107);
    private static final Vector2f DOWN_INSTRUMENT_BUTTON_COORDS = new Vector2f(144,107);
    private static final Vector2f UP_INSTRUMENT_BUTTON_COORDS = new Vector2f(260,107);
    private static final Vector2f INVERT_INSTRUMENT_BUTTON_COORDS = new Vector2f(279,107);
    
    // Runtime Data
    private List<Byte> INSTRUMENT_ID_LIST;
    private Integer instrumentIndex = 0;
    
    public GuiAdvListenerContainerScreen(ContainerAdvListener container, PlayerInventory inv, ITextComponent textComponent) {
        super(container, inv, 315, 136, 315, "textures/gui/container_advlistener.png", textComponent);
    }

    @Override
    public void loadSelectedSwitchboard() {
        super.loadSelectedSwitchboard();
        this.instrumentIndex = INSTRUMENT_ID_LIST().indexOf(ItemMidiSwitchboard.getInstrument(selectedSwitchboardStack));
    }

    @Override
    public void clearSwitchboard() {
        super.clearSwitchboard();
        this.instrumentIndex = null;
    }

    @Override
    public boolean mouseReleased(double dmouseX, double dmouseY, int button) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        
		if(selectedSwitchboardStack != null) {
			if(clickedBox(imouseX, imouseY, NOTE_LETTER_BUTTON_COORDS)) {
				this.shiftFilterNoteLetter();
			} else if(clickedBox(imouseX, imouseY, NOTE_OCTAVE_BUTTON_COORDS)) {
				this.shiftFilterNoteOctave();
			} else if(clickedBox(imouseX, imouseY, NOTE_INVERT_BUTTON_COORDS)) {
				this.toggleInvertFilterNote();
			} else if(clickedBox(imouseX, imouseY, DOWN_INSTRUMENT_BUTTON_COORDS)) {
				shiftInstrumentId(false);
				ItemMidiSwitchboard.setInstrument(selectedSwitchboardStack, INSTRUMENT_ID_LIST.get(instrumentIndex));
				this.syncSwitchboardToServer();
			} else if(clickedBox(imouseX, imouseY, UP_INSTRUMENT_BUTTON_COORDS)) {
				shiftInstrumentId(true);
				ItemMidiSwitchboard.setInstrument(selectedSwitchboardStack, INSTRUMENT_ID_LIST.get(instrumentIndex));
				this.syncSwitchboardToServer();
			} else if(clickedBox(imouseX, imouseY, INVERT_INSTRUMENT_BUTTON_COORDS)) {
				ItemMidiSwitchboard.setInvertInstrument(selectedSwitchboardStack, !ItemMidiSwitchboard.getInvertInstrument(selectedSwitchboardStack));
				this.syncSwitchboardToServer();
			}
		}

        return super.mouseReleased(dmouseX, dmouseY, button);
    }

    @Override
    protected MatrixStack renderGraphics(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        Minecraft.getInstance().getTextureManager().bindTexture(guiTexture);

        // GUI Background
        blit(matrixStack, this.guiLeft, this.guiTop, this.getBlitOffset(), 0, 0, this.xSize, this.ySize, TEXTURE_SIZE, TEXTURE_SIZE);

        if(selectedSwitchboardStack != null) {
            // Invert Status Lights
            if(ItemMidiSwitchboard.getInvertInstrument(selectedSwitchboardStack)) {
                blit(matrixStack, this.guiLeft + 298, this.guiTop + 113, this.getBlitOffset(), 0, 137, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
            }
    
            if(ItemMidiSwitchboard.getInvertNoteOct(selectedSwitchboardStack)) {
                blit(matrixStack, this.guiLeft + 128, this.guiTop + 113, this.getBlitOffset(), 0, 137, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        } 

        return matrixStack;
    }

    @Override
    protected MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY) {
		if(this.selectedSwitchboardStack != null) {
			// Filter Note
			font.drawString(matrixStack, this.filterNoteString, 54, 111, invalidFilterNote() ? 0xFFE60000 : 0xFF00E600);

			// Filter Instrument
			font.drawString(matrixStack, ModItems.SWITCHBOARD.getInstrumentName(selectedSwitchboardStack), 165, 111, 0xFF00E600);
		}
       
        return matrixStack;
    }

    public List<Byte> INSTRUMENT_ID_LIST() {
        if(this.INSTRUMENT_ID_LIST == null) {
            this.INSTRUMENT_ID_LIST = ModItems.SWITCHBOARD.INSTRUMENT_NAME_MAP().keySet().stream().sorted().collect(Collectors.toList());
        }
        return this.INSTRUMENT_ID_LIST;
    }
	
    public void shiftInstrumentId(Boolean up) {
        if(up) {
            if(instrumentIndex < INSTRUMENT_ID_LIST.size()-1) {
                instrumentIndex++;
            } else {
                instrumentIndex = 0;
            }
        } else {
            if(instrumentIndex > 0) {
                instrumentIndex--;
            } else {
                instrumentIndex = INSTRUMENT_ID_LIST.size()-1;
            }
        }
    }
}