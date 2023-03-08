package io.github.tofodroid.mods.mimi.common.config;

import java.util.Arrays;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraftforge.common.ForgeConfigSpec;

// 1. Default MIDI Input Device

public class ClientConfig {
    public static final String INSTRUMENT_GUI_CATEGORY_NAME = "Instrument GUI";
    public static final String MIDI_PLAYER_CATEGORY_NAME = "MIDI Player";
    public static final String MIDI_INPUT_CATEGORY_NAME = "MIDI Input";
    public static final String MIDI_SYNTH_CATEGORY_NAME = "MIDI Synth";

    // INSTRUMENT GUI
    public static enum KEYBOARD_LAYOUTS {
        MIMI,
        VPiano
    }
    public ForgeConfigSpec.EnumValue<KEYBOARD_LAYOUTS> keyboardLayout;

    // PLAYER
    public ForgeConfigSpec.ConfigValue<String> playlistFolderPath;

    // INPUT
    public ForgeConfigSpec.ConfigValue<String> selectedMidiDevice;
    public ForgeConfigSpec.BooleanValue enableMidiLogs;

    // SYNTH
    public ForgeConfigSpec.IntValue noteBufferMs;
    public ForgeConfigSpec.BooleanValue jitterCorrection;
    public ForgeConfigSpec.IntValue latency;
    public ForgeConfigSpec.IntValue localLatency;
    public ForgeConfigSpec.ConfigValue<Integer> synthSampleRate;
    public ForgeConfigSpec.ConfigValue<Integer> synthBitRate;
    public ForgeConfigSpec.ConfigValue<String> soundfontPath;
    public ForgeConfigSpec.BooleanValue raytraceSound;

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push(INSTRUMENT_GUI_CATEGORY_NAME);
        keyboardLayout = builder.comment("Instrument GUI keyboard layout for notes. MIMI uses its own layout by default but also supports the layout used by VirtualPiano.net.")
            .translation(MIMIMod.MODID + ".config.instrument.keyboard.layout")
            .defineEnum("instrumentKeyboardLayout", KEYBOARD_LAYOUTS.MIMI);
        builder.pop();
        builder.push(MIDI_PLAYER_CATEGORY_NAME);
        playlistFolderPath = builder.comment("Optional full path to a folder containing MIDI files to be used by the MIDI Player. See guide book for more information.")
            .translation(MIMIMod.MODID + ".config.midi.player.playlist.path")
            .define("playlistFolderPath", "");
        builder.pop();
        builder.push(MIDI_INPUT_CATEGORY_NAME);
        selectedMidiDevice = builder.comment("What MIDI Input Device should be used (if available)? This can be set from the in-game MIDI Input Device Configuration menu. Changes require a game restart to take affect.")
            .translation(MIMIMod.MODID + ".config.midi.input.defaultdevice")
            .define("defaultMidiInputDevice", "");
            enableMidiLogs = builder.comment("Should MIMI include detailed logs tracking MIDI note input and output?")
            .translation(MIMIMod.MODID + ".config.midi.synth.enablemidilogs")
            .define("enableMidiLogs", false);
        builder.pop();
        builder.push(MIDI_SYNTH_CATEGORY_NAME);
        raytraceSound = builder.comment("Should MIDI notes that are played take into account any blocks between you and the source and muffle the sound accordingly? Note that enabling this may impact performance on lower-end systems.")
            .translation(MIMIMod.MODID + ".config.midi.synth.raytracesound")
            .define("rayTraceSound", false);
        jitterCorrection = builder.comment("Should the built-in midi synthesizer enable Jitter Correction? When enabled note timing will be more accurate but latency will increase.")
            .translation(MIMIMod.MODID + ".config.midi.synth.jittercorrection")
            .define("synthJitterCorrection", true);
        latency = builder.comment("What baseline latency should the built-in midi synthesizer use (ms) for notes from other players? Smaller values will decrease latency but may cause stutter when playing notes. Very small values may cause notes to fail to play at all.")
            .translation(MIMIMod.MODID + ".config.midi.synth.latency")
            .defineInRange("synthBaselineLatency", 250, 10, 800);
        localLatency = builder.comment("What baseline latency should the built-in midi synthesizer use (ms) for notes played by you? Smaller values will decrease latency but may cause stutter when playing notes. Very small values may cause notes to fail to play at all.")
            .translation(MIMIMod.MODID + ".config.midi.synth.localLatency")
            .defineInRange("synthBaselineLocalLatency", 30, 10, 800);
        synthSampleRate = builder.comment("What sample rate should the built-in midi synthesizer use (hz)? Smaller values may decrease latency but will also decrease quality.","Allowed values: [8000,11025,16000,22050,44100,48000,96000]")
            .translation(MIMIMod.MODID + ".config.midi.synth.samplerate")
            .defineInList("synthSampleRate", 44100, Arrays.asList(8000,11025,16000,22050,44100,48000,96000));
        synthBitRate = builder.comment("What bitrate should the built-in midi synthesizer use (bits)? Smaller values may decrease latency but will also decrease quality.","Allowed values: [8,16,24,32]")
            .translation(MIMIMod.MODID + ".config.midi.synth.bitrate")
            .defineInList("synthBitRate", 16, Arrays.asList(8,16,24,32));
        soundfontPath = builder.comment("Optional full path to an SF2 format SoundFont to be used by the MIDI Synthesizer. See project page for more information.")
            .translation(MIMIMod.MODID + ".config.midi.synth.soundfont.path")
            .define("soundfontPath", "");
        noteBufferMs = builder.comment("How many milliseconds should notes from a server buffer before playing locally? Smaller values will decrease latency but may result in stuttering when the server is under heavy load.","Allowed values: 0-999")
            .translation(MIMIMod.MODID + ".config.midi.synth.bufferms")
            .defineInRange("synthBufferMillis",100, 0, 999);
        builder.pop();
    }
}
