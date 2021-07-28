package io.github.tofodroid.mods.mimi.client.gui;

import java.util.ArrayList;
import java.util.UUID;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ContainerReceiver;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.SwitchboardStackUpdatePacket;
import io.github.tofodroid.mods.mimi.util.PlayerNameUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;

public class GuiReceiverContainerScreen extends BaseContainerGui<ContainerReceiver> {
    // Button Boxes
    private static final Vector2f SOURCE_SELF_BUTTON_COORDS = new Vector2f(40,112);
    private static final Vector2f SOURCE_PUBLIC_BUTTON_COORDS = new Vector2f(59,112);
    private static final Vector2f SOURCE_CLEAR_BUTTON_COORDS = new Vector2f(78,112);
    private static final Vector2f NOTE_LETTER_BUTTON_COORDS = new Vector2f(73,59);
    private static final Vector2f NOTE_OCTAVE_BUTTON_COORDS = new Vector2f(92,59);
    private static final Vector2f ALL_MIDI_BUTTON_COORDS = new Vector2f(121,28);
    private static final Vector2f GEN_MIDI_BUTTON_COORDS = new Vector2f(140,28);
    private static final Vector2f CLEAR_MIDI_BUTTON_COORDS = new Vector2f(121,53);

	// Input Data
    private final PlayerEntity player;

    // Runtime Data
    private Integer filterNoteOctave = -1;
    private Integer filterNoteLetter = -1;
    private Boolean invalidFilterNote = false;
    private String filterNoteString = "";
    private String selectedSourceName = "";
	private ItemStack selectedSwitchboardStack;
    
    public GuiReceiverContainerScreen(ContainerReceiver container, PlayerInventory inv, ITextComponent textComponent) {
        super(container, inv, 303, 172, 303, "textures/gui/container_receiver.png", textComponent);
        this.player = inv.player;
		
        if(ModItems.SWITCHBOARD.equals(container.getSlot(ContainerReceiver.TARGET_CONTAINER_MIN_SLOT_ID).getStack().getItem())) {
            this.selectedSwitchboardStack = container.getSlot(ContainerReceiver.TARGET_CONTAINER_MIN_SLOT_ID).getStack();
			this.loadLetterAndOctave();
			this.refreshSourceName();
        }
    }

    @Override
    public boolean mouseReleased(double dmouseX, double dmouseY, int button) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        
		if(selectedSwitchboardStack != null) {
			if(clickedBox(imouseX, imouseY, SOURCE_SELF_BUTTON_COORDS)) {
				ItemMidiSwitchboard.setMidiSource(selectedSwitchboardStack, this.player.getUniqueID());
				this.syncReceiverToServer();
				this.refreshSourceName();
			} else if(clickedBox(imouseX, imouseY, SOURCE_PUBLIC_BUTTON_COORDS)) {
				ItemMidiSwitchboard.setMidiSource(selectedSwitchboardStack, ItemMidiSwitchboard.PUBLIC_SOURCE_ID);
				this.syncReceiverToServer();
				this.refreshSourceName();
			} else if(clickedBox(imouseX, imouseY, SOURCE_CLEAR_BUTTON_COORDS)) {
				ItemMidiSwitchboard.setMidiSource(selectedSwitchboardStack, null);
				this.syncReceiverToServer();
				this.refreshSourceName();
			} else if(clickedBox(imouseX, imouseY, NOTE_LETTER_BUTTON_COORDS)) {
				shiftFilterNoteLetter();
				ItemMidiSwitchboard.setFilterNoteString(selectedSwitchboardStack, buildFilterNoteString());
				this.syncReceiverToServer();
			} else if(clickedBox(imouseX, imouseY, NOTE_OCTAVE_BUTTON_COORDS)) {
				shiftFilterNoteOctave();
				ItemMidiSwitchboard.setFilterNoteString(selectedSwitchboardStack, buildFilterNoteString());
				this.syncReceiverToServer();
			} else if(clickedBox(imouseX, imouseY, CLEAR_MIDI_BUTTON_COORDS)) {
				ItemMidiSwitchboard.clearEnabledChannels(selectedSwitchboardStack);
				this.syncReceiverToServer();
			} else if(clickedBox(imouseX, imouseY, ALL_MIDI_BUTTON_COORDS)) {
				ItemMidiSwitchboard.setEnableAllChannels(selectedSwitchboardStack);
				this.syncReceiverToServer();
			} else {
				// Individual Midi Channel Buttons
				for(int i = 0; i < 16; i++) {
					Vector2f buttonCoords = new Vector2f(
						GEN_MIDI_BUTTON_COORDS.x + (i % 8) * 19,
						GEN_MIDI_BUTTON_COORDS.y + (i / 8) * 25
					);

					if(clickedBox(imouseX, imouseY, buttonCoords)) {
						ItemMidiSwitchboard.toggleChannel(selectedSwitchboardStack, new Integer(i).byteValue());
						this.syncReceiverToServer();
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

        // Channel Output Status Lights
		if(this.selectedSwitchboardStack != null) {
			SortedArraySet<Byte> acceptedChannels = ItemMidiSwitchboard.getEnabledChannelsSet(this.selectedSwitchboardStack);

			if(acceptedChannels != null && !acceptedChannels.isEmpty()) {
				for(Byte channelId : acceptedChannels) {
                    blit(matrixStack, this.guiLeft + 146 + 19 * (channelId % 8), this.guiTop + 46 + (channelId / 8) * 25, this.getBlitOffset(), 0, 172, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
                }
			}
		}

        return matrixStack;
    }

    @Override
    protected MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY) {
		if(this.selectedSwitchboardStack != null) {
			// MIDI Source Name
			font.drawString(matrixStack, this.selectedSourceName.length() <= 22 ? this.selectedSourceName : this.selectedSourceName.substring(0,21) + "...", 16, 100, 0xFF00E600);
		
			// Filter Note
			font.drawString(matrixStack, this.filterNoteString, 43, 63, invalidFilterNote ? 0xFFE60000 : 0xFF00E600);
		}
       
        return matrixStack;
    }
	
    @Override
    public void tick() {
        super.tick();

        if(this.selectedSwitchboardStack == null && ModItems.SWITCHBOARD.equals(container.getSlot(ContainerReceiver.TARGET_CONTAINER_MIN_SLOT_ID).getStack().getItem())) {
            this.selectedSwitchboardStack = container.getSlot(ContainerReceiver.TARGET_CONTAINER_MIN_SLOT_ID).getStack();
			this.loadLetterAndOctave();
			this.refreshSourceName();
        } else if(selectedSwitchboardStack != null && !ModItems.SWITCHBOARD.equals(container.getSlot(ContainerReceiver.TARGET_CONTAINER_MIN_SLOT_ID).getStack().getItem())) {
            this.selectedSwitchboardStack = null;
			this.loadLetterAndOctave();
			this.refreshSourceName();
        }
    }

    private void refreshSourceName() {
		if(this.selectedSwitchboardStack != null) {
			UUID sourceId = ItemMidiSwitchboard.getMidiSource(selectedSwitchboardStack);
			if(sourceId != null) {
				if(sourceId.equals(player.getUniqueID())) {
					this.selectedSourceName = "My Transmitter";
				} else if(sourceId.equals(ItemMidiSwitchboard.SYS_SOURCE_ID)) {
					this.selectedSourceName = "MIDI Input Device";
				} else if(sourceId.equals(ItemMidiSwitchboard.PUBLIC_SOURCE_ID)) {
					this.selectedSourceName = "Public Transmitters";
				} else if(this.minecraft != null && this.minecraft.world != null) {
					this.selectedSourceName = PlayerNameUtils.getPlayerNameFromUUID(sourceId, this.minecraft.world);
				} else {
					this.selectedSourceName = "Unknown";
				}
			} else {
				this.selectedSourceName = "None";
			}
		} else {
			this.selectedSourceName = "";
		}
    }

    public void loadLetterAndOctave() {

		if(this.selectedSwitchboardStack != null) {
			ArrayList<Byte> filteredNoteList = ItemMidiSwitchboard.getFilterNotes(selectedSwitchboardStack);
        
			if(filteredNoteList != null && !filteredNoteList.isEmpty()) {
				if(filteredNoteList.size() == 1) {
					// Single Note
					if(filteredNoteList.get(0) < 0) {
						filterNoteOctave = 10;
						filterNoteLetter = (127 - filteredNoteList.get(0).intValue()) % 12;
						invalidFilterNote = true;
					} else {
						filterNoteOctave = (filteredNoteList.get(0).byteValue()) / 12;
						filterNoteLetter = filteredNoteList.get(0).intValue() % 12;
					}
				} else if(filteredNoteList.get(1) - filteredNoteList.get(0) == 1) {
					// Any Letter
					filterNoteOctave = (filteredNoteList.get(0).byteValue()) / 12;
				} else {
					// Any Octave
					filterNoteLetter = filteredNoteList.get(0).intValue() % 12;
				}
			}
			this.updateFilterNoteString();
		} else {
			filterNoteOctave = -1;
			filterNoteLetter = -1;
			filterNoteString = "";
		}       
    }

    public String buildFilterNoteString() {
        String filterString = "";
        Integer filterNoteLetterMidi = filterNoteLetter >= 0 ? filterNoteLetter % 12 : -1;
        invalidFilterNote = false;

        if(filterNoteLetterMidi >= 0 && filterNoteOctave >= 0) {
            Integer midiNote = 12 * filterNoteOctave;
            midiNote += filterNoteLetterMidi;

            if(midiNote > 127) {
                invalidFilterNote = true;
                filterString = new Integer(127 - midiNote).toString();
            } else {
                filterString = midiNote.toString();
            }
        } else if(filterNoteLetterMidi >= 0) {
            Integer midiNote = filterNoteLetterMidi;
            for(int i = 0; i < 11; i++) {
                Integer note = (midiNote + i * 12);
                if(note <= Byte.MAX_VALUE) {
                    filterString += note + ",";
                }
            }
            filterString = filterString.substring(0, filterString.length()-1);
        } else if(filterNoteOctave >= 0) {
            Integer midiNote = filterNoteOctave*12;
            for(int i = 0; i < 12; i++) {
                Integer note = (midiNote + i);
                if(note <= Byte.MAX_VALUE) {
                    filterString += note + ",";
                }
            }
            filterString = filterString.substring(0, filterString.length()-1);
        }
        
        MIMIMod.LOGGER.info("Saved Note String: " + filterString);
        return filterString;
    }

    public void updateFilterNoteString() {
        filterNoteString = noteLetterFromNum(filterNoteLetter) + (filterNoteOctave >= 0 ? filterNoteOctave : "*");
        filterNoteString = "**".equals(filterNoteString) ? "All" : filterNoteString;
    }

    public void shiftFilterNoteLetter() {
        if(filterNoteLetter < 11) {
            filterNoteLetter++;
        } else {
            filterNoteLetter = -1;
        }
        updateFilterNoteString();
    }
    
    public void shiftFilterNoteOctave() {
        if(filterNoteOctave < 10) {
            filterNoteOctave++;
        } else {
            filterNoteOctave = -1;
        }
        updateFilterNoteString();
    }

    private String noteLetterFromNum(Integer octaveNoteNum) {
        switch(octaveNoteNum) {
            case -1:
                return "*";
            case 0:
                return "C";
            case 1:
                return "C#";
            case 2:
                return "D";
            case 3:
                return "D#";
            case 4:
                return "E";
            case 5:
                return "F";
            case 6:
                return "F#";
            case 7:
                return "G";
            case 8:
                return "G#";
            case 9:
                return "A";
            case 10:
                return "A#";
            case 11:
                return "B";
        }

        return "";
    }

    public void syncReceiverToServer() {
        SwitchboardStackUpdatePacket packet = null;

        if(selectedSwitchboardStack != null && ModItems.SWITCHBOARD.equals(selectedSwitchboardStack.getItem())) {
            packet = ItemMidiSwitchboard.getSyncPacket(selectedSwitchboardStack);
        }        
        if(packet != null) {
            NetworkManager.NET_CHANNEL.sendToServer(packet);
        }
    }  
}