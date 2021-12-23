package io.github.tofodroid.mods.mimi.util;

import java.time.Instant;
import java.util.Map;

import javax.sound.midi.MidiDevice;

import com.google.gson.Gson;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import net.minecraft.core.BlockPos;

public abstract class DebugUtils {

    @SuppressWarnings("rawtypes")
    public static void logNoteTimingInfo(Class clazz, Boolean onOrOff, Byte instrumentId, Byte note, Byte velocity, BlockPos notePos) {
        if(ModConfigs.CLIENT.enableMidiLogs.get())
            MIMIMod.LOGGER.info("NOTE TRACE: [" + instrumentId + "-" + note + (velocity != null ? ("-" + velocity) : "") + (notePos != null ? ("-" + notePos.getX() + "." + notePos.getY() + "." + notePos.getZ()) : "") + "] - " + (onOrOff ? "ON @ " : "OFF @ ") + clazz.getSimpleName() + ": " + Instant.now().toString());
    }

    public static void logSynthInfo(MidiDevice device, Map<String, Object> params) {
        if(ModConfigs.CLIENT.enableMidiLogs.get()) {
            MIMIMod.LOGGER.info("SYNTH INFO:   " + device.getClass().getName() + " - " + new Gson().toJson(device.getDeviceInfo()));
            MIMIMod.LOGGER.info("SYNTH PARAMS: " + new Gson().toJson(params));
        }
    }
}
