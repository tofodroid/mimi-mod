package io.github.tofodroid.mods.mimi.common.config;

import java.util.Arrays;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraftforge.common.ForgeConfigSpec;

// 1. Default MIDI Input Device


public class ClientConfig {
    public static final String MIDI_PLAYER_CATEGORY_NAME = "MIDI Player";
    public static final String MIDI_INPUT_CATEGORY_NAME = "MIDI Input";
    public static final String MIDI_SYNTH_CATEGORY_NAME = "MIDI Synth";

    // PLAYER
    public ForgeConfigSpec.ConfigValue<String> playlistFolderPath;
    // INPUT
    public ForgeConfigSpec.IntValue defaultMidiInputDevice;
    // SYNTH
    public ForgeConfigSpec.BooleanValue jitterCorrection;
    public ForgeConfigSpec.IntValue latency;
    public ForgeConfigSpec.ConfigValue<Integer> synthSampleRate;
    public ForgeConfigSpec.ConfigValue<Integer> synthBitRate;
    public ForgeConfigSpec.ConfigValue<String> soundfontPath;
    public ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push(MIDI_PLAYER_CATEGORY_NAME);
        playlistFolderPath = builder.comment("Optional full path to a folder containing MIDI files to be used by the MIDI Player. See project page for more information.")
            .translation(MIMIMod.MODID + ".config.midi.player.playlist.path")
            .define("playlistFolderPath", "");
        builder.pop();
        builder.push(MIDI_INPUT_CATEGORY_NAME);
        defaultMidiInputDevice = builder.comment("What MIDI Input Device ID should be used by default (if available)? Set to -1 to have no default selection.")
            .translation(MIMIMod.MODID + ".config.midi.input.defaultdevice")
            .defineInRange("defaultMidiInputDevice", -1, -1, 99);
        builder.pop();
        builder.push(MIDI_SYNTH_CATEGORY_NAME);
        jitterCorrection = builder.comment("Should the built-in midi synthesizer enable Jitter Correction? When enabled note timing will be more accurate but latency will increase.")
            .translation(MIMIMod.MODID + ".config.midi.synth.jittercorrection")
            .define("synthJitterCorrection", true);
        latency = builder.comment("What baseline latency should the built-in midi synthesizer use (ms)? Smaller values will decrease latency but may cause stutter when playing notes. Very small values may cause notes to fail to play at all.")
            .translation(MIMIMod.MODID + ".config.midi.synth.latency")
            .defineInRange("synthBaselineLatency", 250, 10, 800);
        synthSampleRate = builder.comment("What sample rate should the built-in midi synthesizer use (hz)? Smaller values may decrease latency but will also decrease quality.","Allowed values: [8000,11025,16000,22050,44100,48000,96000]")
            .translation(MIMIMod.MODID + ".config.midi.synth.samplerate")
            .defineInList("synthSampleRate", 44100, Arrays.asList(8000,11025,16000,22050,44100,48000,96000));
        synthBitRate = builder.comment("What bitrate should the built-in midi synthesizer use (bits)? Smaller values may decrease latency but will also decrease quality.","Allowed values: [8,16,24,32]")
            .translation(MIMIMod.MODID + ".config.midi.synth.samplerate")
            .defineInList("synthBitRate", 16, Arrays.asList(8,16,24,32));
        soundfontPath = builder.comment("Optional full path to an SF2 or DLS format SoundFont to be used by the MIDI Synthesizer. See project page for more information.")
            .translation(MIMIMod.MODID + ".config.midi.synth.soundfont.path")
            .define("soundfontPath", "");
        builder.pop();
    }
}
