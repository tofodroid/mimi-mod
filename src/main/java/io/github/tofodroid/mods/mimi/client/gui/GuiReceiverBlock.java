package io.github.tofodroid.mods.mimi.client.gui;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.data.ReceiverDataUtil;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ReceiverTileDataUpdatePacket;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.util.PlayerNameUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;

public class GuiReceiverBlock extends BaseGui {
    // Button Boxes
    private static final Vector2f SOURCE_SELF_BUTTON_COORDS = new Vector2f(184,38);
    private static final Vector2f SOURCE_PUBLIC_BUTTON_COORDS = new Vector2f(203,38);
    private static final Vector2f SOURCE_CLEAR_BUTTON_COORDS = new Vector2f(222,38);
    private static final Vector2f NOTE_LETTER_BUTTON_COORDS = new Vector2f(320,38);
    private static final Vector2f NOTE_OCTAVE_BUTTON_COORDS = new Vector2f(339,38);
    private static final Vector2f CLEAR_MIDI_BUTTON_COORDS = new Vector2f(15,78);
    private static final Vector2f ALL_MIDI_BUTTON_COORDS = new Vector2f(338,78);
    private static final Vector2f GEN_MIDI_BUTTON_COORDS = new Vector2f(34,78);

    // Data
    private Integer filterNoteOctave = -1;
    private Integer filterNoteLetter = -1;
    private Boolean invalidFilterNote = false;
    private String filterNoteString = "All";
    private String midiSourceName;
    private final PlayerEntity player;
    private final World worldIn;
    private final TileReceiver receiverData;
    private final ReceiverDataUtil dataUtil;
    

    public GuiReceiverBlock(PlayerEntity player, World worldIn, TileReceiver receiverData, ReceiverDataUtil dataUtil) {
        super(368, 112, 368, "textures/gui/gui_receiver.png",  "item.MIMIMod.gui_receiver_block");
        this.player = player;
        this.worldIn = worldIn;
        this.receiverData = receiverData;
        this.dataUtil = dataUtil;
        this.loadLetterAndOctave();
        this.refreshSourceName();
    }

    @Override
    public boolean mouseReleased(double dmouseX, double dmouseY, int button) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        
        if(clickedBox(imouseX, imouseY, SOURCE_SELF_BUTTON_COORDS)) {
            dataUtil.setMidiSource(receiverData, this.player.getUniqueID());
            this.syncReceiverToServer();
            this.refreshSourceName();
        } else if(clickedBox(imouseX, imouseY, SOURCE_PUBLIC_BUTTON_COORDS)) {
            dataUtil.setMidiSource(receiverData, ReceiverDataUtil.PUBLIC_SOURCE_ID);
            this.syncReceiverToServer();
            this.refreshSourceName();
        } else if(clickedBox(imouseX, imouseY, SOURCE_CLEAR_BUTTON_COORDS)) {
            dataUtil.setMidiSource(receiverData, null);
            this.syncReceiverToServer();
            this.refreshSourceName();
        } else if(clickedBox(imouseX, imouseY, NOTE_LETTER_BUTTON_COORDS)) {
            shiftFilterNoteLetter();
            dataUtil.setFilterNoteString(receiverData, buildFilterNoteString());
            this.syncReceiverToServer();
        } else if(clickedBox(imouseX, imouseY, NOTE_OCTAVE_BUTTON_COORDS)) {
            shiftFilterNoteOctave();
            dataUtil.setFilterNoteString(receiverData, buildFilterNoteString());
            this.syncReceiverToServer();
        } else if(clickedBox(imouseX, imouseY, CLEAR_MIDI_BUTTON_COORDS)) {
            dataUtil.clearAcceptedChannels(receiverData);
            this.syncReceiverToServer();
        } else if(clickedBox(imouseX, imouseY, ALL_MIDI_BUTTON_COORDS)) {
            dataUtil.setAcceptAllChannels(receiverData);
            this.syncReceiverToServer();
        } else {
            // Individual Midi Channel Buttons
            for(int i = 0; i < 16; i++) {
                Vector2f buttonCoords = new Vector2f(
                    GEN_MIDI_BUTTON_COORDS.x + i * 19,
                    GEN_MIDI_BUTTON_COORDS.y
                );

                if(clickedBox(imouseX, imouseY, buttonCoords)) {
                    dataUtil.toggleChannel(receiverData, new Integer(i).byteValue());
                    this.syncReceiverToServer();
                    return super.mouseReleased(dmouseX, dmouseY, button);
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
        blit(matrixStack, START_X, START_Y, this.getBlitOffset(), 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Channel Output Status Lights
        SortedArraySet<Byte> acceptedChannels = this.dataUtil.getAcceptedChannelsSet(this.receiverData);

        if(acceptedChannels != null && !acceptedChannels.isEmpty()) {
            for(Byte channelId : acceptedChannels) {
                blit(matrixStack, START_X + 40 + 19 * channelId, START_Y + 96, this.getBlitOffset(), 0, 113, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }

        return matrixStack;
    }

    @Override
    protected MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // MIDI Source Name
        font.drawString(matrixStack, this.midiSourceName.length() <= 22 ? this.midiSourceName : this.midiSourceName.substring(0,21) + "...", START_X + 81, START_Y + 42, 0xFF00E600);
       
        // Filter Note
        font.drawString(matrixStack, this.filterNoteString, START_X + 280, START_Y + 42, invalidFilterNote ? 0xFFE60000 : 0xFF00E600);
       
        return matrixStack;
    }

    private void refreshSourceName() {
        UUID midiSource = dataUtil.getMidiSource(receiverData);
        if(midiSource != null) {
            if(midiSource.equals(player.getUniqueID())) {
                this.midiSourceName = "My Transmitter";
            } else if(midiSource.equals(ReceiverDataUtil.SYS_SOURCE_ID)) {
                this.midiSourceName = "MIDI Input Device";
            } else if(midiSource.equals(ReceiverDataUtil.PUBLIC_SOURCE_ID)) {
                this.midiSourceName = "Public Transmitters";
            } else {
                this.midiSourceName = PlayerNameUtils.getPlayerNameFromUUID(midiSource, worldIn);
            }
        } else {
            this.midiSourceName = "None";
        }
    }

    public void loadLetterAndOctave() {
        MIMIMod.LOGGER.info("Loaded Note String: " + dataUtil.getFilterNoteString(receiverData));
        ArrayList<Byte> filteredNoteList = dataUtil.getFilterNotes(receiverData);
        MIMIMod.LOGGER.info("Loaded Note Set: " + filteredNoteList.stream().map(b -> b.toString()).collect(Collectors.joining(",")));
        
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
    }

    public void syncReceiverToServer() {
        ReceiverTileDataUpdatePacket packet = null;
        packet = TileReceiver.getSyncPacket(receiverData);
        
        if(packet != null) {
            NetworkManager.NET_CHANNEL.sendToServer(packet);
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
}