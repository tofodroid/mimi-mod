package io.github.tofodroid.mods.mimi.common.config;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import io.github.tofodroid.mods.mimi.neoforge.common.config.ModConfigs;

public class ConfigProxy {
    // INSTRUMENT GUI
    public static enum KEYBOARD_LAYOUTS {
        MIMI,
        VPiano
    }

    public static Path getConfigPath() {
        return ModConfigs.getConfigPath();   
    }

    public static void registerConfigs() {
        ModConfigs.registerConfigs();
    }

    // Read-Write
    public static KEYBOARD_LAYOUTS getKeyboardLayout() {
        return ModConfigs.CLIENT.keyboardLayout.get();
    };

    public static void setKeyboardLayout(KEYBOARD_LAYOUTS layouts) {
        ModConfigs.CLIENT.keyboardLayout.set(layouts);
    }

    public static Integer getMidiDeviceVelocity() {
        return ModConfigs.CLIENT.midiDeviceVelocity.get();
    }

    public static void setMidiDeviceVelocity(Integer velocity) {
        ModConfigs.CLIENT.midiDeviceVelocity.set(velocity);
    }

    public static Boolean getAutomaticAudioDevice() {
        return ModConfigs.CLIENT.automaticAudioDevice.get();
    }

    public static void setAutomaticAudioDevice(Boolean auto) {
        ModConfigs.CLIENT.automaticAudioDevice.set(auto);
    }

    public static String getAudioOutputDevice() {
        return ModConfigs.CLIENT.audioOutputDevice.get();
    }

    public static void setAudioOutputDevice(String device) {
        ModConfigs.CLIENT.audioOutputDevice.set(device);
    }

    public static Integer getAudioDeviceVolume() {
        return ModConfigs.CLIENT.audioDeviceVolume.get();
    }

    public static void setAudioDeviceVolume(Integer vol) {
        ModConfigs.CLIENT.audioDeviceVolume.set(vol);
    }

    public static String getTransmitterMidiPath() {
        return ModConfigs.CLIENT.transmitterMidiPath.get();
    }

    public static void setTransmitterMidiPath(String path) {
        ModConfigs.CLIENT.transmitterMidiPath.set(path);
    }

    // SYNTH

    // Read-Only
    public static Boolean isInstrumentalistShopEnabled() {
        return ModConfigs.COMMON.enableInstrumentalistShop.get();
    }

    public static List<String> getAllowedInstrumentMobs() {
        return Arrays.asList(ModConfigs.COMMON.allowedInstrumentMobs.get().split(","));
    }
    
    public static Integer getLocalBufferms() {
        return ModConfigs.CLIENT.localBufferms.get();
    }

    public static Boolean getJitterCorrection() {
        return ModConfigs.CLIENT.jitterCorrection.get();
    }

    public static Integer getLatency() {
        return ModConfigs.CLIENT.latency.get();
    }

    public static Integer getLocalLatency() {
        return ModConfigs.CLIENT.localLatency.get();
    }

    public static Integer getSynthSampleRate() {
        return ModConfigs.CLIENT.synthSampleRate.get();
    }

    public static Integer getSynthBitRate() {
        return ModConfigs.CLIENT.synthBitRate.get();
    }

    public static String getSoundfontPath() {
        return ModConfigs.CLIENT.soundfontPath.get();
    }
}
