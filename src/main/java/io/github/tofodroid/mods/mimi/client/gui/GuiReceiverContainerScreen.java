package io.github.tofodroid.mods.mimi.client.gui;

import java.util.UUID;

import com.mojang.blaze3d.matrix.MatrixStack;

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
				this.syncSwitchboardToServer();
				this.refreshSourceName();
			} else if(clickedBox(imouseX, imouseY, SOURCE_PUBLIC_BUTTON_COORDS)) {
				ItemMidiSwitchboard.setMidiSource(selectedSwitchboardStack, ItemMidiSwitchboard.PUBLIC_SOURCE_ID);
				this.syncSwitchboardToServer();
				this.refreshSourceName();
			} else if(clickedBox(imouseX, imouseY, SOURCE_CLEAR_BUTTON_COORDS)) {
				ItemMidiSwitchboard.setMidiSource(selectedSwitchboardStack, null);
				this.syncSwitchboardToServer();
				this.refreshSourceName();
			} else if(clickedBox(imouseX, imouseY, NOTE_LETTER_BUTTON_COORDS)) {
				shiftFilterNoteLetter();
				ItemMidiSwitchboard.setFilterNote(selectedSwitchboardStack, filterNoteLetter);
                this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
				this.syncSwitchboardToServer();
			} else if(clickedBox(imouseX, imouseY, NOTE_OCTAVE_BUTTON_COORDS)) {
				shiftFilterNoteOctave();
				ItemMidiSwitchboard.setFilterOct(selectedSwitchboardStack, filterNoteOctave);
                this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
				this.syncSwitchboardToServer();
			} else if(clickedBox(imouseX, imouseY, CLEAR_MIDI_BUTTON_COORDS)) {
				ItemMidiSwitchboard.clearEnabledChannels(selectedSwitchboardStack);
				this.syncSwitchboardToServer();
			} else if(clickedBox(imouseX, imouseY, ALL_MIDI_BUTTON_COORDS)) {
				ItemMidiSwitchboard.setEnableAllChannels(selectedSwitchboardStack);
				this.syncSwitchboardToServer();
			} else {
				// Individual Midi Channel Buttons
				for(int i = 0; i < 16; i++) {
					Vector2f buttonCoords = new Vector2f(
						GEN_MIDI_BUTTON_COORDS.x + (i % 8) * 19,
						GEN_MIDI_BUTTON_COORDS.y + (i / 8) * 25
					);

					if(clickedBox(imouseX, imouseY, buttonCoords)) {
						ItemMidiSwitchboard.toggleChannel(selectedSwitchboardStack, new Integer(i).byteValue());
						this.syncSwitchboardToServer();
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
					this.selectedSourceName = player.getName().getString();
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
			this.filterNoteLetter = ItemMidiSwitchboard.getFilterNote(selectedSwitchboardStack);
			this.filterNoteOctave = ItemMidiSwitchboard.getFilterOct(selectedSwitchboardStack);
            this.filterNoteString = ItemMidiSwitchboard.getFilteredNotesAsString(selectedSwitchboardStack);
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