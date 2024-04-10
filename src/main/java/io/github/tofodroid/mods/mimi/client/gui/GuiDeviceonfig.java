package io.github.tofodroid.mods.mimi.client.gui;

import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.sampled.Mixer;

import io.github.tofodroid.mods.mimi.util.Vector2Int;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.client.midi.AudioOutputDeviceManager;
import io.github.tofodroid.mods.mimi.client.midi.MidiInputDeviceManager;
import io.github.tofodroid.mods.mimi.client.midi.synth.MidiMultiSynthManager;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class GuiDeviceonfig extends BaseGui {    
    // Button Boxes
    private static final Vector2Int AUDIO_DEVICE_BUTTON = new Vector2Int(7, 25);
    private static final Vector2Int MIDI_DEVICE_BUTTON = new Vector2Int(211, 25);

    private static final Vector2Int REFRESH_DEVICES_BUTTON = new Vector2Int(266,114);
    private static final Vector2Int SHIFT_DEVICE_DOWN_BUTTON = new Vector2Int(8,114);
    private static final Vector2Int SHIFT_DEVICE_UP_BUTTON = new Vector2Int(247,114);
    private static final Vector2Int SAVE_DEVICE_BUTTON = new Vector2Int(266,164);

    // Text Boxes
    private static final Vector2Int MIDI_DEVICE_NAME_BOX = new Vector2Int(51, 51);
    private static final Vector2Int MIDI_DEVICE_STATUS_BOX = new Vector2Int(51, 66);
    private static final Vector2Int AUDIO_DEVICE_NAME_BOX = new Vector2Int(51, 46);
    private static final Vector2Int AUDIO_DEVICE_STATUS_BOX = new Vector2Int(51, 69);

    // Data
    private Boolean audioMode = true;
    private Integer visibleDeviceId = -1;

    // Data - MIDI
    private MidiInputDeviceManager midiDeviceManager;
    private List<MidiDevice> availableMidiDevices;

    // Data - Audio
    private MidiMultiSynthManager synthManager;
    private List<Mixer> availableAudioDevices;
    private List<String> availableAudioDeviceDisplayNames;

    public GuiDeviceonfig(Player player) {
        super(288, 187, 288, "textures/gui/gui_midi_config.png",  "item.MIMIMod.gui_midi_input_config");
        
        // MIDI
        this.midiDeviceManager = ((ClientProxy)MIMIMod.getProxy()).getMidiData().inputDeviceManager;
        availableMidiDevices = this.midiDeviceManager.getAvailableDevices();

        // Audio
        this.synthManager = ((ClientProxy)MIMIMod.getProxy()).getMidiSynth();
        this.availableAudioDevices = this.synthManager.audioDeviceManager.getAvailableOutputDevices();
        this.availableAudioDeviceDisplayNames = AudioOutputDeviceManager.getDeviceDisplayNames(availableAudioDevices);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(AUDIO_DEVICE_BUTTON), new Vector2Int(87, 14))) {
            audioMode = true;
            visibleDeviceId = 0;
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(MIDI_DEVICE_BUTTON), new Vector2Int(70, 14))) {
            audioMode = false;
            visibleDeviceId = 0;
        } else if(audioMode) {
            if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(REFRESH_DEVICES_BUTTON))) {
                visibleDeviceId = -1;
                availableAudioDevices = this.synthManager.audioDeviceManager.getAvailableOutputDevices();
                availableAudioDeviceDisplayNames = AudioOutputDeviceManager.getDeviceDisplayNames(availableAudioDevices);
            } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SAVE_DEVICE_BUTTON))) {
                if(visibleDeviceId == -1) {
                    this.synthManager.audioDeviceManager.setAutomaticDevice();
                    this.synthManager.reloadSynths();
                } else {
                    this.synthManager.audioDeviceManager.setDevice(this.availableAudioDeviceDisplayNames.get(visibleDeviceId));
                    this.synthManager.reloadSynths();
                }
            } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHIFT_DEVICE_UP_BUTTON))) {
                visibleDeviceId = visibleDeviceId < (this.availableAudioDevices.size() - 1) ? visibleDeviceId + 1 : visibleDeviceId;
            } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHIFT_DEVICE_DOWN_BUTTON))) {
                visibleDeviceId = visibleDeviceId > -1 ? visibleDeviceId - 1 : visibleDeviceId;
            }
        } else if(!audioMode) {
            if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(REFRESH_DEVICES_BUTTON))) {
                visibleDeviceId = 0;
                availableMidiDevices = this.midiDeviceManager.getAvailableDevices();
            } else if(!this.midiDeviceManager.isDeviceSelected() && this.availableMidiDevices != null && this.availableMidiDevices.size() > visibleDeviceId && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SAVE_DEVICE_BUTTON))) {
                this.midiDeviceManager.saveDeviceSelection(availableMidiDevices.get(visibleDeviceId));
            } else if(this.availableMidiDevices != null && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHIFT_DEVICE_UP_BUTTON))) {
                visibleDeviceId = visibleDeviceId < (this.availableMidiDevices.size() - 1) ? visibleDeviceId + 1 : visibleDeviceId;
            } else if(this.availableMidiDevices != null  && CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SHIFT_DEVICE_DOWN_BUTTON))) {
                visibleDeviceId = visibleDeviceId > 0 ? visibleDeviceId - 1 : visibleDeviceId;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Background
        this.blitAbsolute(graphics, guiTexture, START_X, START_Y, 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);

        // Audio Overlay
        if(this.audioMode) {
            this.blitAbsolute(graphics, guiTexture, START_X + 4, START_Y + 22, 4, 218, 280, 69, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        
        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if(this.audioMode) {
            // Selected Device Name / Status
            List<String> nameLines = CommonGuiUtils.wrapString(font, this.synthManager.audioDeviceManager.getCurrentDeviceName(), 228, 2);
            if(!nameLines.isEmpty()) {
                this.drawStringAbsolute(graphics, font, nameLines.get(0), START_X + AUDIO_DEVICE_NAME_BOX.x(), START_Y + AUDIO_DEVICE_NAME_BOX.y(), 0xFF00E600);

                if(nameLines.size() > 1) {
                    this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, nameLines.get(1), 228), START_X + AUDIO_DEVICE_NAME_BOX.x(), START_Y + AUDIO_DEVICE_NAME_BOX.y() + 10, 0xFF00E600);
                }
            }
            
            List<String> statusLines = CommonGuiUtils.wrapString(font, this.synthManager.audioDeviceManager.getCurrentDeviceStatus(), 228, 2);
            if(!statusLines.isEmpty()) {
                this.drawStringAbsolute(graphics, font, statusLines.get(0), START_X + AUDIO_DEVICE_STATUS_BOX.x(), START_Y + AUDIO_DEVICE_STATUS_BOX.y(), 0xFF00E600);

                if(statusLines.size() > 1) {
                    this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, statusLines.get(1), 228), START_X + AUDIO_DEVICE_STATUS_BOX.x(), START_Y + AUDIO_DEVICE_STATUS_BOX.y() + 10, 0xFF00E600);
                }
            }

            // Available Audio Device Info
            if(this.visibleDeviceId == -1) {
                this.drawStringAbsolute(graphics, font, "Automatic", START_X + 29, START_Y + 118, 0xFF00E600);
                this.drawStringAbsolute(graphics, font, "MIMI will attempt to connect to the same audio", START_X + 10, START_Y + 134, 0xFF00E600);
                this.drawStringAbsolute(graphics, font, "device that Minecraft is connected to. This", START_X + 10, START_Y + 144, 0xFF00E600);
                this.drawStringAbsolute(graphics, font, "usually works, but may not work if you are using", START_X + 10, START_Y + 154, 0xFF00E600);
                this.drawStringAbsolute(graphics, font, "a non-Windows operating system (Mac or Linux).", START_X + 10, START_Y + 164, 0xFF00E600);
            } else if(this.availableAudioDevices != null && this.availableAudioDevices.size() > visibleDeviceId) {
                this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, visibleDeviceId + ": " + this.availableAudioDeviceDisplayNames.get(visibleDeviceId), 212), START_X + 29, START_Y + 118, 0xFF00E600);
                Mixer.Info info = this.availableAudioDevices.get(visibleDeviceId).getMixerInfo();

                if(info != null) {
                    List<String> descLines = CommonGuiUtils.wrapString(font, "Description: " + info.getDescription(), 252, 2);
                    if(!descLines.isEmpty()) {
                        this.drawStringAbsolute(graphics, font, descLines.get(0), START_X + 10, START_Y + 134, 0xFF00E600);

                        if(descLines.size() > 1) {
                            this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, descLines.get(1), 228), START_X + 10, START_Y + 144, 0xFF00E600);
                        }
                    }
                    this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, "Vendor: " + info.getVendor(), 252), START_X + 10, START_Y + 158, 0xFF00E600);
                    this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, "Version: " + info.getVersion(), 252), START_X + 10, START_Y + 170, 0xFF00E600);  
                } else if(this.availableAudioDevices == null || this.availableAudioDevices.isEmpty()) {
                    this.drawStringAbsolute(graphics, font, "No devices detected on system.", START_X + 10, START_Y + 134, 0xFF00E600);
                }
            }
        } else {
            // Selected Device Name / Status
            if(this.midiDeviceManager.isDeviceSelected()) {
                this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, this.midiDeviceManager.getSelectedDeviceName(), 228), START_X + MIDI_DEVICE_NAME_BOX.x(), START_Y + MIDI_DEVICE_NAME_BOX.y(), 0xFF00E600);

                if(this.midiDeviceManager.isDeviceAvailable()) {
                    this.drawStringAbsolute(graphics, font, "Connected", START_X + MIDI_DEVICE_STATUS_BOX.x(), START_Y + MIDI_DEVICE_STATUS_BOX.y(), 0xFF00E600);
                } else if(this.midiDeviceManager.isDeviceError()) {
                    this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, "Error: " + this.midiDeviceManager.getSelectedDeviceError(), 252), START_X + MIDI_DEVICE_STATUS_BOX.x(), START_Y + MIDI_DEVICE_STATUS_BOX.y(), 0xFF00E600);
                } else {
                    this.drawStringAbsolute(graphics, font, "Error: Unavailable", START_X + MIDI_DEVICE_STATUS_BOX.x(), START_Y + MIDI_DEVICE_STATUS_BOX.y(), 0xFF00E600);
                }
            }

            // Available MIDI Device Info
            if(this.availableMidiDevices != null && this.availableMidiDevices.size() > visibleDeviceId) {
                this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, visibleDeviceId + ": " + this.availableMidiDevices.get(visibleDeviceId).getDeviceInfo().getName(), 212), START_X + 29, START_Y + 118, 0xFF00E600);
                Info info = this.availableMidiDevices.get(visibleDeviceId).getDeviceInfo();

                if(info != null) {
                    List<String> descLines = CommonGuiUtils.wrapString(font, "Description: " + info.getDescription(), 252, 2);
                    if(!descLines.isEmpty()) {
                        this.drawStringAbsolute(graphics, font, descLines.get(0), START_X + 10, START_Y + 134, 0xFF00E600);

                        if(descLines.size() > 1) {
                            this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, descLines.get(1), 228), START_X + 10, START_Y + 144, 0xFF00E600);
                        }
                    }
                    this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, "Vendor: " + info.getVendor(), 252), START_X + 10, START_Y + 158, 0xFF00E600);
                    this.drawStringAbsolute(graphics, font, CommonGuiUtils.truncateString(font, "Version: " + info.getVersion(), 252), START_X + 10, START_Y + 170, 0xFF00E600);  
                }
            } else if(this.availableMidiDevices == null || this.availableMidiDevices.isEmpty()) {
                this.drawStringAbsolute(graphics, font, "No devices detected on system.", START_X + 10, START_Y + 134, 0xFF00E600);
            }
        }

        return graphics;
    }
}