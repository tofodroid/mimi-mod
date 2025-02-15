package io.github.tofodroid.mods.mimi.forge.common.config;

import java.util.Arrays;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ConfigProxy.KEYBOARD_LAYOUTS;

import net.minecraftforge.common.ForgeConfigSpec;

// 1. Default MIDI Input Device

public class ClientConfig {
    public static final String AUDIO_OUTPUT_CATEGORY_NAME = "Audio Output";
    public static final String INSTRUMENT_GUI_CATEGORY_NAME = "Instrument GUI";
    public static final String MIDI_PLAYER_CATEGORY_NAME = "MIDI Player";
    public static final String MIDI_INPUT_CATEGORY_NAME = "MIDI Input";
    public static final String MIDI_SYNTH_CATEGORY_NAME = "MIDI Synth";

    // INSTRUMENT GUI
    public ForgeConfigSpec.EnumValue<KEYBOARD_LAYOUTS> keyboardLayout;

    // MIDI INPUT
    public ForgeConfigSpec.IntValue midiDeviceVelocity;
    public ForgeConfigSpec.ConfigValue<String> transmitterMidiPath;

    // AUDIO
    public ForgeConfigSpec.BooleanValue automaticAudioDevice;
    public ForgeConfigSpec.ConfigValue<String> audioOutputDevice;
    public ForgeConfigSpec.IntValue audioDeviceVolume;

    // SYNTH
    public ForgeConfigSpec.IntValue localBufferms;
    public ForgeConfigSpec.BooleanValue jitterCorrection;
    public ForgeConfigSpec.IntValue latency;
    public ForgeConfigSpec.IntValue localLatency;
    public ForgeConfigSpec.ConfigValue<Integer> synthSampleRate;
    public ForgeConfigSpec.ConfigValue<Integer> synthBitRate;
    public ForgeConfigSpec.ConfigValue<String> soundfontPath;

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push(AUDIO_OUTPUT_CATEGORY_NAME);
        automaticAudioDevice = builder.comment("Whether or not MIMI should attempt to automatically determine the audio output device to use based on the Minecraft audio device. Works best on Windows.")
            .translation(MIMIMod.MODID + ".config.audio.automatic")
            .define("automaticAudioDevice", true);
        audioOutputDevice = builder.comment("When automatic device determination is set to false, the name of the audio output device that MIMI should attempt to use. If not found MIMI will use the System Default device.")
            .translation(MIMIMod.MODID + ".config.audio.device")
            .define("audioOutputDevice", "");
        audioDeviceVolume = builder.comment("A multipler used to increase or decrease the base volume of all notes played by MIMI instruments.","Allowed values: 0-10")
            .translation(MIMIMod.MODID + ".config.audio.volume")
            .defineInRange("audioDeviceVolume",5, 0, 10);
        builder.pop();
        builder.push(INSTRUMENT_GUI_CATEGORY_NAME);
        keyboardLayout = builder.comment("Instrument GUI keyboard layout for notes. MIMI uses its own layout by default but also supports the layout used by VirtualPiano.net.")
            .translation(MIMIMod.MODID + ".config.instrument.keyboard.layout")
            .defineEnum("instrumentKeyboardLayout", KEYBOARD_LAYOUTS.MIMI);
        builder.pop();
        builder.push(MIDI_INPUT_CATEGORY_NAME);
        midiDeviceVelocity = builder.comment("A value to add to the velocity of notes recieved from the MIDI input device (to increase or decrease volume).","Allowed values: - (-)127-(+)127")
            .translation(MIMIMod.MODID + ".config.midi.input.volume")
            .defineInRange("midiDeviceVelocity",0, -127, 127);
        transmitterMidiPath = builder.comment("Optional full path to the folder on your system that the Transmitter should MIDI songs from.")
            .translation(MIMIMod.MODID + ".config.midi.input.transmitter.path")
            .define("transmitterMidiPath", "");
        builder.pop();
        builder.push(MIDI_SYNTH_CATEGORY_NAME);
        jitterCorrection = builder.comment("Should the built-in midi synthesizer enable Jitter Correction? When enabled note timing will be more accurate but latency will increase.")
            .translation(MIMIMod.MODID + ".config.midi.synth.jittercorrection")
            .define("synthJitterCorrection", true);
        latency = builder.comment("What baseline latency should the built-in midi synthesizer use (ms) for notes from other players? Smaller values will decrease latency but may cause stutter when playing notes. Very small values may cause notes to fail to play at all.")
            .translation(MIMIMod.MODID + ".config.midi.synth.latency")
            .defineInRange("synthBaselineLatency", 50, 10, 500);
        localLatency = builder.comment("What baseline latency should the built-in midi synthesizer use (ms) for notes played by you? Smaller values will decrease latency but may cause stutter when playing notes. Very small values may cause notes to fail to play at all.")
            .translation(MIMIMod.MODID + ".config.midi.synth.localLatency")
            .defineInRange("synthBaselineLocalLatency", 30, 10, 500);
        synthSampleRate = builder.comment("What sample rate should the built-in midi synthesizer use (hz)? Smaller values may decrease latency but will also decrease quality.","Allowed values: [8000,11025,16000,22050,44100,48000,96000]")
            .translation(MIMIMod.MODID + ".config.midi.synth.samplerate")
            .defineInList("synthSampleRate", 22050, Arrays.asList(8000,11025,16000,22050,44100,48000,96000));
        synthBitRate = builder.comment("What bitrate should the built-in midi synthesizer use (bits)? Smaller values may decrease latency but will also decrease quality.","Allowed values: [8,16,24,32]")
            .translation(MIMIMod.MODID + ".config.midi.synth.bitrate")
            .defineInList("synthBitRate", 16, Arrays.asList(8,16,24,32));
        soundfontPath = builder.comment("Optional full path to an SF2 format SoundFont to be used by the MIDI Synthesizer. See project page for more information.")
            .translation(MIMIMod.MODID + ".config.midi.synth.soundfont.path")
            .define("soundfontPath", "");
        localBufferms = builder.comment("How long to have notes from the server buffer locally before playing. Higher values may decrease stuttering on high-latency connections but will cause redstone effects to be slightly off-tempo.","Allowed values: 0-100")
            .translation(MIMIMod.MODID + ".config.midi.synth.localBufferms")
            .defineInRange("localBufferms",10, 0, 100);
        builder.pop();
    }
}
