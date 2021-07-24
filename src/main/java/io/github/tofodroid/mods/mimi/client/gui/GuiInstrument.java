package io.github.tofodroid.mods.mimi.client.gui;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.network.InstrumentDataUpdatePacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNoteOffPacket;
import io.github.tofodroid.mods.mimi.common.network.MidiNoteOnPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.common.config.ClientConfig;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.data.InstrumentDataUtil;
import io.github.tofodroid.mods.mimi.util.PlayerNameUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;

import org.lwjgl.glfw.GLFW;

public class GuiInstrument<T> extends BaseGui {
    // Texture
    private static final Integer NOTE_WIDTH = 14;
    private static final Integer NOTE_OFFSET_X = 11;
    private static final Integer NOTE_OFFSET_Y = 109;

    // GUI
    private static final Vector2f MAESTRO_MIDI_BUTTON_COORDS = new Vector2f(184,39);
    private static final Vector2f MAESTRO_SELF_BUTTON_COORDS = new Vector2f(202,39);
    private static final Vector2f MAESTRO_PUBLIC_BUTTON_COORDS = new Vector2f(220,39);
    private static final Vector2f MAESTRO_CLEAR_BUTTON_COORDS = new Vector2f(238,39);
    private static final Vector2f KEYBOARD_LAYOUT_BUTTON_COORDS = new Vector2f(339,39);
    private static final Vector2f CLEAR_MIDI_BUTTON_COORDS = new Vector2f(15,79);
    private static final Vector2f ALL_MIDI_BUTTON_COORDS = new Vector2f(338,79);
    private static final Vector2f GEN_MIDI_BUTTON_COORDS = new Vector2f(34,79);
    private static final Vector2f GEN_SHIFT_BUTTON_COORDS = new Vector2f(327,124);
    
    // MIDI
    private static final Integer KEYBOARD_START_NOTE = 21;

    // Mouse
    private Byte mouseNote = null;

    //Keyboard
    private static final Integer VISIBLE_NOTES = 44;
    private static final Integer MAX_NOTE_SHIFT = 53;
    private Integer visibleNoteShift = KEYBOARD_START_NOTE;
    private String minNoteString = "C3";
    private ConcurrentHashMap<Byte, Instant> heldNotes = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Byte, Instant> releasedNotes = new ConcurrentHashMap<>();

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

    // Data
    private final Byte instrumentId;
    private final T instrumentData;
    private final InstrumentDataUtil<T> instrumentUtil;
    private final PlayerEntity player;
    private final Hand handIn;
    private final World world;
    private String selectedMaestroName = "None";
    private String instrumentNameString;

    public GuiInstrument(PlayerEntity player, World worldIn, Byte instrumentId, T instrumentData, InstrumentDataUtil<T> instrumentUtil, Hand handIn) {
        super(368, 246, 530, "textures/gui/gui_instrument.png", "item.MIMIMod.gui_instrument");
        this.instrumentId = instrumentId;
        this.instrumentData = instrumentData;
        this.instrumentUtil = instrumentUtil;
        this.instrumentNameString = instrumentUtil.getInstrumentName(instrumentData);
        this.player = player;
        this.handIn = handIn;
        this.world = worldIn;
        this.refreshMaestroName();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        super.init();
        this.heldNotes.clear();
        this.releasedNotes.clear();
    }
    
    @Override
    public void closeScreen() {
        super.closeScreen();
        this.allNotesOff();
    }

    @Override
    public boolean mouseClicked(double dmouseX, double dmouseY, int mouseButton) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        int firstNoteX = START_X + NOTE_OFFSET_X;
        int firstNoteY = START_Y + NOTE_OFFSET_Y;
        int relativeMouseX = imouseX - firstNoteX;
        int relativeMouseY = imouseY - firstNoteY;

        // Check if click position is within keyboard
        if(relativeMouseX >= 0 && relativeMouseY >= 0 && imouseX < (START_X + GUI_WIDTH - 51) && imouseY < (START_Y + GUI_HEIGHT - 11)) {
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

        // Shift Buttons
        for(int i = 0; i < 4; i++) {
            Vector2f buttonCoords = new Vector2f(
                GEN_SHIFT_BUTTON_COORDS.x,
                GEN_SHIFT_BUTTON_COORDS.y + i * 19
            );

            if(clickedBox(imouseX, imouseY, buttonCoords)) {
                switch(i) {
                    case 0:
                        this.shiftVisibleNotes(true, 1);
                        break;
                    case 1:
                        this.shiftVisibleNotes(false, 1);
                        break;
                    case 2:
                        this.shiftVisibleNotes(true, 7);
                        break;
                    case 3:
                        this.shiftVisibleNotes(false, 7);
                        break;
                }
            }
        }

        // Midi Buttons
        if(instrumentData != null) {
            if(clickedBox(imouseX, imouseY, MAESTRO_MIDI_BUTTON_COORDS)) {
                // Link MIDI Device Button
                instrumentUtil.setMidiSource(instrumentData, InstrumentDataUtil.SYS_SOURCE_ID);
                this.syncInstrumentToServer();
                this.refreshMaestroName();
                this.allNotesOff();
            } else if(clickedBox(imouseX, imouseY, MAESTRO_SELF_BUTTON_COORDS)) {
                // Link Self Button
                instrumentUtil.setMidiSource(instrumentData, player.getUniqueID());
                this.syncInstrumentToServer();
                this.refreshMaestroName();
                this.allNotesOff();
            } else if(clickedBox(imouseX, imouseY, MAESTRO_PUBLIC_BUTTON_COORDS)) {
                // Link Public Button
                instrumentUtil.setMidiSource(instrumentData,  InstrumentDataUtil.PUBLIC_SOURCE_ID);
                this.syncInstrumentToServer();
                this.refreshMaestroName();
                this.allNotesOff();
            } else if(clickedBox(imouseX, imouseY, MAESTRO_CLEAR_BUTTON_COORDS)) {
                // Link Clear Button
                instrumentUtil.setMidiSource(instrumentData, null);
                this.syncInstrumentToServer();
                this.refreshMaestroName();
                this.allNotesOff();
            } else if(clickedBox(imouseX, imouseY, KEYBOARD_LAYOUT_BUTTON_COORDS)) {
                // Change Keyboard Layout
                if(ModConfigs.CLIENT.keyboardLayout.get().ordinal() < ClientConfig.KEYBOARD_LAYOUTS.values().length - 1) {
                    ModConfigs.CLIENT.keyboardLayout.set(ClientConfig.KEYBOARD_LAYOUTS.values()[ModConfigs.CLIENT.keyboardLayout.get().ordinal()+1]);
                } else {
                    ModConfigs.CLIENT.keyboardLayout.set(ClientConfig.KEYBOARD_LAYOUTS.values()[0]);
                }
            } else if(clickedBox(imouseX, imouseY, CLEAR_MIDI_BUTTON_COORDS)) {
                // Clear Midi Channels Button
                instrumentUtil.clearAcceptedChannels(instrumentData);
                this.syncInstrumentToServer();
                this.allNotesOff();
            } else if(clickedBox(imouseX, imouseY, ALL_MIDI_BUTTON_COORDS)) {
                // Select All Midi Channels Button
                instrumentUtil.setAcceptAllChannels(instrumentData);
                this.syncInstrumentToServer();
                this.allNotesOff();
            } else {
                // Individual Midi Channel Buttons
                for(int i = 0; i < 16; i++) {
                    Vector2f buttonCoords = new Vector2f(
                        GEN_MIDI_BUTTON_COORDS.x + i * 19,
                        GEN_MIDI_BUTTON_COORDS.y
                    );
    
                    if(clickedBox(imouseX, imouseY, buttonCoords)) {
                        instrumentUtil.toggleChannel(instrumentData, new Integer(i).byteValue());
                        this.syncInstrumentToServer();
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
        } else {
            List<Byte> midiNoteNums = getMidiNoteFromScanCode(scanCode, modifiers == 1, false);

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

        List<Byte> midiNoteNums = getMidiNoteFromScanCode(scanCode, modifiers == 1, true);

        if(midiNoteNums != null) {
            for(Byte midiNoteNum : midiNoteNums) {
                this.onGuiNoteRelease(midiNoteNum);
            }
        }

        return true;
    }

    public Byte getInstrumentId() {
        return this.instrumentUtil.getInstrumentIdFromData(this.instrumentData);
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
            result = new Integer(octaveNote + 12 * octaveNum).byteValue();
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
        
        // Set Min Note String
        Integer octaveNoteNum = visibleNoteShift % 7;
        Integer minOctave = visibleNoteShift / 7;
        this.minNoteString = noteLetterFromNum(octaveNoteNum) + minOctave.toString();
    }

    public void onMidiNoteOn(Byte channel, Byte midiNote, Byte velocity) {
        if(instrumentUtil.doesAcceptChannel(instrumentData, channel)) {
            this.onGuiNotePress(midiNote, velocity);
        }
    }
    
    public void onMidiNoteOff(Byte channel, Byte midiNote) {
        if(instrumentUtil.doesAcceptChannel(instrumentData, channel)) {
            this.onGuiNoteRelease(midiNote);
        }
    }

    public void onMidiAllNotesOff(Byte channel) {
        if(instrumentUtil.doesAcceptChannel(instrumentData, channel)) {
            this.allNotesOff();
        }
    }
    
    private void allNotesOff() {
        // Release all notes
        List<Byte> notesToRemove = new ArrayList<>(this.heldNotes.keySet());
        for(Byte note : notesToRemove) {
            this.releaseNote(note);
        }

        // Send all notes off packet
        MidiNoteOffPacket packet = new MidiNoteOffPacket(MidiNoteOffPacket.ALL_NOTES_OFF, instrumentId, player.getUniqueID());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }

    private void onGuiNotePress(Byte midiNote, Byte velocity) {
        // hold note
        this.holdNote(midiNote, velocity);

        // send packet
        MidiNoteOnPacket packet = new MidiNoteOnPacket(midiNote, velocity, instrumentId, player.getUniqueID(), player.getPosition());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
    }
    
    private void onGuiNoteRelease(Byte midiNote) {
        // release note
        this.releaseNote(midiNote);

        // send packet
        MidiNoteOffPacket packet = new MidiNoteOffPacket(midiNote, instrumentId, player.getUniqueID());
        NetworkManager.NET_CHANNEL.sendToServer(packet);
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
    private List<Byte> getMidiNoteFromScanCode(Integer scanCode, Boolean modifier, Boolean ignoreModifier) {
        switch(ModConfigs.CLIENT.keyboardLayout.get()) {
            case MIMI:
                return Arrays.asList(getMidiNoteFromScanCode_MIMI(scanCode))
                    .stream().filter(b -> b != null).collect(Collectors.toList());
            case VPiano:
                return Arrays.asList(getMidiNoteFromScanCode_VPiano(scanCode, modifier), ignoreModifier ? getMidiNoteFromScanCode_VPiano(scanCode, !modifier) : null)
                    .stream().filter(b -> b != null).collect(Collectors.toList());
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
        return midiInt != null ?  new Integer(12 * 2 + midiInt).byteValue() : null;
    }

    // Render Functions
    @Override
    protected MatrixStack renderGraphics(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        setAlpha(1.0f);

        // Set Texture
        Minecraft.getInstance().getTextureManager().bindTexture(guiTexture);

        // Visible Notes
        Integer keyboardTextureShift = (visibleNoteShift % (NOTE_WIDTH/2)) * NOTE_WIDTH;
        blit(matrixStack, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y - 1, this.getBlitOffset(), keyboardTextureShift, 274, 308, 128, TEXTURE_SIZE, TEXTURE_SIZE);

        // Note Labels
        if(ClientConfig.KEYBOARD_LAYOUTS.MIMI.equals(ModConfigs.CLIENT.keyboardLayout.get())) {
            blit(matrixStack, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y + 70, this.getBlitOffset(), 0, 456, 308, 54, TEXTURE_SIZE, TEXTURE_SIZE);
        } else {
            if(visibleNoteShift < V_PIANO_MIN_SHIFT) {
                Integer widthShift = (V_PIANO_MIN_SHIFT - visibleNoteShift) * NOTE_WIDTH;
                blit(matrixStack, START_X + NOTE_OFFSET_X - 1 + widthShift, START_Y + NOTE_OFFSET_Y + 70, this.getBlitOffset(), 0, 402, 308 - widthShift, 54, TEXTURE_SIZE, TEXTURE_SIZE);
            } else if(visibleNoteShift >= V_PIANO_MIN_SHIFT && visibleNoteShift <= V_PIANO_MAX_SHIFT) {
                blit(matrixStack, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y + 70, this.getBlitOffset(), (visibleNoteShift - V_PIANO_MIN_SHIFT) * NOTE_WIDTH , 402, 308, 54, TEXTURE_SIZE, TEXTURE_SIZE);
            } else if(visibleNoteShift <= V_PIANO_MAX_NOTE) {
                Integer widthShift = (V_PIANO_MAX_SHIFT - visibleNoteShift) * -NOTE_WIDTH;
                blit(matrixStack, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y + 70, this.getBlitOffset(), (visibleNoteShift - V_PIANO_MIN_SHIFT) * NOTE_WIDTH, 402, 308 - widthShift, 54, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }

        // Note Edges
        if(visibleNoteShift == 0) {
            blit(matrixStack, START_X + NOTE_OFFSET_X, START_Y + NOTE_OFFSET_Y, this.getBlitOffset(), 392, 274, 6, 86, TEXTURE_SIZE, TEXTURE_SIZE);
        } else if(visibleNoteShift == MAX_NOTE_SHIFT) {
            blit(matrixStack, START_X + 311, START_Y + NOTE_OFFSET_Y, this.getBlitOffset(), 392, 274, 6, 86, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        
        // Active Notes
        matrixStack = renderAndCleanNoteSet(matrixStack, this.heldNotes, 5000, true, entry -> {this.onGuiNoteRelease(entry.getKey());});
        matrixStack = renderAndCleanNoteSet(matrixStack, this.releasedNotes, 1000, false, entry -> {this.releasedNotes.remove(entry.getKey());});

        // Reset alpha for next layers
        setAlpha(1.0f);

        // GUI Background
        blit(matrixStack, START_X, START_Y, this.getBlitOffset(), 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Note Key Covers
        blit(matrixStack, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y + 55, this.getBlitOffset(), keyboardTextureShift, 247, 308, 26, TEXTURE_SIZE, TEXTURE_SIZE);
        
        // Channel Output Status Lights
        SortedArraySet<Byte> acceptedChannels = this.instrumentUtil.getAcceptedChannelsSet(this.instrumentData);

        if(acceptedChannels != null && !acceptedChannels.isEmpty()) {
            for(Byte channelId : acceptedChannels) {
                blit(matrixStack, START_X + 40 + 19 * channelId, START_Y + 97, this.getBlitOffset(), 369, 42, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }
        
        return matrixStack;
    }

    private MatrixStack renderAndCleanNoteSet(MatrixStack matrixStack, ConcurrentHashMap<Byte,Instant> noteMap, Integer sustainMillis, Boolean held, Consumer<Entry<Byte,Instant>> removeHandler) {
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

    private MatrixStack renderNote(MatrixStack matrixStack, Byte note, Boolean held, Instant releaseTime) {
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
            382 - (keyNum % 2) * 13, 
            0, 12, 41, 
            TEXTURE_SIZE, TEXTURE_SIZE
        );
        
        return matrixStack;
    }

    @Override
    protected MatrixStack renderText(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Instrument Name
        font.drawString(matrixStack, this.instrumentNameString, START_X + 214, START_Y + 17, 0xFF00E600);

        // Min Note Text
        font.drawString(matrixStack, this.minNoteString, START_X + 335, START_Y + 224, 0xFF00E600);

        // MIDI Source Name
        font.drawString(matrixStack, this.selectedMaestroName.length() <= 22 ? this.selectedMaestroName : this.selectedMaestroName.substring(0,21) + "...", START_X + 80, START_Y + 43, 0xFF00E600);
        
        // Keyboard Layout
        font.drawString(matrixStack, ModConfigs.CLIENT.keyboardLayout.get().toString(), START_X + 303, START_Y + 43, 0xFF00E600);

        return matrixStack;
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

    private void refreshMaestroName() {
        UUID linkedMaestro = instrumentUtil.getMidiSource(instrumentData);
        if(linkedMaestro != null) {
            if(linkedMaestro.equals(player.getUniqueID())) {
                this.selectedMaestroName = "My Transmitter";
            } else if(linkedMaestro.equals(InstrumentDataUtil.SYS_SOURCE_ID)) {
                this.selectedMaestroName = "MIDI Input Device";
            } else if(linkedMaestro.equals(InstrumentDataUtil.PUBLIC_SOURCE_ID)) {
                this.selectedMaestroName = "Public Transmitters";
            } else {
                this.selectedMaestroName = PlayerNameUtils.getPlayerNameFromUUID(linkedMaestro, world);
            }
        } else {
            this.selectedMaestroName = "None";
        }
    }

    // Data Utils
    public InstrumentDataUtil<T> getInstrumentDataUtil() {
        return this.instrumentUtil;
    }

    public T getInsturmentData() {
        return this.instrumentData;
    }
    
    public void syncInstrumentToServer() {
        InstrumentDataUpdatePacket packet = null;

        if(instrumentData instanceof ItemStack) {
            packet = ItemInstrument.getSyncPacket((ItemStack)instrumentData, handIn);
        } else if(instrumentData instanceof TileInstrument) {
            packet = TileInstrument.getSyncPacket((TileInstrument)instrumentData);
        }
        
        if(packet != null) {
            NetworkManager.NET_CHANNEL.sendToServer(packet);
        }
    }
}