package io.github.tofodroid.mods.mimi.client.gui;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.tofodroid.mods.mimi.common.container.ContainerAdvListener;
import io.github.tofodroid.mods.mimi.common.container.ContainerReceiver;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.SwitchboardStackUpdatePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;

public class GuiAdvListenerContainerScreen extends BaseContainerGui<ContainerAdvListener> {
    // Button Boxes
    private static final Vector2f NOTE_LETTER_BUTTON_COORDS = new Vector2f(14,107);
    private static final Vector2f NOTE_OCTAVE_BUTTON_COORDS = new Vector2f(33,107);
    private static final Vector2f NOTE_INVERT_BUTTON_COORDS = new Vector2f(109,107);
    private static final Vector2f DOWN_INSTRUMENT_BUTTON_COORDS = new Vector2f(144,107);
    private static final Vector2f UP_INSTRUMENT_BUTTON_COORDS = new Vector2f(260,107);
    private static final Vector2f INVERT_INSTRUMeNT_BUTTON_COORDS = new Vector2f(279,107);
    private final List<Byte> INSTRUMENT_ID_LIST;
    
    // Runtime Data
    private Integer filterNoteOctave = -1;
    private Integer filterNoteLetter = -1;
    private Boolean invalidFilterNote = false;
    private String filterNoteString = "";
    private Integer instrumentIndex = 0;
	private ItemStack selectedSwitchboardStack;
    
    public GuiAdvListenerContainerScreen(ContainerAdvListener container, PlayerInventory inv, ITextComponent textComponent) {
        super(container, inv, 315, 136, 315, "textures/gui/container_advlistener.png", textComponent);

        INSTRUMENT_ID_LIST = ModItems.SWITCHBOARD.INSTRUMENT_NAME_MAP().keySet().stream().sorted().collect(Collectors.toList());
		
        if(ModItems.SWITCHBOARD.equals(container.getSlot(ContainerReceiver.TARGET_CONTAINER_MIN_SLOT_ID).getStack().getItem())) {
            this.selectedSwitchboardStack = container.getSlot(ContainerReceiver.TARGET_CONTAINER_MIN_SLOT_ID).getStack();
            this.instrumentIndex = INSTRUMENT_ID_LIST.indexOf(ItemMidiSwitchboard.getInstrument(selectedSwitchboardStack));
			this.loadLetterAndOctave();
        }
    }

    @Override
    public boolean mouseReleased(double dmouseX, double dmouseY, int button) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        
		if(selectedSwitchboardStack != null) {
			if(clickedBox(imouseX, imouseY, NOTE_LETTER_BUTTON_COORDS)) {
				shiftFilterNoteLetter();
				ItemMidiSwitchboard.setFilterNote(selectedSwitchboardStack, filterNoteLetter.byteValue());
                this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
				this.syncSwitchboardToServer();
			} else if(clickedBox(imouseX, imouseY, NOTE_OCTAVE_BUTTON_COORDS)) {
				shiftFilterNoteOctave();
				ItemMidiSwitchboard.setFilterOct(selectedSwitchboardStack, filterNoteOctave.byteValue());
                this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
				this.syncSwitchboardToServer();
			} else if(clickedBox(imouseX, imouseY, NOTE_INVERT_BUTTON_COORDS)) {
				ItemMidiSwitchboard.setInvertNoteOct(selectedSwitchboardStack, !ItemMidiSwitchboard.getInvertNoteOct(selectedSwitchboardStack));
                this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
				this.syncSwitchboardToServer();
			} else if(clickedBox(imouseX, imouseY, DOWN_INSTRUMENT_BUTTON_COORDS)) {
				shiftInstrumentId(false);
				ItemMidiSwitchboard.setInstrument(selectedSwitchboardStack, INSTRUMENT_ID_LIST.get(instrumentIndex));
				this.syncSwitchboardToServer();
			} else if(clickedBox(imouseX, imouseY, UP_INSTRUMENT_BUTTON_COORDS)) {
				shiftInstrumentId(true);
				ItemMidiSwitchboard.setInstrument(selectedSwitchboardStack, INSTRUMENT_ID_LIST.get(instrumentIndex));
				this.syncSwitchboardToServer();
			} else if(clickedBox(imouseX, imouseY, INVERT_INSTRUMeNT_BUTTON_COORDS)) {
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
			font.drawString(matrixStack, this.filterNoteString, 54, 111, invalidFilterNote ? 0xFFE60000 : 0xFF00E600);

			// Filter Instrument
			font.drawString(matrixStack, ModItems.SWITCHBOARD.getInstrumentName(selectedSwitchboardStack), 165, 111, invalidFilterNote ? 0xFFE60000 : 0xFF00E600);
		}
       
        return matrixStack;
    }
	
    @Override
    public void tick() {
        super.tick();

        if(this.selectedSwitchboardStack == null && ModItems.SWITCHBOARD.equals(container.getSlot(ContainerReceiver.TARGET_CONTAINER_MIN_SLOT_ID).getStack().getItem())) {
            this.selectedSwitchboardStack = container.getSlot(ContainerReceiver.TARGET_CONTAINER_MIN_SLOT_ID).getStack();
			this.loadLetterAndOctave();
        } else if(selectedSwitchboardStack != null && !ModItems.SWITCHBOARD.equals(container.getSlot(ContainerReceiver.TARGET_CONTAINER_MIN_SLOT_ID).getStack().getItem())) {
            this.selectedSwitchboardStack = null;
			this.loadLetterAndOctave();
        }
    }

    public void loadLetterAndOctave() {
		if(this.selectedSwitchboardStack != null) {
			filterNoteLetter = ItemMidiSwitchboard.getFilterNote(selectedSwitchboardStack).intValue();
			filterNoteOctave = ItemMidiSwitchboard.getFilterOct(selectedSwitchboardStack).intValue();
			filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
		} else {
			filterNoteOctave = -1;
			filterNoteLetter = -1;
			filterNoteString = "";
		}       
    }

    public void shiftFilterNoteLetter() {
        if(filterNoteLetter < 11) {
            filterNoteLetter++;
        } else {
            filterNoteLetter = -1;
        }
    }
    
    public void shiftFilterNoteOctave() {
        if(filterNoteOctave < 10) {
            filterNoteOctave++;
        } else {
            filterNoteOctave = -1;
        }
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

    public void syncSwitchboardToServer() {
        SwitchboardStackUpdatePacket packet = null;

        if(selectedSwitchboardStack != null && ModItems.SWITCHBOARD.equals(selectedSwitchboardStack.getItem())) {
            packet = ItemMidiSwitchboard.getSyncPacket(selectedSwitchboardStack);
        }        
        if(packet != null) {
            NetworkManager.NET_CHANNEL.sendToServer(packet);
        }
    }
}