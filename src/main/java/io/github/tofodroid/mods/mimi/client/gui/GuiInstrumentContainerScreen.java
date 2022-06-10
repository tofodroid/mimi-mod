package io.github.tofodroid.mods.mimi.client.gui;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.SyncItemInstrumentSwitchboardPacket;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.common.config.ClientConfig;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;

import org.lwjgl.glfw.GLFW;

import io.github.tofodroid.mods.mimi.common.container.ContainerInstrument;

public class GuiInstrumentContainerScreen extends ASwitchboardGui<ContainerInstrument> {
    // Texture
    private static final Integer NOTE_WIDTH = 14;
    private static final Integer NOTE_OFFSET_X = 11;
    private static final Integer NOTE_OFFSET_Y = 29;

    // GUI
    private static final Vector3f SYS_DEVICE_BUTTON_COORDS = new Vector3f(105,84,0);
    private static final Vector3f SOURCE_SELF_BUTTON_COORDS = new Vector3f(47,133,0);
    private static final Vector3f SOURCE_PUBLIC_BUTTON_COORDS = new Vector3f(66,133,0);
    private static final Vector3f SOURCE_CLEAR_BUTTON_COORDS = new Vector3f(85,133,0);
    private static final Vector3f KEYBOARD_LAYOUT_BUTTON_COORDS = new Vector3f(300,31,0);
    private static final Vector3f ALL_MIDI_BUTTON_COORDS = new Vector3f(141,101,0);
    private static final Vector3f CLEAR_MIDI_BUTTON_COORDS = new Vector3f(141,126,0);
    private static final Vector3f GEN_MIDI_BUTTON_COORDS = new Vector3f(160,101,0);
    private static final Vector3f NOTE_SHIFT_DOWN_BUTTON_COORDS = new Vector3f(14,177,0);
    private static final Vector3f NOTE_SHIFT_UP_BUTTON_COORDS = new Vector3f(59,177,0);
    private static final Vector3f OCT_SHIFT_DOWN_BUTTON_COORDS = new Vector3f(84,177,0);
    private static final Vector3f OCT_SHIFT_UP_BUTTON_COORDS = new Vector3f(129,177,0);
    private static final Vector3f SWITCHBOARD_EDIT_BUTTON_COORDS = new Vector3f(105,219,0);
    private static final Vector3f INSTRUMENT_VOLUME_UP_BUTTON_COORDS = new Vector3f(202,58,0);
    private static final Vector3f INSTRUMENT_VOLUME_DOWN_BUTTON_COORDS = new Vector3f(155,58,0);

    // Keyboard
    private static final Integer KEYBOARD_START_NOTE = 21;
    private static final Integer VISIBLE_NOTES = 44;
    private static final Integer MAX_NOTE_SHIFT = 53;

    // MIMI Layout
    private final Integer ACCENT_LEFT_MIN_SCAN = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_S);
    private final Integer ACCENT_LEFT_MAX_SCAN = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_APOSTROPHE);
    private final Integer ACCENT_RIGHT_MIN_SCAN = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_1);
    private final Integer ACCENT_RIGHT_MAX_SCAN = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_EQUAL);
    private final Integer NOTE_LEFT_MIN_SCAN = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_Z);
    private final Integer NOTE_LEFT_MAX_SCAN = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_SLASH);
    private final Integer NOTE_RIGHT_MIN_SCAN = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_Q);
    private final Integer NOTE_RIGHT_MAX_SCAN = GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_RIGHT_BRACKET);

    // VPiano Layout
    private final Integer V_PIANO_MIN_SHIFT = 14;
    private final Integer V_PIANO_MAX_SHIFT = 28;
    private final Integer V_PIANO_MAX_NOTE = 49;
    private final Map<Integer,Integer> VPianoMidiMap = Stream.of(new Integer[][] {
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_1), 0},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_1), 1},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_2), 2},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_2), 3},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_3), 4},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_4), 5},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_4), 6},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_5), 7},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_5), 8},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_6), 9},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_6), 10},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_7), 11},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_8), 12},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_8), 13},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_9), 14},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_9), 15},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_0), 16},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_Q), 17},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_Q), 18},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_W), 19},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_W), 20},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_E), 21},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_E), 22},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_R), 23},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_T), 24},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_T), 25},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_Y), 26},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_Y), 27},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_U), 28},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_I), 29},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_I), 30},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_O), 31},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_O), 32},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_P), 33},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_P), 34},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_A), 35},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_S), 36},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_S), 37},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_D), 38},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_D), 39},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_F), 40},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_G), 41},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_G), 42},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_H), 43},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_H), 44},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_J), 45},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_J), 46},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_K), 47},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_L), 48},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_L), 49},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_Z), 50},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_Z), 51},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_X), 52},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_C), 53},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_C), 54},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_V), 55},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_V), 56},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_B), 57},
        {-GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_B), 58},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_N), 59},
        {GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_M), 60}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    // Input Data
    private final Byte instrumentId;
    private final InteractionHand handIn;
    private final BlockPos tilePos;

    // Runtime Data
    private ConcurrentHashMap<Byte, Instant> heldNotes;
    private ConcurrentHashMap<Byte, Instant> releasedNotes;
    private String instrumentNameString;
    private Boolean editMode = false;
    private Integer visibleNoteShift = KEYBOARD_START_NOTE;
    private String noteIdString = "C3,F4 | G4,C6";
    private Byte mouseNote = null;

    public GuiInstrumentContainerScreen(ContainerInstrument container, Inventory inv, Component textComponent) {
        super(container, inv, 328, 250, 530, "textures/gui/container_instrument.png", textComponent);
        this.instrumentId = container.getInstrumentId();

        if(container.isHandheld()) {
            this.handIn = container.getHandIn();
            this.tilePos = null;
        } else {
            this.handIn = null;
            this.tilePos = container.getTilePos();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        super.init();
        this.heldNotes = new ConcurrentHashMap<>();
        this.releasedNotes = new ConcurrentHashMap<>();

        if(container.isHandheld()) {
            this.instrumentNameString = ItemInstrument.getInstrumentName(this.player.getItemInHand(handIn));
        } else {
            this.instrumentNameString = ((TileInstrument)this.minecraft.level.getBlockEntity(tilePos)).getInstrumentName();
        }
    }

    @Override
    protected Boolean shouldRenderBackground() {
        return false;
    }

    @Override
    public void onClose() {
        this.toggleHoldPedal(false);
        this.allNotesOff();
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double dmouseX, double dmouseY, int mouseButton) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        int firstNoteX = START_X + NOTE_OFFSET_X;
        int firstNoteY = START_Y + NOTE_OFFSET_Y;
        int relativeMouseX = imouseX - firstNoteX;
        int relativeMouseY = imouseY - firstNoteY;

        if(clickedBox(imouseX, imouseY, SWITCHBOARD_EDIT_BUTTON_COORDS) && this.selectedSwitchboardStack != null) {
            editMode = !editMode;
        } else if(!editMode) {
            // Keyboard Layout Hover Box
            if(imouseX >= 220 && imouseY >= 28 && imouseX < (START_X + this.GUI_WIDTH) && imouseY < (START_Y + 50)) {
                if(clickedBox(imouseX, imouseY, KEYBOARD_LAYOUT_BUTTON_COORDS)) {
                    if(ModConfigs.CLIENT.keyboardLayout.get().ordinal() < ClientConfig.KEYBOARD_LAYOUTS.values().length - 1) {
                        ModConfigs.CLIENT.keyboardLayout.set(ClientConfig.KEYBOARD_LAYOUTS.values()[ModConfigs.CLIENT.keyboardLayout.get().ordinal()+1]);
                    } else {
                        ModConfigs.CLIENT.keyboardLayout.set(ClientConfig.KEYBOARD_LAYOUTS.values()[0]);
                    }
                    this.allNotesOff();
                }

                return super.mouseClicked(dmouseX, dmouseY, mouseButton);
            }
            
            // Note Keys
            if(relativeMouseX >= 0 && relativeMouseY >= 0 && imouseX < (START_X + this.GUI_WIDTH - 11) && imouseY < (START_Y + this.GUI_HEIGHT - 95)) {
                Byte midiNote = null;
                
                if(relativeMouseY <= 84) {
                    midiNote = keyNumToMidiNote(2*((relativeMouseX + NOTE_WIDTH/2) / NOTE_WIDTH));
                }

                if(midiNote == null) {
                    midiNote = keyNumToMidiNote(2*(relativeMouseX / NOTE_WIDTH)+1);
                }
                
                if(midiNote != null) {
                    this.mouseNote = midiNote;
                    this.onGuiNotePress(midiNote, Byte.MAX_VALUE);
                }
            }

            // Keyboard Controls
            if(clickedBox(imouseX, imouseY, NOTE_SHIFT_UP_BUTTON_COORDS)) {
                this.shiftVisibleNotes(true, 1);
            } else if(clickedBox(imouseX, imouseY, NOTE_SHIFT_DOWN_BUTTON_COORDS)) {
                this.shiftVisibleNotes(false, 1);
            } else if(clickedBox(imouseX, imouseY, OCT_SHIFT_UP_BUTTON_COORDS)) {
                this.shiftVisibleNotes(true, 7);
            } else if(clickedBox(imouseX, imouseY, OCT_SHIFT_DOWN_BUTTON_COORDS)) {
                this.shiftVisibleNotes(false, 7);
            } 
        } else {
            // Switchboard MIDI Controls
            if(clickedBox(imouseX, imouseY, SYS_DEVICE_BUTTON_COORDS)) {
                // Toggle Sys Device Button
                ItemMidiSwitchboard.setSysInput(selectedSwitchboardStack, !ItemMidiSwitchboard.getSysInput(selectedSwitchboardStack));
                this.syncSwitchboardToServer();
                this.allNotesOff();
            } else if(clickedBox(imouseX, imouseY, SOURCE_SELF_BUTTON_COORDS)) {
               this.setSelfSource();
               this.allNotesOff();
            } else if(clickedBox(imouseX, imouseY, SOURCE_PUBLIC_BUTTON_COORDS)) {
                this.setPublicSource();
                this.allNotesOff();
            } else if(clickedBox(imouseX, imouseY, SOURCE_CLEAR_BUTTON_COORDS)) {
                this.clearSource();
                this.allNotesOff();
            } else if(clickedBox(imouseX, imouseY, INSTRUMENT_VOLUME_UP_BUTTON_COORDS)) {
                this.changeVolume(1);
            } else if(clickedBox(imouseX, imouseY, INSTRUMENT_VOLUME_DOWN_BUTTON_COORDS)) {
                this.changeVolume(-1);
            } else if(clickedBox(imouseX, imouseY, CLEAR_MIDI_BUTTON_COORDS)) {
                this.clearChannels();
                this.allNotesOff();
            } else if(clickedBox(imouseX, imouseY, ALL_MIDI_BUTTON_COORDS)) {
                this.enableAllChannels();
                this.allNotesOff();
            } else {
                // Individual Midi Channel Buttons
                for(int i = 0; i < 16; i++) {
                    Vector3f buttonCoords = new Vector3f(
                        GEN_MIDI_BUTTON_COORDS.x() + (i % 8) * 19,
                        GEN_MIDI_BUTTON_COORDS.y() + (i / 8) * 25,
                        0
                    );

                    if(clickedBox(imouseX, imouseY, buttonCoords)) {
                        this.toggleChannel(i);
                        this.allNotesOff();
                        return super.mouseClicked(dmouseX, dmouseY, mouseButton);
                    }
                }
            }
        }
        
        return super.mouseClicked(dmouseX, dmouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(this.mouseNote != null) {
            this.onGuiNoteRelease(mouseNote);
            this.mouseNote = null;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        
        if(keyCode == GLFW.GLFW_KEY_LEFT) {
            shiftVisibleNotes(false, 1);
        } else if(keyCode == GLFW.GLFW_KEY_RIGHT) {
            shiftVisibleNotes(true, 1);
        } else if(keyCode == GLFW.GLFW_KEY_DOWN) {
            shiftVisibleNotes(false, 7);
        } else if(keyCode == GLFW.GLFW_KEY_UP) {
            shiftVisibleNotes(true, 7);
        } else if(keyCode == GLFW.GLFW_KEY_SPACE) {
            this.toggleHoldPedal(true);
        } else {
            Set<Byte> midiNoteNums = getMidiNoteFromScanCode(scanCode, modifiers == 1, false);

            if(midiNoteNums != null) {
                for(Byte midiNoteNum : midiNoteNums) {
                    if(!this.heldNotes.containsKey(midiNoteNum)) {
                        this.onGuiNotePress(midiNoteNum, Byte.MAX_VALUE);
                    }
                }
            }
        }
        
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        super.keyReleased(keyCode, scanCode, modifiers);

        if(keyCode == GLFW.GLFW_KEY_SPACE) {
            this.toggleHoldPedal(false);
        } else {
            Set<Byte> midiNoteNums = getMidiNoteFromScanCode(scanCode, modifiers == 1, true);

            if(midiNoteNums != null) {
                for(Byte midiNoteNum : midiNoteNums) {
                    this.onGuiNoteRelease(midiNoteNum);
                }
            }
        }

        return true;
    }

    @Override
    public void loadSelectedSwitchboard() {
        super.loadSelectedSwitchboard();
        this.allNotesOff();
        this.syncInstrumentToServer();
    }

    @Override
    public void clearSwitchboard() {
        super.clearSwitchboard();
        this.editMode = false;
        this.allNotesOff();
        this.syncInstrumentToServer();
    }

    private void toggleHoldPedal(Boolean on) {
        Byte controller = 64;
        Byte value = on ? Byte.MAX_VALUE : Byte.MIN_VALUE;
        MidiNotePacket packet = MidiNotePacket.createControlPacket(controller, value, instrumentId, player.getUUID(), player.getOnPos());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
        MIMIMod.proxy.getMidiSynth().handlePacket(packet);
    }

    // Midi Functions
    private Byte keyNumToMidiNote(Integer keyNum) {
        if(keyNum == null) {
            return null;
        }

        Byte result = null;
        Integer octaveNote = (keyNum + 2 * visibleNoteShift) % 14;
        Integer octaveNum = (keyNum + 2 * visibleNoteShift) / 14;

        if(octaveNote != 0 && octaveNote != 6) {
            octaveNote -= octaveNote > 6 ? 2 : 1;
            result = Integer.valueOf(octaveNote + 12 * octaveNum).byteValue();
        }

        return result;
    }

    private Integer midiNoteToKeyNum(Byte midiNote) {
        if(midiNote == null) {
            return null;
        }

        Integer octaveNote = midiNote % 12;
        Integer octaveNum = midiNote / 12;
        octaveNote += octaveNote > 4 ? 2 : 1;
        Integer result = octaveNote + 14 * (octaveNum) - 2 * visibleNoteShift;

        if(result >= 0 && result <= VISIBLE_NOTES) {
            return result;
        }
        return null;
    }

    private void shiftVisibleNotes(Boolean up, Integer amount) {
        if(up) {
            visibleNoteShift += amount;
        } else {
            visibleNoteShift -= amount;
        }

        // Clamp between 0 and 53
        visibleNoteShift = visibleNoteShift < 0 ? 0 : visibleNoteShift > MAX_NOTE_SHIFT ? MAX_NOTE_SHIFT : visibleNoteShift;
        
        this.noteIdString = buildNoteIdString();
    }

    public void onMidiNoteOn(Byte channel, Byte midiNote, Byte velocity) {
        this.holdNote(midiNote, velocity);
    }

    public void onMidiNoteOff(Byte channel, Byte midiNote) {
        this.releaseNote(midiNote);
    }

    private void allNotesOff() {
        // Release all notes
        if(this.heldNotes != null && this.instrumentId != null) {
            List<Byte> notesToRemove = new ArrayList<>(this.heldNotes.keySet());
            for(Byte note : notesToRemove) {
                this.releaseNote(note);
            }

            MidiNotePacket packet = MidiNotePacket.createAllNotesOffPacket(instrumentId, player.getUUID(), player.getOnPos());
            NetworkManager.NET_CHANNEL.sendToServer(packet);
            MIMIMod.proxy.getMidiSynth().handlePacket(packet);
        }
    }

    private void onGuiNotePress(Byte midiNote, Byte velocity) {
        MidiNotePacket packet = new MidiNotePacket(midiNote, ItemMidiSwitchboard.applyVolume(selectedSwitchboardStack, velocity), instrumentId, player.getUUID(), player.getOnPos());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
        MIMIMod.proxy.getMidiSynth().handlePacket(packet);
        this.onMidiNoteOn(null, midiNote, velocity);
    }

    private void onGuiNoteRelease(Byte midiNote) {
        MidiNotePacket packet = new MidiNotePacket(midiNote, Integer.valueOf(0).byteValue(), instrumentId, player.getUUID(), player.getOnPos());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
        MIMIMod.proxy.getMidiSynth().handlePacket(packet);
        this.onMidiNoteOff(null, midiNote);
    }

    private void holdNote(Byte midiNote, Byte velocity) {
        this.releasedNotes.remove(midiNote);
        this.heldNotes.put(midiNote, Instant.now());
    }

    private void releaseNote(Byte midiNote) {
        if(this.heldNotes.remove(midiNote) != null) {
            this.releasedNotes.put(midiNote, Instant.now());
        }
    }

    // Keyboard Input Functions
    private Set<Byte> getMidiNoteFromScanCode(Integer scanCode, Boolean modifier, Boolean ignoreModifier) {
        switch(ModConfigs.CLIENT.keyboardLayout.get()) {
            case MIMI:
                return Arrays.asList(getMidiNoteFromScanCode_MIMI(scanCode))
                    .stream().filter(b -> b != null).collect(Collectors.toSet());
            case VPiano:
                return Arrays.asList(getMidiNoteFromScanCode_VPiano(scanCode, modifier), ignoreModifier ? getMidiNoteFromScanCode_VPiano(scanCode, !modifier) : null)
                    .stream().filter(b -> b != null).collect(Collectors.toSet());
            default:
                MIMIMod.LOGGER.info("Warning: Unknown keyboard layout selected for Instrument GUI.");
                return null;
        }
    }

    private Byte getMidiNoteFromScanCode_MIMI(Integer scanCode) {
        Integer keyNum = null;

        if (scanCode >= ACCENT_LEFT_MIN_SCAN && scanCode <= ACCENT_LEFT_MAX_SCAN) {
            //Accent note - 1st row
            keyNum = scanCode - ACCENT_LEFT_MIN_SCAN + 1;
            keyNum *= 2;
        } else if(scanCode >= NOTE_LEFT_MIN_SCAN && scanCode <= NOTE_LEFT_MAX_SCAN) {
            //Primary note 1st row
            keyNum = scanCode - NOTE_LEFT_MIN_SCAN + 1;
            keyNum += (keyNum-1);
        } else if(scanCode >= ACCENT_RIGHT_MIN_SCAN && scanCode <= ACCENT_RIGHT_MAX_SCAN) {
            //Accent note - 2nd row
            keyNum = scanCode - ACCENT_RIGHT_MIN_SCAN + (ACCENT_LEFT_MAX_SCAN - ACCENT_LEFT_MIN_SCAN) + 1;
            keyNum *= 2;
        } else if(scanCode >= NOTE_RIGHT_MIN_SCAN && scanCode <= NOTE_RIGHT_MAX_SCAN) {
            //Primary note 2nd row
            keyNum = scanCode - NOTE_RIGHT_MIN_SCAN + (NOTE_LEFT_MAX_SCAN - NOTE_LEFT_MIN_SCAN) + 2;
            keyNum += (keyNum-1);
        }

        if(keyNum != null) {
            Byte result = keyNumToMidiNote(keyNum);
            return result;
        }

        return null;
    }

    private Byte getMidiNoteFromScanCode_VPiano(Integer scanCode, Boolean modifier) {
        Integer midiInt = VPianoMidiMap.get(scanCode * (modifier ? -1 : 1));
        return midiInt != null ?  Integer.valueOf(12 * 2 + midiInt).byteValue() : null;
    }

    // Render Functions
    @Override
    protected PoseStack renderGraphics(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        setAlpha(1.0f);

        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // Visible Notes
        Integer keyboardTextureShift = (visibleNoteShift % (NOTE_WIDTH/2)) * NOTE_WIDTH;
        blit(matrixStack, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y - 1, this.getBlitOffset(), keyboardTextureShift, 276, 308, 128, TEXTURE_SIZE, TEXTURE_SIZE);

        // Note Labels
        if(ClientConfig.KEYBOARD_LAYOUTS.MIMI.equals(ModConfigs.CLIENT.keyboardLayout.get())) {
            blit(matrixStack, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y + 70, this.getBlitOffset(), 0, 457, 308, 53, TEXTURE_SIZE, TEXTURE_SIZE);
        } else {
            if(visibleNoteShift < V_PIANO_MIN_SHIFT) {
                Integer widthShift = (V_PIANO_MIN_SHIFT - visibleNoteShift) * NOTE_WIDTH;
                blit(matrixStack, START_X + NOTE_OFFSET_X - 1 + widthShift, START_Y + NOTE_OFFSET_Y + 70, this.getBlitOffset(), 0, 404, 308 - widthShift, 53, TEXTURE_SIZE, TEXTURE_SIZE);
            } else if(visibleNoteShift >= V_PIANO_MIN_SHIFT && visibleNoteShift <= V_PIANO_MAX_SHIFT) {
                blit(matrixStack, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y + 70, this.getBlitOffset(), (visibleNoteShift - V_PIANO_MIN_SHIFT) * NOTE_WIDTH, 404, 308, 53, TEXTURE_SIZE, TEXTURE_SIZE);
            } else if(visibleNoteShift <= V_PIANO_MAX_NOTE) {
                Integer widthShift = (V_PIANO_MAX_SHIFT - visibleNoteShift) * -NOTE_WIDTH;
                blit(matrixStack, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y + 70, this.getBlitOffset(), (visibleNoteShift - V_PIANO_MIN_SHIFT) * NOTE_WIDTH, 404, 308 - widthShift, 53, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }

        // Note Edges
        if(visibleNoteShift == 0) {
            blit(matrixStack, START_X + NOTE_OFFSET_X, START_Y + NOTE_OFFSET_Y, this.getBlitOffset(), 392, 276, 6, 86, TEXTURE_SIZE, TEXTURE_SIZE);
        } else if(visibleNoteShift == MAX_NOTE_SHIFT) {
            blit(matrixStack, START_X + 311, START_Y + NOTE_OFFSET_Y, this.getBlitOffset(), 392, 276, 6, 86, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        
        // Active Notes
        matrixStack = renderAndCleanNoteSet(matrixStack, this.heldNotes, 5000, true, entry -> {this.onGuiNoteRelease(entry.getKey());});
        matrixStack = renderAndCleanNoteSet(matrixStack, this.releasedNotes, 1000, false, entry -> {this.releasedNotes.remove(entry.getKey());});

        // Reset alpha for next layers
        setAlpha(1.0f);

        // GUI Background
        blit(matrixStack, START_X, START_Y, this.getBlitOffset(), 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Note Key Covers
        blit(matrixStack, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y + 55, this.getBlitOffset(), keyboardTextureShift, 250, 308, 26, TEXTURE_SIZE, TEXTURE_SIZE);
        
        // Switchboard Edit Panel
        if(editMode) {
            // Switchboard Background Panel
            matrixStack.pushPose();
            matrixStack.mulPose(new Quaternion(new Vector3f(0,0,1),-90,true));
            blit(matrixStack, -(START_Y + 29 + 126),  START_X + 11, this.getBlitOffset(), 404, 0, 126, 306, TEXTURE_SIZE, TEXTURE_SIZE);
            matrixStack.popPose();

            // Sys MIDI Device Status Light
            if(ItemMidiSwitchboard.getSysInput(selectedSwitchboardStack)) {
                blit(matrixStack, START_X + 124, START_Y + 90, this.getBlitOffset(), 329, 42, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
            }

            // Channel Output Status Lights
            SortedArraySet<Byte> acceptedChannels = ItemMidiSwitchboard.getEnabledChannelsSet(this.selectedSwitchboardStack);

            if(acceptedChannels != null && !acceptedChannels.isEmpty()) {
                for(Byte channelId : acceptedChannels) {
                    blit(matrixStack, START_X + 166 + 19 * (channelId % 8), START_Y + 119 + (channelId / 8) * 25, this.getBlitOffset(), 329, 42, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
                }
            }
        }
        
        return matrixStack;
    }

    private PoseStack renderAndCleanNoteSet(PoseStack matrixStack, ConcurrentHashMap<Byte,Instant> noteMap, Integer sustainMillis, Boolean held, Consumer<Entry<Byte,Instant>> removeHandler) {
        List<Entry<Byte,Instant>> notesToRemove = new ArrayList<>();
        if(!noteMap.isEmpty()) {
            for(Entry<Byte,Instant> entry : noteMap.entrySet()) {
                if(Math.abs(ChronoUnit.MILLIS.between(Instant.now(), entry.getValue())) > sustainMillis) {
                    notesToRemove.add(entry);
                }
                matrixStack = this.renderNote(matrixStack, entry.getKey(), held, entry.getValue());
            }

            notesToRemove.forEach(entry -> removeHandler.accept(entry));
        }

        return matrixStack;
    }

    private PoseStack renderNote(PoseStack matrixStack, Byte note, Boolean held, Instant releaseTime) {
        Float alpha = 1.0f;
        Integer keyNum = midiNoteToKeyNum(note);

        // If we can't find a visible key for the note then skip rendering
        if(keyNum == null) {
            return matrixStack;
        }

        if(!held) {
            alpha -= Math.min(Math.abs(ChronoUnit.MILLIS.between(Instant.now(), releaseTime))/1000f, 1.0f);
        }
        
        setAlpha(alpha);

        blit(
            matrixStack, 
            START_X + NOTE_OFFSET_X + (keyNum - 1) * NOTE_WIDTH/2, 
            START_Y + NOTE_OFFSET_Y + 43 + (keyNum % 2) * 42, 
            this.getBlitOffset(), 
            342 - (keyNum % 2) * 13, 
            0, 12, 41, 
            TEXTURE_SIZE, TEXTURE_SIZE
        );
        
        return matrixStack;
    }

    @Override
    protected PoseStack renderText(PoseStack matrixStack, int mouseX, int mouseY) {
        // Instrument Name
        font.draw(matrixStack, this.instrumentNameString, 198, 13, 0xFF00E600);

        // Note Text: Left
        font.draw(matrixStack, this.noteIdString.split(",")[0], 22, 197, 0xFF00E600);

        // Note Text: Middle
        font.draw(matrixStack, this.noteIdString.split(",")[1], 67, 197, 0xFF00E600);

        // Note Text: Right
        font.draw(matrixStack, this.noteIdString.split(",")[2], 126, 197, 0xFF00E600);

        // MIDI Source Name & Volume
        if(editMode) {
            String selectedSourceName = ItemMidiSwitchboard.getMidiSourceName(selectedSwitchboardStack);
            font.draw(matrixStack, selectedSourceName.length() <= 22 ? selectedSourceName : selectedSourceName.substring(0,21) + "...", 21, 122, 0xFF00E600);
            font.draw(matrixStack, ItemMidiSwitchboard.getInstrumentVolumePercent(selectedSwitchboardStack).toString(), 180, 62, 0xFF00E600);
        }

        // Keyboard Layout
        if(editMode) {
            font.draw(matrixStack, ModConfigs.CLIENT.keyboardLayout.get().toString(), 264, 35, 0xFF003600);
        } else { 
            font.draw(matrixStack, ModConfigs.CLIENT.keyboardLayout.get().toString(), 264, 35, 0xFF00E600);
        }

        return matrixStack;
    }

    private String buildNoteIdString() {
        String result = "";    
        result += noteLetterFromNum(visibleNoteShift % 7) + Integer.valueOf(visibleNoteShift / 7).toString();
        result += "," + noteLetterFromNum((visibleNoteShift+10) % 7) + Integer.valueOf((visibleNoteShift+10) / 7).toString();
        result += " | " + noteLetterFromNum((visibleNoteShift+11) % 7) + Integer.valueOf((visibleNoteShift+11) / 7).toString();
        result += "," + noteLetterFromNum((visibleNoteShift+21) % 7) + Integer.valueOf((visibleNoteShift+21) / 7).toString();
        return result;
    }

    private String noteLetterFromNum(Integer octaveNoteNum) {
        switch(octaveNoteNum) {
            case 0:
                return "C";
            case 1:
                return "D";
            case 2:
                return "E";
            case 3:
                return "F";
            case 4:
                return "G";
            case 5:
                return "A";
            case 6:
                return "B";
        }

        return "";
    }

    public void syncInstrumentToServer() {
        SyncItemInstrumentSwitchboardPacket packet = new SyncItemInstrumentSwitchboardPacket(true);
        
        if(packet != null) {
            NetworkManager.NET_CHANNEL.sendToServer(packet);
        }
    }    
}
