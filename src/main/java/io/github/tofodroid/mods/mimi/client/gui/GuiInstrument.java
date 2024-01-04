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
import com.mojang.math.Axis;

import org.joml.Vector2i;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.client.gui.widget.MidiChannelToggleWidget;
import io.github.tofodroid.mods.mimi.client.gui.widget.TransmitterSourceWidget;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.SyncInstrumentPacket;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import io.github.tofodroid.mods.mimi.common.config.ClientConfig;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.lwjgl.glfw.GLFW;

public class GuiInstrument extends BaseGui {
    // Texture
    private static final Integer NOTE_WIDTH = 14;
    private static final Integer NOTE_OFFSET_X = 11;
    private static final Integer NOTE_OFFSET_Y = 29;

    // GUI
    private static final Vector2i MIDI_CHANNEL_WIDGET_COORDS = new Vector2i(137,71);
    private static final Vector2i TRANSMIT_SOURCE_WIDGET_COORDS = new Vector2i(18,105);
    private static final Vector2i SYS_DEVICE_BUTTON_COORDS = new Vector2i(108,85);
    private static final Vector2i INSTRUMENT_VOLUME_UP_BUTTON_COORDS = new Vector2i(114,62);
    private static final Vector2i INSTRUMENT_VOLUME_DOWN_BUTTON_COORDS = new Vector2i(67,62);
    private static final Vector2i KEYBOARD_LAYOUT_BUTTON_COORDS = new Vector2i(300,31);
    private static final Vector2i NOTE_SHIFT_DOWN_BUTTON_COORDS = new Vector2i(33,161);
    private static final Vector2i NOTE_SHIFT_UP_BUTTON_COORDS = new Vector2i(52,161);
    private static final Vector2i OCT_SHIFT_DOWN_BUTTON_COORDS = new Vector2i(14,161);
    private static final Vector2i OCT_SHIFT_UP_BUTTON_COORDS = new Vector2i(71,161);
    private static final Vector2i MIDI_EDIT_BUTTON_COORDS = new Vector2i(299,161);

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

    // Widgets
    private MidiChannelToggleWidget midiChannelToggle;
    private TransmitterSourceWidget transmitSource;

    // Input Data
    private final ItemStack instrumentStack;
    private Player player;
    private InteractionHand handIn;

    // Runtime Data
    private ConcurrentHashMap<Byte, Instant> heldNotes;
    private ConcurrentHashMap<Byte, Instant> releasedNotes;
    private String instrumentNameString = null;
    private Byte instrumentId = null;
    private Boolean editMode = false;
    private Integer visibleNoteShift = KEYBOARD_START_NOTE;
    private String noteIdString = "C3,F4 | G4,C6";
    private Byte mouseNote = null;

    public GuiInstrument(Player player, ItemStack instrumentStack, InteractionHand handIn) {
        super(328, 184, 530, "textures/gui/container_instrument.png", "item.MIMIMod.gui_instrument");

        if(instrumentStack == null || instrumentStack.isEmpty()) {
            MIMIMod.LOGGER.error("Instrument stack is null or empty. Force closing GUI!");
            Minecraft.getInstance().forceSetScreen((Screen)null);
            this.instrumentStack = null;
            return;
        }

        this.player = player;
        this.handIn = handIn;
        this.instrumentStack = new ItemStack(instrumentStack.getItem(), instrumentStack.getCount());
        this.instrumentStack.setTag(instrumentStack.getOrCreateTag().copy());
        this.instrumentId = InstrumentDataUtils.getInstrumentId(this.instrumentStack);
        this.instrumentNameString = InstrumentDataUtils.getInstrumentName(this.instrumentId);
    }

    @Override
    public void init() {
        super.init();
        this.heldNotes = new ConcurrentHashMap<>();
        this.releasedNotes = new ConcurrentHashMap<>();
        this.midiChannelToggle = new MidiChannelToggleWidget(instrumentStack, new Vector2i(START_X, START_Y), MIDI_CHANNEL_WIDGET_COORDS);
        this.transmitSource = new TransmitterSourceWidget(instrumentStack, player.getUUID(), player.getName().getString(), new Vector2i(START_X, START_Y), TRANSMIT_SOURCE_WIDGET_COORDS);
    }

    public void syncInstrumentToServer() {
        NetworkManager.INFO_CHANNEL.sendToServer(new SyncInstrumentPacket(instrumentStack, this.handIn));
    }

    @Override
    public void onClose() {
        this.toggleHoldPedal(false);
        this.releaseHeldNotes();
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double dmouseX, double dmouseY, int mouseButton) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);

        // Keyboard Controls
        if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(NOTE_SHIFT_UP_BUTTON_COORDS))) {
            this.shiftVisibleNotes(true, 1);
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(NOTE_SHIFT_DOWN_BUTTON_COORDS))) {
            this.shiftVisibleNotes(false, 1);
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(OCT_SHIFT_UP_BUTTON_COORDS))) {
            this.shiftVisibleNotes(true, 7);
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(OCT_SHIFT_DOWN_BUTTON_COORDS))) {
            this.shiftVisibleNotes(false, 7);
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(MIDI_EDIT_BUTTON_COORDS))) {
            editMode = !editMode;
        }
        
        if(!editMode) {
            // Keyboard Layout Hover Box
            if(imouseX >= (START_X + 222) && imouseY >= (START_Y + 28) && imouseX < (START_X + this.GUI_WIDTH) && imouseY < (START_Y + 50)) {
                if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(KEYBOARD_LAYOUT_BUTTON_COORDS))) {
                    if(ModConfigs.CLIENT.keyboardLayout.get().ordinal() < ClientConfig.KEYBOARD_LAYOUTS.values().length - 1) {
                        ModConfigs.CLIENT.keyboardLayout.set(ClientConfig.KEYBOARD_LAYOUTS.values()[ModConfigs.CLIENT.keyboardLayout.get().ordinal()+1]);
                    } else {
                        ModConfigs.CLIENT.keyboardLayout.set(ClientConfig.KEYBOARD_LAYOUTS.values()[0]);
                    }
                    this.releaseHeldNotes();
                }

                return super.mouseClicked(dmouseX, dmouseY, mouseButton);
            }
            
            // Note Keys
            int relativeMouseX = imouseX - (START_X + NOTE_OFFSET_X);
            int relativeMouseY = imouseY - (START_Y + NOTE_OFFSET_Y);

            if(relativeMouseX >= 0 && relativeMouseY >= 0 && imouseX < (START_X + this.GUI_WIDTH - 11) && imouseY < (START_Y + this.GUI_HEIGHT - 28)) {
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
        } else {
            // MIDI Controls
            if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SYS_DEVICE_BUTTON_COORDS))) {
                // Toggle Sys Device Button
                InstrumentDataUtils.setSysInput(instrumentStack, !InstrumentDataUtils.getSysInput(instrumentStack));
                this.syncInstrumentToServer();
                this.allNotesOff();
            } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(INSTRUMENT_VOLUME_UP_BUTTON_COORDS))) {
                InstrumentDataUtils.setInstrumentVolume(instrumentStack, Integer.valueOf(InstrumentDataUtils.getInstrumentVolume(instrumentStack) + 1).byteValue());
                this.syncInstrumentToServer();
            } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(INSTRUMENT_VOLUME_DOWN_BUTTON_COORDS))) {
                InstrumentDataUtils.setInstrumentVolume(instrumentStack, Integer.valueOf(InstrumentDataUtils.getInstrumentVolume(instrumentStack) - 1).byteValue());
                this.syncInstrumentToServer();
            } else if(transmitSource.mouseClicked(imouseX, imouseY, mouseButton)) {
                this.syncInstrumentToServer();
            } else if(midiChannelToggle.mouseClicked(imouseX, imouseY, mouseButton)) {
                this.syncInstrumentToServer();
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

    private void toggleHoldPedal(Boolean on) {
        Byte controller = 64;
        Byte value = on ? Byte.MAX_VALUE : 0;
        MidiNotePacket packet = MidiNotePacket.createControlPacket(controller, value, instrumentId, player.getUUID(), player.getOnPos());
        NetworkManager.NOTE_CHANNEL.sendToServer(packet);
        ((ClientProxy)MIMIMod.proxy).getMidiSynth().handleLocalPacketInstant(packet);
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

    private void releaseHeldNotes() {
        // Release all notes
        if(this.heldNotes != null && this.instrumentId != null) {
            List<Byte> notesToRemove = new ArrayList<>(this.heldNotes.keySet());
            for(Byte note : notesToRemove) {
                this.onGuiNoteRelease(note);
            }

            MidiNotePacket packet = MidiNotePacket.createAllNotesOffPacket(instrumentId, player.getUUID(), player.getOnPos());
            NetworkManager.NOTE_CHANNEL.sendToServer(packet);
            // Turn off matching notes from BOTH synths because it could affect local notes and transmitter notes
            ((ClientProxy)MIMIMod.proxy).getMidiSynth().handlePacket(packet);
            ((ClientProxy)MIMIMod.proxy).getMidiSynth().handleLocalPacketInstant(packet);
        }
    }

    private void allNotesOff() {
        // Release all notes
        if(this.heldNotes != null && this.instrumentId != null) {
            this.releaseHeldNotes();

            MidiNotePacket packet = MidiNotePacket.createAllNotesOffPacket(instrumentId, player.getUUID(), player.getOnPos());
            NetworkManager.NOTE_CHANNEL.sendToServer(packet);
            // Turn off matching notes from BOTH synths because it could affect local notes and transmitter notes
            ((ClientProxy)MIMIMod.proxy).getMidiSynth().handlePacket(packet);
            ((ClientProxy)MIMIMod.proxy).getMidiSynth().handleLocalPacketInstant(packet);
        }
    }

    private void onGuiNotePress(Byte midiNote, Byte velocity) {
        if(this.instrumentId != null) {
            MidiNotePacket packet = MidiNotePacket.createNotePacket(midiNote, InstrumentDataUtils.applyVolume(instrumentStack, velocity), instrumentId, player.getUUID(), player.getOnPos());
            NetworkManager.NOTE_CHANNEL.sendToServer(packet);
            ((ClientProxy)MIMIMod.proxy).getMidiSynth().handleLocalPacketInstant(packet);
            this.releasedNotes.remove(midiNote);
            this.heldNotes.put(midiNote, Instant.now());
        }
    }

    private void onGuiNoteRelease(Byte midiNote) {
        if(this.instrumentId != null) {
            MidiNotePacket packet = MidiNotePacket.createNotePacket(midiNote, Integer.valueOf(0).byteValue(), instrumentId, player.getUUID(), player.getOnPos());
            NetworkManager.NOTE_CHANNEL.sendToServer(packet);
            ((ClientProxy)MIMIMod.proxy).getMidiSynth().handleLocalPacketInstant(packet);

            if(this.heldNotes.remove(midiNote) != null) {
                this.releasedNotes.put(midiNote, Instant.now());
            }
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
                MIMIMod.LOGGER.warn("Warning: Unknown keyboard layout selected for Instrument GUI.");
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
    protected GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        setAlpha(1.0f);

        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // Visible Notes
        Integer keyboardTextureShift = (visibleNoteShift % (NOTE_WIDTH/2)) * NOTE_WIDTH;
        graphics.blit(guiTexture, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y - 1, keyboardTextureShift, 276, 308, 128, TEXTURE_SIZE, TEXTURE_SIZE);

        // Note Labels
        if(ClientConfig.KEYBOARD_LAYOUTS.MIMI.equals(ModConfigs.CLIENT.keyboardLayout.get())) {
            graphics.blit(guiTexture, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y + 70, 0, 457, 308, 53, TEXTURE_SIZE, TEXTURE_SIZE);
        } else {
            if(visibleNoteShift < V_PIANO_MIN_SHIFT) {
                Integer widthShift = (V_PIANO_MIN_SHIFT - visibleNoteShift) * NOTE_WIDTH;
                graphics.blit(guiTexture, START_X + NOTE_OFFSET_X - 1 + widthShift, START_Y + NOTE_OFFSET_Y + 70, 0, 404, 308 - widthShift, 53, TEXTURE_SIZE, TEXTURE_SIZE);
            } else if(visibleNoteShift >= V_PIANO_MIN_SHIFT && visibleNoteShift <= V_PIANO_MAX_SHIFT) {
                graphics.blit(guiTexture, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y + 70, (visibleNoteShift - V_PIANO_MIN_SHIFT) * NOTE_WIDTH, 404, 308, 53, TEXTURE_SIZE, TEXTURE_SIZE);
            } else if(visibleNoteShift <= V_PIANO_MAX_NOTE) {
                Integer widthShift = (V_PIANO_MAX_SHIFT - visibleNoteShift) * -NOTE_WIDTH;
                graphics.blit(guiTexture, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y + 70, (visibleNoteShift - V_PIANO_MIN_SHIFT) * NOTE_WIDTH, 404, 308 - widthShift, 53, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }

        // Note Edges
        if(visibleNoteShift == 0) {
            graphics.blit(guiTexture, START_X + NOTE_OFFSET_X, START_Y + NOTE_OFFSET_Y, 392, 276, 6, 86, TEXTURE_SIZE, TEXTURE_SIZE);
        } else if(visibleNoteShift == MAX_NOTE_SHIFT) {
            graphics.blit(guiTexture, START_X + 311, START_Y + NOTE_OFFSET_Y, 392, 276, 6, 86, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        
        // Active Notes
        graphics = renderAndCleanNoteSet(graphics, this.heldNotes, 5000, true, entry -> {this.onGuiNoteRelease(entry.getKey());});
        graphics = renderAndCleanNoteSet(graphics, this.releasedNotes, 1000, false, entry -> {this.releasedNotes.remove(entry.getKey());});

        // Reset alpha for next layers
        setAlpha(1.0f);

        // GUI Background
        graphics.blit(guiTexture, START_X, START_Y, 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Note Key Covers
        graphics.blit(guiTexture, START_X + NOTE_OFFSET_X - 1, START_Y + NOTE_OFFSET_Y + 55, keyboardTextureShift, 250, 308, 26, TEXTURE_SIZE, TEXTURE_SIZE);
        
        // Switchboard Edit Panel
        if(editMode) {
            // Switchboard Background Panel
            graphics.pose().pushPose();
            graphics.pose().mulPose(Axis.ZN.rotationDegrees(90.0F));
            graphics.blit(guiTexture, -(START_Y + 29 + 126),  START_X + 11, 404, 0, 126, 306, TEXTURE_SIZE, TEXTURE_SIZE);
            graphics.pose().popPose();

            // Sys MIDI Device Status Light
            if(InstrumentDataUtils.getSysInput(this.instrumentStack)) {
                graphics.blit(guiTexture, START_X + 127, START_Y + 91, 329, 42, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
            }

            this.midiChannelToggle.renderGraphics(graphics, mouseX, mouseY);
            this.transmitSource.renderGraphics(graphics, mouseX, mouseY);
        }
        
        return graphics;
    }

    private GuiGraphics renderAndCleanNoteSet(GuiGraphics graphics, ConcurrentHashMap<Byte,Instant> noteMap, Integer sustainMillis, Boolean held, Consumer<Entry<Byte,Instant>> removeHandler) {
        List<Entry<Byte,Instant>> notesToRemove = new ArrayList<>();
        if(!noteMap.isEmpty()) {
            for(Entry<Byte,Instant> entry : noteMap.entrySet()) {
                if(Math.abs(ChronoUnit.MILLIS.between(Instant.now(), entry.getValue())) > sustainMillis) {
                    notesToRemove.add(entry);
                }
                graphics = this.renderNote(graphics, entry.getKey(), held, entry.getValue());
            }

            notesToRemove.forEach(entry -> removeHandler.accept(entry));
        }

        return graphics;
    }

    private GuiGraphics renderNote(GuiGraphics graphics, Byte note, Boolean held, Instant releaseTime) {
        Float alpha = 1.0f;
        Integer keyNum = midiNoteToKeyNum(note);

        // If we can't find a visible key for the note then skip rendering
        if(keyNum == null) {
            return graphics;
        }

        if(!held) {
            alpha -= Math.min(Math.abs(ChronoUnit.MILLIS.between(Instant.now(), releaseTime))/1000f, 1.0f);
        }
        
        setAlpha(alpha);

        graphics.blit(
            guiTexture, 
            START_X + NOTE_OFFSET_X + (keyNum - 1) * NOTE_WIDTH/2, 
            START_Y + NOTE_OFFSET_Y + 43 + (keyNum % 2) * 42, 
            
            342 - (keyNum % 2) * 13, 
            0, 12, 41, 
            TEXTURE_SIZE, TEXTURE_SIZE
        );
        
        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Instrument Name
        graphics.drawString(font, this.instrumentNameString, START_X + 198, START_Y + 13, 0xFF00E600);

        // Note Text: Left
        graphics.drawString(font, this.noteIdString.split(",")[0], START_X + 102, START_Y + 165, 0xFF00E600);

        // Note Text: Middle
        graphics.drawString(font, this.noteIdString.split(",")[1], START_X + 143, START_Y + 165, 0xFF00E600);

        // Note Text: Right
        graphics.drawString(font, this.noteIdString.split(",")[2], START_X + 198, START_Y + 165, 0xFF00E600);

        // MIDI Source Name & Volume
        if(editMode) {
            this.midiChannelToggle.renderText(graphics, font, mouseX, mouseY);
            this.transmitSource.renderText(graphics, font, mouseX, mouseY);
            graphics.drawString(font, InstrumentDataUtils.getInstrumentVolume(this.instrumentStack).toString(), START_X + 88, START_Y + 66, 0xFF00E600);
        }

        // Keyboard Layout
        if(editMode) {
            graphics.drawString(font, ModConfigs.CLIENT.keyboardLayout.get().toString(), START_X + 264, START_Y + 35, 0xFF003600);
        } else { 
            graphics.drawString(font, ModConfigs.CLIENT.keyboardLayout.get().toString(), START_X + 264, START_Y + 35, 0xFF00E600);
        }

        return graphics;
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
}
