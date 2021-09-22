package io.github.tofodroid.mods.mimi.client.gui;

import java.util.List;
import java.util.stream.Collectors;
import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.tofodroid.mods.mimi.common.container.ASwitchboardContainer;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.SortedArraySet;

public abstract class ASwitchboardBlockGui<T extends ASwitchboardContainer> extends ASwitchboardGui<T> {
    // Button Boxes
    protected static final Vector2f ALL_MIDI_BUTTON_COORDS = new Vector2f(23,48);
    protected static final Vector2f GEN_MIDI_BUTTON_COORDS = new Vector2f(42,48);
    protected static final Vector2f CLEAR_MIDI_BUTTON_COORDS = new Vector2f(23,73);
    protected static final Vector2f FILTER_NOTE_OCTAVE_BUTTON_COORDS = new Vector2f(218,48);
    protected static final Vector2f FILTER_NOTE_LETTER_BUTTON_COORDS = new Vector2f(199,48);
    protected static final Vector2f FILTER_NOTE_INVERT_BUTTON_COORDS = new Vector2f(282,48);
    protected static final Vector2f FILTER_INSTRUMENT_PREV_BUTTON_COORDS = new Vector2f(24,113);
    protected static final Vector2f FILTER_INSTRUMENT_NEXT_BUTTON_COORDS = new Vector2f(148,113);
    protected static final Vector2f FILTER_INSTRUMENT_INVERT_BUTTON_COORDS = new Vector2f(167,113);
    protected static final Vector2f TRANSMIT_SELF_BUTTON_COORDS = new Vector2f(225,97);
    protected static final Vector2f TRANSMIT_PUBLIC_BUTTON_COORDS = new Vector2f(244,97);
    protected static final Vector2f TRANSMIT_CLEAR_BUTTON_COORDS = new Vector2f(263,97);
    protected static final Vector2f BROADCAST_MODE_BUTTON_COORDS = new Vector2f(150,135);
    protected static final Vector2f BROADCAST_NOTE_OCTAVE_BUTTON_COORDS = new Vector2f(218,134);
    protected static final Vector2f BROADCAST_NOTE_LETTER_BUTTON_COORDS = new Vector2f(199,134);

    // Text Boxes
    protected static final Vector2f FILTER_NOTE_TEXTBOX_COORDS = new Vector2f(239,52);
    protected static final Vector2f FILTER_INSTRUMENT_TEXTBOX_COORDS = new Vector2f(45,117);
    protected static final Vector2f LINKED_TRANSMITTER_TEXTBOX_COORDS = new Vector2f(201,85);
    protected static final Vector2f BROADCAST_NOTE_TEXTBOX_COORDS = new Vector2f(239,138);

    // Status Boxes
    protected static final Vector2f MIDI_STATUSBOX_COORDS = new Vector2f(48,66);
    protected static final Vector2f FILTER_NOTE_STATUSBOX_COORDS = new Vector2f(301,54);
    protected static final Vector2f FILTER_INSTRUMENT_STATUSBOX_COORDS = new Vector2f(186,119);
    protected static final Vector2f BROADCAST_MODE_STATUSBOX_COORDS = new Vector2f(168,136);

    // Runtime Data
    protected List<Byte> INSTRUMENT_ID_LIST;
    protected Integer filterInstrumentIndex = 0;
    protected Integer filterNoteOctave;
    protected Integer filterNoteLetter;
    protected String filterNoteString = "";

    public ASwitchboardBlockGui(T container, PlayerInventory inv, ITextComponent textComponent) {
        super(container, inv, 328, 250, 395, "textures/gui/container_generic_switchboard_block.png", textComponent);
    }

    @Override
    public void loadSelectedSwitchboard() {
        super.loadSelectedSwitchboard();
        this.loadFilterLetterAndOctave();
        this.filterInstrumentIndex = INSTRUMENT_ID_LIST().indexOf(ItemMidiSwitchboard.getInstrument(selectedSwitchboardStack));
    }

    @Override
    public void clearSwitchboard() {
        super.clearSwitchboard();
        this.filterNoteLetter = 127;
        this.filterNoteOctave = 127;
        this.filterNoteString = "";
        this.filterInstrumentIndex = null;
    }

    @Override
    public boolean mouseReleased(double dmouseX, double dmouseY, int button) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        
		if(selectedSwitchboardStack != null) {
			if(noteFilterWidgetEnabled() && clickedBox(imouseX, imouseY, FILTER_NOTE_OCTAVE_BUTTON_COORDS)) {
				this.shiftFilterNoteOctave();
			} else if(noteFilterWidgetEnabled() && clickedBox(imouseX, imouseY, FILTER_NOTE_LETTER_BUTTON_COORDS)) {
				this.shiftFilterNoteLetter();
			} else if(noteFilterWidgetEnabled() && clickedBox(imouseX, imouseY, FILTER_NOTE_INVERT_BUTTON_COORDS)) {
				this.toggleInvertFilterNote();
			} else if(linkedTransmitterWidgetEnabled() && clickedBox(imouseX, imouseY, TRANSMIT_SELF_BUTTON_COORDS)) {
				this.setSelfSource();
			} else if(linkedTransmitterWidgetEnabled() && clickedBox(imouseX, imouseY, TRANSMIT_PUBLIC_BUTTON_COORDS)) {
				this.setPublicSource();
			} else if(linkedTransmitterWidgetEnabled() && clickedBox(imouseX, imouseY, TRANSMIT_CLEAR_BUTTON_COORDS)) {
				this.clearSource();
            } else if(instrumentFilterWidgetEnabled() && clickedBox(imouseX, imouseY, FILTER_INSTRUMENT_PREV_BUTTON_COORDS)) {
				this.shiftInstrumentId(false);
			} else if(instrumentFilterWidgetEnabled() && clickedBox(imouseX, imouseY, FILTER_INSTRUMENT_NEXT_BUTTON_COORDS)) {
				this.shiftInstrumentId(true);
			} else if(instrumentFilterWidgetEnabled() && clickedBox(imouseX, imouseY, FILTER_INSTRUMENT_INVERT_BUTTON_COORDS)) {
				ItemMidiSwitchboard.setInvertInstrument(selectedSwitchboardStack, !ItemMidiSwitchboard.getInvertInstrument(selectedSwitchboardStack));
				this.syncSwitchboardToServer();
            } else if(broadcastNoteWidgetEnabled() && clickedBox(imouseX, imouseY, BROADCAST_NOTE_OCTAVE_BUTTON_COORDS)) {
                this.shiftBroadcastNoteOctave();
            } else if(broadcastNoteWidgetEnabled() && clickedBox(imouseX, imouseY, BROADCAST_NOTE_LETTER_BUTTON_COORDS)) {
                this.shiftBroadcastNoteLetter();
            } else if(broadcastModeWidgetEnabled() && clickedBox(imouseX, imouseY, BROADCAST_MODE_BUTTON_COORDS)) {
                ItemMidiSwitchboard.setPublicBroadcast(selectedSwitchboardStack, !ItemMidiSwitchboard.getPublicBroadcast(selectedSwitchboardStack));
				this.syncSwitchboardToServer();
            } else if(channelWidgetEnabled() && clickedBox(imouseX, imouseY, CLEAR_MIDI_BUTTON_COORDS)) {
				this.clearChannels();
			} else if(channelWidgetEnabled() && clickedBox(imouseX, imouseY, ALL_MIDI_BUTTON_COORDS)) {
				this.enableAllChannels();
			} else if(channelWidgetEnabled()) {
				// Individual Midi Channel Buttons
				for(int i = 0; i < 16; i++) {
					Vector2f buttonCoords = new Vector2f(
						GEN_MIDI_BUTTON_COORDS.x + (i % 8) * 19,
						GEN_MIDI_BUTTON_COORDS.y + (i / 8) * 27
					);

					if(clickedBox(imouseX, imouseY, buttonCoords)) {
						this.toggleChannel(i);
						return super.mouseClicked(dmouseX, dmouseY, button);
					}
				}
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

        // GUI Title
        blit(matrixStack, this.guiLeft + new Float(titleBoxPos().x).intValue(), this.guiTop + new Float(titleBoxPos().y).intValue(), this.getBlitOffset(), new Float(titleBoxBlit().x).intValue(), new Float(titleBoxBlit().y).intValue(), new Float(titleBoxSize().x).intValue(), new Float(titleBoxSize().y).intValue(), TEXTURE_SIZE, TEXTURE_SIZE);

        // Switchboard Slot
        blit(matrixStack, this.guiLeft + new Float(switchboardSlotPos().x).intValue(), this.guiTop + new Float(switchboardSlotPos().y).intValue(), this.getBlitOffset(), 143, 367, 140, 28, TEXTURE_SIZE, TEXTURE_SIZE);

        // Widgets
        if(this.selectedSwitchboardStack != null) {
			// Channel Output Status Lights
			SortedArraySet<Byte> acceptedChannels = ItemMidiSwitchboard.getEnabledChannelsSet(this.selectedSwitchboardStack);
			if(acceptedChannels != null && !acceptedChannels.isEmpty()) {
				for(Byte channelId : acceptedChannels) {
                    blit(matrixStack, this.guiLeft + new Float(MIDI_STATUSBOX_COORDS.x).intValue() + 19 * (channelId % 8), this.guiTop + new Float(MIDI_STATUSBOX_COORDS.y).intValue() + (channelId / 8) * 25, this.getBlitOffset(), 213, 281, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
                }
			}

            // Broadcast Mode
            if(ItemMidiSwitchboard.getPublicBroadcast(selectedSwitchboardStack)) {
                blit(matrixStack, this.guiLeft + new Float(BROADCAST_MODE_STATUSBOX_COORDS.x).intValue(), this.guiTop + new Float(BROADCAST_MODE_STATUSBOX_COORDS.y).intValue(), this.getBlitOffset(), 173, 281, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
            } else {
                blit(matrixStack, this.guiLeft + new Float(BROADCAST_MODE_STATUSBOX_COORDS.x).intValue(), this.guiTop + new Float(BROADCAST_MODE_STATUSBOX_COORDS.y).intValue(), this.getBlitOffset(), 186, 281, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
            }
			
        	// Filter Note Invert Status Light
			if(ItemMidiSwitchboard.getInvertNoteOct(selectedSwitchboardStack)) {
				blit(matrixStack, this.guiLeft + new Float(FILTER_NOTE_STATUSBOX_COORDS.x).intValue(), this.guiTop + new Float(FILTER_NOTE_STATUSBOX_COORDS.y).intValue(), this.getBlitOffset(), 213, 281, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
			}

            // Filter Instrument Invert Status Light
            if(ItemMidiSwitchboard.getInvertInstrument(selectedSwitchboardStack)) {
                blit(matrixStack, this.guiLeft + new Float(FILTER_INSTRUMENT_STATUSBOX_COORDS.x).intValue(), this.guiTop + new Float(FILTER_INSTRUMENT_STATUSBOX_COORDS.y).intValue(), this.getBlitOffset(), 213, 281, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
            }
		}

        // Disabled Widgets
        if(!channelWidgetEnabled()) {
            blit(matrixStack, this.guiLeft + 21, this.guiTop + 32, this.getBlitOffset(), 1, 281, 171, 65, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        if(!noteFilterWidgetEnabled()) {
            blit(matrixStack, this.guiLeft + 196, this.guiTop + 32, this.getBlitOffset(), 237, 254, 111, 34, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        if(!instrumentFilterWidgetEnabled()) {
            blit(matrixStack, this.guiLeft + 21, this.guiTop + 100, this.getBlitOffset(), 177, 336, 171, 30, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        if(!linkedTransmitterWidgetEnabled()) {
            blit(matrixStack, this.guiLeft + 196, this.guiTop + 69, this.getBlitOffset(), 237, 289, 111, 46, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        if(!broadcastModeWidgetEnabled()) {
            blit(matrixStack, this.guiLeft + 21, this.guiTop + 133, this.getBlitOffset(), 1, 347, 171, 19, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        if(!broadcastNoteWidgetEnabled()) {
            blit(matrixStack, this.guiLeft + 196, this.guiTop + 118, this.getBlitOffset(), 237, 254, 111, 34, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        return matrixStack;
    }

    @Override
    protected MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY) {
		if(this.selectedSwitchboardStack != null) {
            // MIDI Source Name
            String selectedSourceName = ItemMidiSwitchboard.getMidiSourceName(selectedSwitchboardStack);
			font.drawString(matrixStack, selectedSourceName.length() <= 22 ? selectedSourceName : selectedSourceName.substring(0,21) + "...", new Float(LINKED_TRANSMITTER_TEXTBOX_COORDS.x).intValue(), new Float(LINKED_TRANSMITTER_TEXTBOX_COORDS.y).intValue(), linkedTransmitterWidgetEnabled() ? 0xFF00E600 : 0xFF005C00);

			// Filter Note
			font.drawString(matrixStack, this.filterNoteString, new Float(FILTER_NOTE_TEXTBOX_COORDS.x).intValue(), new Float(FILTER_NOTE_TEXTBOX_COORDS.y).intValue(), noteFilterWidgetEnabled() ? 0xFF00E600 : 0xFF005C00);

			// Filter Instrument
			font.drawString(matrixStack, ModItems.SWITCHBOARD.getInstrumentName(selectedSwitchboardStack), new Float(FILTER_INSTRUMENT_TEXTBOX_COORDS.x).intValue(), new Float(FILTER_INSTRUMENT_TEXTBOX_COORDS.y).intValue(), instrumentFilterWidgetEnabled() ? 0xFF00E600 : 0xFF005C00);

			// Broadcast Note
			font.drawString(matrixStack, ItemMidiSwitchboard.getBroadcastNoteAsString(selectedSwitchboardStack), new Float(BROADCAST_NOTE_TEXTBOX_COORDS.x).intValue(), new Float(BROADCAST_NOTE_TEXTBOX_COORDS.y).intValue(), broadcastNoteWidgetEnabled() ? 0xFF00E600 : 0xFF005C00);
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
            if(filterInstrumentIndex < INSTRUMENT_ID_LIST.size()-1) {
                filterInstrumentIndex++;
            } else {
                filterInstrumentIndex = 0;
            }
        } else {
            if(filterInstrumentIndex > 0) {
                filterInstrumentIndex--;
            } else {
                filterInstrumentIndex = INSTRUMENT_ID_LIST.size()-1;
            }
        }
        
        ItemMidiSwitchboard.setInstrument(selectedSwitchboardStack, INSTRUMENT_ID_LIST.get(filterInstrumentIndex));
        this.syncSwitchboardToServer();
    }
        
    protected void shiftBroadcastNoteLetter() {
        Byte broadcastNote = ItemMidiSwitchboard.getBroadcastNote(selectedSwitchboardStack);

        if(broadcastNote % 12 < 11) {
            if(broadcastNote + 1 <= Byte.MAX_VALUE) {
                broadcastNote++;
            } else {
                broadcastNote = new Integer(broadcastNote - (broadcastNote % 12)).byteValue();
            }
        } else {
            broadcastNote = new Integer(broadcastNote - 11).byteValue();
        }

        ItemMidiSwitchboard.setBroadcastNote(selectedSwitchboardStack, broadcastNote);
        this.syncSwitchboardToServer();
    }
    
    protected void shiftBroadcastNoteOctave() {
        Byte broadcastNote = ItemMidiSwitchboard.getBroadcastNote(selectedSwitchboardStack);

        if((broadcastNote / 12)  < 10) {
            if(broadcastNote + 12 <= Byte.MAX_VALUE) {
                broadcastNote = new Integer(broadcastNote + 12).byteValue();
            } else {
                broadcastNote = new Integer(broadcastNote + - 108).byteValue();
            }
        } else {
            broadcastNote = new Integer(broadcastNote - 120).byteValue();
        }
        ItemMidiSwitchboard.setBroadcastNote(selectedSwitchboardStack, broadcastNote);
        this.syncSwitchboardToServer();
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

        ItemMidiSwitchboard.setFilterNote(selectedSwitchboardStack, filterNoteLetter.byteValue());
        this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
        this.syncSwitchboardToServer();
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
        
        ItemMidiSwitchboard.setFilterOct(selectedSwitchboardStack, filterNoteOctave.byteValue());
        this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
        this.syncSwitchboardToServer();
    }

    protected void toggleInvertFilterNote() {
        ItemMidiSwitchboard.setInvertNoteOct(selectedSwitchboardStack, !ItemMidiSwitchboard.getInvertNoteOct(selectedSwitchboardStack));
        this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
        this.syncSwitchboardToServer();
    }
    
	protected Boolean invalidFilterNote() {
		return new Integer(filterNoteOctave*12+filterNoteLetter) > Byte.MAX_VALUE;
	}

    protected void loadFilterLetterAndOctave() {
		if(this.selectedSwitchboardStack != null) {
			filterNoteLetter = ItemMidiSwitchboard.getFilterNote(selectedSwitchboardStack).intValue();
			filterNoteOctave = ItemMidiSwitchboard.getFilterOct(selectedSwitchboardStack).intValue();
			filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
		} else {
			filterNoteOctave = 127;
			filterNoteLetter = 127;
			filterNoteString = "";
		}       
    }

    protected Boolean channelWidgetEnabled() {return false;}
    protected Boolean noteFilterWidgetEnabled() {return false;}
    protected Boolean instrumentFilterWidgetEnabled() {return false;}
    protected Boolean linkedTransmitterWidgetEnabled() {return false;}
    protected Boolean broadcastModeWidgetEnabled() {return false;}
    protected Boolean broadcastNoteWidgetEnabled() {return false;}
    protected abstract Vector2f titleBoxPos();
    protected abstract Vector2f titleBoxBlit();
    protected abstract Vector2f titleBoxSize();
    protected Vector2f switchboardSlotPos() {return new Vector2f(9,187);}
}
