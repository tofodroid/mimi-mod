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
    private static final Vector2f NOTE_LETTER_BUTTON_COORDS = new Vector2f(85,67);
    private static final Vector2f NOTE_OCTAVE_BUTTON_COORDS = new Vector2f(104,67);
    private static final Vector2f CYCLE_INSTRUMENT_BUTTON_COORDS = new Vector2f(274,14);
    private final List<Byte> INSTRUMENT_ID_LIST;
    
    // Runtime Data
    private Integer filterNoteOctave = -1;
    private Integer filterNoteLetter = -1;
    private Boolean invalidFilterNote = false;
    private String filterNoteString = "";
    private Integer instrumentIndex = 0;
	private ItemStack selectedSwitchboardStack;
    
    public GuiAdvListenerContainerScreen(ContainerAdvListener container, PlayerInventory inv, ITextComponent textComponent) {
        super(container, inv, 303, 127, 303, "textures/gui/container_advlistener.png", textComponent);

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
				ItemMidiSwitchboard.setFilterNote(selectedSwitchboardStack, filterNoteLetter);
                this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
				this.syncSwitchboardToServer();
			} else if(clickedBox(imouseX, imouseY, NOTE_OCTAVE_BUTTON_COORDS)) {
				shiftFilterNoteOctave();
				ItemMidiSwitchboard.setFilterOct(selectedSwitchboardStack, filterNoteOctave);
                this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
				this.syncSwitchboardToServer();
			} else if(clickedBox(imouseX, imouseY, CYCLE_INSTRUMENT_BUTTON_COORDS)) {
				shiftInstrumentId();
				ItemMidiSwitchboard.setInstrument(selectedSwitchboardStack, INSTRUMENT_ID_LIST.get(instrumentIndex));
                this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
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

        return matrixStack;
    }

    @Override
    protected MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY) {
		if(this.selectedSwitchboardStack != null) {
			// Filter Note
			font.drawString(matrixStack, this.filterNoteString, 43, 71, invalidFilterNote ? 0xFFE60000 : 0xFF00E600);

			// Filter Instrument
			font.drawString(matrixStack, ModItems.SWITCHBOARD.getInstrumentName(selectedSwitchboardStack), 193, 18, invalidFilterNote ? 0xFFE60000 : 0xFF00E600);
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
			filterNoteLetter = ItemMidiSwitchboard.getFilterNote(selectedSwitchboardStack);
			filterNoteOctave = ItemMidiSwitchboard.getFilterOct(selectedSwitchboardStack);
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

    public void shiftInstrumentId() {
        if(instrumentIndex < INSTRUMENT_ID_LIST.size()-1) {
            instrumentIndex++;
        } else {
            instrumentIndex = 0;
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