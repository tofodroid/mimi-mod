package io.github.tofodroid.mods.mimi.client.gui;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import io.github.tofodroid.mods.mimi.common.container.ASwitchboardContainer;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.entity.player.Inventory;

public abstract class ASwitchboardBlockGui<T extends ASwitchboardContainer> extends ASwitchboardGui<T> {
    // Button Boxes
    protected static final Vector3f ALL_MIDI_BUTTON_COORDS = new Vector3f(16,48,0);
    protected static final Vector3f GEN_MIDI_BUTTON_COORDS = new Vector3f(35,48,0);
    protected static final Vector3f CLEAR_MIDI_BUTTON_COORDS = new Vector3f(16,73,0);
    protected static final Vector3f FILTER_NOTE_OCTAVE_BUTTON_COORDS = new Vector3f(211,48,0);
    protected static final Vector3f FILTER_NOTE_LETTER_BUTTON_COORDS = new Vector3f(192,48,0);
    protected static final Vector3f FILTER_NOTE_INVERT_BUTTON_COORDS = new Vector3f(275,48,0);
    protected static final Vector3f FILTER_INSTRUMENT_PREV_BUTTON_COORDS = new Vector3f(17,113,0);
    protected static final Vector3f FILTER_INSTRUMENT_NEXT_BUTTON_COORDS = new Vector3f(141,113,0);
    protected static final Vector3f FILTER_INSTRUMENT_INVERT_BUTTON_COORDS = new Vector3f(160,113,0);
    protected static final Vector3f TRANSMIT_SELF_BUTTON_COORDS = new Vector3f(218,97,0);
    protected static final Vector3f TRANSMIT_PUBLIC_BUTTON_COORDS = new Vector3f(237,97,0);
    protected static final Vector3f TRANSMIT_CLEAR_BUTTON_COORDS = new Vector3f(256,97,0);
    protected static final Vector3f BROADCAST_MODE_BUTTON_COORDS = new Vector3f(143,135,0);
    protected static final Vector3f BROADCAST_NOTE_OCTAVE_BUTTON_COORDS = new Vector3f(211,134,0);
    protected static final Vector3f BROADCAST_NOTE_LETTER_BUTTON_COORDS = new Vector3f(192,134,0);
    protected static final Vector3f INSTRUMENT_VOLUME_UP_BUTTON_COORDS = new Vector3f(306,74,0);
    protected static final Vector3f INSTRUMENT_VOLUME_DOWN_BUTTON_COORDS = new Vector3f(306,110,0);

    // Text Boxes
    protected static final Vector3f FILTER_NOTE_TEXTBOX_COORDS = new Vector3f(232,52,0);
    protected static final Vector3f FILTER_INSTRUMENT_TEXTBOX_COORDS = new Vector3f(38,117,0);
    protected static final Vector3f LINKED_TRANSMITTER_TEXTBOX_COORDS = new Vector3f(194,85,0);
    protected static final Vector3f BROADCAST_NOTE_TEXTBOX_COORDS = new Vector3f(232,138,0);
    protected static final Vector3f INSTRUMENT_VOLUME_TEXTBOX_COORDS = new Vector3f(308,96,0);

    // Status Boxes
    protected static final Vector3f MIDI_STATUSBOX_COORDS = new Vector3f(41,66,0);
    protected static final Vector3f FILTER_NOTE_STATUSBOX_COORDS = new Vector3f(294,54,0);
    protected static final Vector3f FILTER_INSTRUMENT_STATUSBOX_COORDS = new Vector3f(179,119,0);
    protected static final Vector3f BROADCAST_MODE_STATUSBOX_COORDS = new Vector3f(161,136,0);

    // Runtime Data
    protected List<Byte> INSTRUMENT_ID_LIST;
    protected Integer filterInstrumentIndex = 0;
    protected Integer filterNoteOctave;
    protected Integer filterNoteLetter;
    protected String filterNoteString = "";

    public ASwitchboardBlockGui(T container, Inventory inv, Component textComponent) {
        super(container, inv, 337, 250, 395, "textures/gui/container_generic_switchboard_block.png", textComponent);
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
            } else if(instrumentVolumeWidgetEnabled() && clickedBox(imouseX, imouseY, INSTRUMENT_VOLUME_UP_BUTTON_COORDS)) {
                this.changeVolume(1);
            } else if(instrumentVolumeWidgetEnabled() && clickedBox(imouseX, imouseY, INSTRUMENT_VOLUME_DOWN_BUTTON_COORDS)) {
                this.changeVolume(-1);
            } else if(channelWidgetEnabled() && clickedBox(imouseX, imouseY, CLEAR_MIDI_BUTTON_COORDS)) {
				this.clearChannels();
			} else if(channelWidgetEnabled() && clickedBox(imouseX, imouseY, ALL_MIDI_BUTTON_COORDS)) {
				this.enableAllChannels();
			} else if(channelWidgetEnabled()) {
				// Individual Midi Channel Buttons
				for(int i = 0; i < 16; i++) {
					Vector3f buttonCoords = new Vector3f(
						GEN_MIDI_BUTTON_COORDS.x() + (i % 8) * 19,
						GEN_MIDI_BUTTON_COORDS.y() + (i / 8) * 27,
                        0
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
    protected PoseStack renderGraphics(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // GUI Background
        blit(matrixStack, START_X, START_Y, this.getBlitOffset(), 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // GUI Title
        blit(matrixStack, START_X + Float.valueOf(titleBoxPos().x()).intValue(), START_Y + Float.valueOf(titleBoxPos().y()).intValue(), this.getBlitOffset(), Float.valueOf(titleBoxBlit().x()).intValue(), Float.valueOf(titleBoxBlit().y()).intValue(), Float.valueOf(titleBoxSize().x()).intValue(), Float.valueOf(titleBoxSize().y()).intValue(), TEXTURE_SIZE, TEXTURE_SIZE);

        // Switchboard Slot
        blit(matrixStack, START_X + Float.valueOf(switchboardSlotPos().x()).intValue(), START_Y + Float.valueOf(switchboardSlotPos().y()).intValue(), this.getBlitOffset(), 143, 367, 140, 28, TEXTURE_SIZE, TEXTURE_SIZE);

        // Widgets
        if(this.selectedSwitchboardStack != null) {
			// Channel Output Status Lights
			SortedArraySet<Byte> acceptedChannels = ItemMidiSwitchboard.getEnabledChannelsSet(this.selectedSwitchboardStack);
			if(acceptedChannels != null && !acceptedChannels.isEmpty()) {
				for(Byte channelId : acceptedChannels) {
                    blit(matrixStack, START_X + Float.valueOf(MIDI_STATUSBOX_COORDS.x()).intValue() + 19 * (channelId % 8), START_Y + Float.valueOf(MIDI_STATUSBOX_COORDS.y()).intValue() + (channelId / 8) * 25, this.getBlitOffset(), 213, 281, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
                }
			}

            // Broadcast Mode
            if(ItemMidiSwitchboard.getPublicBroadcast(selectedSwitchboardStack)) {
                blit(matrixStack, START_X + Float.valueOf(BROADCAST_MODE_STATUSBOX_COORDS.x()).intValue(), START_Y + Float.valueOf(BROADCAST_MODE_STATUSBOX_COORDS.y()).intValue(), this.getBlitOffset(), 173, 281, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
            } else {
                blit(matrixStack, START_X + Float.valueOf(BROADCAST_MODE_STATUSBOX_COORDS.x()).intValue(), START_Y + Float.valueOf(BROADCAST_MODE_STATUSBOX_COORDS.y()).intValue(), this.getBlitOffset(), 186, 281, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
            }
			
        	// Filter Note Invert Status Light
			if(ItemMidiSwitchboard.getInvertNoteOct(selectedSwitchboardStack)) {
				blit(matrixStack, START_X + Float.valueOf(FILTER_NOTE_STATUSBOX_COORDS.x()).intValue(), START_Y + Float.valueOf(FILTER_NOTE_STATUSBOX_COORDS.y()).intValue(), this.getBlitOffset(), 213, 281, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
			}

            // Filter Instrument Invert Status Light
            if(ItemMidiSwitchboard.getInvertInstrument(selectedSwitchboardStack)) {
                blit(matrixStack, START_X + Float.valueOf(FILTER_INSTRUMENT_STATUSBOX_COORDS.x()).intValue(), START_Y + Float.valueOf(FILTER_INSTRUMENT_STATUSBOX_COORDS.y()).intValue(), this.getBlitOffset(), 213, 281, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
            }
		}

        // Disabled Widgets
        if(!channelWidgetEnabled()) {
            blit(matrixStack, START_X + 14, START_Y + 32, this.getBlitOffset(), 1, 281, 171, 65, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        if(!noteFilterWidgetEnabled()) {
            blit(matrixStack, START_X + 189, START_Y + 32, this.getBlitOffset(), 237, 254, 111, 34, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        if(!instrumentFilterWidgetEnabled()) {
            blit(matrixStack, START_X + 14, START_Y + 100, this.getBlitOffset(), 177, 336, 171, 30, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        if(!linkedTransmitterWidgetEnabled()) {
            blit(matrixStack, START_X + 189, START_Y + 69, this.getBlitOffset(), 237, 289, 111, 46, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        if(!broadcastModeWidgetEnabled()) {
            blit(matrixStack, START_X + 14, START_Y + 133, this.getBlitOffset(), 1, 347, 171, 19, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        if(!broadcastNoteWidgetEnabled()) {
            blit(matrixStack, START_X + 189, START_Y + 118, this.getBlitOffset(), 237, 254, 111, 34, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        if(!instrumentVolumeWidgetEnabled()) {
            blit(matrixStack, START_X + 304, START_Y + 58, this.getBlitOffset(), 217, 266, 19, 69, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        return matrixStack;
    }

    @Override
    protected PoseStack renderText(PoseStack matrixStack, int mouseX, int mouseY) {
		if(this.selectedSwitchboardStack != null) {
            // MIDI Source Name
            String selectedSourceName = ItemMidiSwitchboard.getMidiSourceName(selectedSwitchboardStack);
			font.draw(matrixStack, selectedSourceName.length() <= 22 ? selectedSourceName : selectedSourceName.substring(0,21) + "...", Float.valueOf(LINKED_TRANSMITTER_TEXTBOX_COORDS.x()).intValue(), Float.valueOf(LINKED_TRANSMITTER_TEXTBOX_COORDS.y()).intValue(), linkedTransmitterWidgetEnabled() ? 0xFF00E600 : 0xFF005C00);

			// Filter Note
			font.draw(matrixStack, this.filterNoteString, Float.valueOf(FILTER_NOTE_TEXTBOX_COORDS.x()).intValue(), Float.valueOf(FILTER_NOTE_TEXTBOX_COORDS.y()).intValue(), noteFilterWidgetEnabled() ? 0xFF00E600 : 0xFF005C00);

			// Filter Instrument
			font.draw(matrixStack, ModItems.SWITCHBOARD.getInstrumentName(selectedSwitchboardStack), Float.valueOf(FILTER_INSTRUMENT_TEXTBOX_COORDS.x()).intValue(), Float.valueOf(FILTER_INSTRUMENT_TEXTBOX_COORDS.y()).intValue(), instrumentFilterWidgetEnabled() ? 0xFF00E600 : 0xFF005C00);

			// Broadcast Note
			font.draw(matrixStack, ItemMidiSwitchboard.getBroadcastNoteAsString(selectedSwitchboardStack), Float.valueOf(BROADCAST_NOTE_TEXTBOX_COORDS.x()).intValue(), Float.valueOf(BROADCAST_NOTE_TEXTBOX_COORDS.y()).intValue(), broadcastNoteWidgetEnabled() ? 0xFF00E600 : 0xFF005C00);
            
            // Instrument Volume
			font.draw(matrixStack, ItemMidiSwitchboard.getInstrumentVolumePercent(selectedSwitchboardStack).toString(), Float.valueOf(INSTRUMENT_VOLUME_TEXTBOX_COORDS.x()).intValue(), Float.valueOf(INSTRUMENT_VOLUME_TEXTBOX_COORDS.y()).intValue(), instrumentVolumeWidgetEnabled() ? 0xFF00E600 : 0xFF005C00);
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
                broadcastNote = Integer.valueOf(broadcastNote - (broadcastNote % 12)).byteValue();
            }
        } else {
            broadcastNote = Integer.valueOf(broadcastNote - 11).byteValue();
        }

        ItemMidiSwitchboard.setBroadcastNote(selectedSwitchboardStack, broadcastNote);
        this.syncSwitchboardToServer();
    }
    
    protected void shiftBroadcastNoteOctave() {
        Byte broadcastNote = ItemMidiSwitchboard.getBroadcastNote(selectedSwitchboardStack);

        if((broadcastNote / 12)  < 10) {
            if(broadcastNote + 12 <= Byte.MAX_VALUE) {
                broadcastNote = Integer.valueOf(broadcastNote + 12).byteValue();
            } else {
                broadcastNote = Integer.valueOf(broadcastNote + - 108).byteValue();
            }
        } else {
            broadcastNote = Integer.valueOf(broadcastNote - 120).byteValue();
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
		return Integer.valueOf(filterNoteOctave*12+filterNoteLetter) > Byte.MAX_VALUE;
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
    protected Boolean instrumentVolumeWidgetEnabled() {return false;}
    protected abstract Vector3f titleBoxPos();
    protected abstract Vector3f titleBoxBlit();
    protected abstract Vector3f titleBoxSize();
    protected Vector3f switchboardSlotPos() {return new Vector3f(9,187,0);}
}
