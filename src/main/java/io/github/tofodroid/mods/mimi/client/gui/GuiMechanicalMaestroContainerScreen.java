package io.github.tofodroid.mods.mimi.client.gui;

import java.util.UUID;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ContainerMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.SwitchboardStackUpdatePacket;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import io.github.tofodroid.mods.mimi.util.PlayerNameUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;

public class GuiMechanicalMaestroContainerScreen extends BaseContainerGui<ContainerMechanicalMaestro> {
    // Button Boxes
    private static final Vector2f SOURCE_SELF_BUTTON_COORDS = new Vector2f(40,151);
    private static final Vector2f SOURCE_PUBLIC_BUTTON_COORDS = new Vector2f(59,151);
    private static final Vector2f SOURCE_CLEAR_BUTTON_COORDS = new Vector2f(78,151);
    private static final Vector2f ALL_MIDI_BUTTON_COORDS = new Vector2f(131,118);
    private static final Vector2f GEN_MIDI_BUTTON_COORDS = new Vector2f(150,118);
    private static final Vector2f CLEAR_MIDI_BUTTON_COORDS = new Vector2f(131,144);

	// Input Data
    private final PlayerEntity player;

    // Runtime Data
    private String selectedSourceName = "";
	private ItemStack selectedSwitchboardStack;
    
    public GuiMechanicalMaestroContainerScreen(ContainerMechanicalMaestro container, PlayerInventory inv, ITextComponent textComponent) {
        super(container, inv, 311, 180, 311, "textures/gui/container_mech_maestro.png", textComponent);
        this.player = inv.player;
		
        if(ModItems.SWITCHBOARD.equals(container.getSlot(ContainerMechanicalMaestro.TARGET_CONTAINER_MIN_SLOT_ID).getStack().getItem())) {
            this.selectedSwitchboardStack = container.getSlot(ContainerMechanicalMaestro.TARGET_CONTAINER_MIN_SLOT_ID).getStack();
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
						GEN_MIDI_BUTTON_COORDS.y + (i / 8) * 27
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

		if(this.selectedSwitchboardStack != null) {
			// Channel Output Status Lights
			SortedArraySet<Byte> acceptedChannels = ItemMidiSwitchboard.getEnabledChannelsSet(this.selectedSwitchboardStack);
			if(acceptedChannels != null && !acceptedChannels.isEmpty()) {
				for(Byte channelId : acceptedChannels) {
                    blit(matrixStack, this.guiLeft + 156 + 19 * (channelId % 8), this.guiTop + 136 + (channelId / 8) * 26, this.getBlitOffset(), 0, 181, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
                }
			}
		}

        return matrixStack;
    }

    @Override
    protected MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY) {
		if(this.selectedSwitchboardStack != null) {
			// MIDI Source Name
			font.drawString(matrixStack, this.selectedSourceName.length() <= 22 ? this.selectedSourceName : this.selectedSourceName.substring(0,21) + "...", 16, 139, 0xFF00E600);
		}
       
        return matrixStack;
    }
	
    @Override
    public void tick() {
        super.tick();

        if(this.selectedSwitchboardStack == null && ModItems.SWITCHBOARD.equals(container.getSlot(ContainerMechanicalMaestro.TARGET_CONTAINER_MIN_SLOT_ID).getStack().getItem())) {
            this.selectedSwitchboardStack = container.getSlot(ContainerMechanicalMaestro.TARGET_CONTAINER_MIN_SLOT_ID).getStack();
			this.refreshSourceName();
            this.allNotesOff();
        } else if(selectedSwitchboardStack != null && !ModItems.SWITCHBOARD.equals(container.getSlot(ContainerMechanicalMaestro.TARGET_CONTAINER_MIN_SLOT_ID).getStack().getItem())) {
            this.selectedSwitchboardStack = null;
			this.refreshSourceName();
            this.allNotesOff();
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

    public void syncSwitchboardToServer() {
        SwitchboardStackUpdatePacket packet = null;

        if(selectedSwitchboardStack != null && ModItems.SWITCHBOARD.equals(selectedSwitchboardStack.getItem())) {
            packet = ItemMidiSwitchboard.getSyncPacket(selectedSwitchboardStack);
        }

        if(packet != null) {
            NetworkManager.NET_CHANNEL.sendToServer(packet);
            this.allNotesOff();
        }
    }

    private void allNotesOff() {
		TileEntity tile = player.world.getTileEntity(container.getTilePos());
		TileMechanicalMaestro mechTile = tile != null && ModTiles.MECHANICALMAESTRO.equals(tile.getType()) ? (TileMechanicalMaestro) tile : null;

		if(mechTile != null && mechTile.getInstrumentId() != null) {
			MidiNotePacket packet = new MidiNotePacket(MidiNotePacket.NO_CHANNEL, MidiNotePacket.ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), mechTile.getInstrumentId(), mechTile.getMaestroUUID(), true, mechTile.getPos());
			NetworkManager.NET_CHANNEL.sendToServer(packet);
			MIMIMod.proxy.getMidiSynth().handlePacket(packet);
		}
    }
}