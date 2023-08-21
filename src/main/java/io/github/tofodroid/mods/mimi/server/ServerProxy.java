package io.github.tofodroid.mods.mimi.server;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.Proxy;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileManager;
import net.minecraft.Util;

public class ServerProxy implements Proxy {
    private final Long serverStartEpoch = Util.getEpochMillis();
    private MidiFileManager DEFAULT_MIDI_FILES = new MidiFileManager();

    @Override
    public Boolean isClient() {
        return false;
    }

    @Override
    public Long getServerStartEpoch() {
        MIMIMod.LOGGER.info("TIME SYNC SERVER:\n\tServer Start Millis: " + this.serverStartEpoch);
        return serverStartEpoch;
    }

    @Override
    public MidiFileManager defaultMidiFiles() {
        return DEFAULT_MIDI_FILES;
    }

    @Override
    public MidiFileManager customMidiFiles() {
        MIMIMod.LOGGER.warn("Server attempted to access custom MIDI files which are not supported!");
        return DEFAULT_MIDI_FILES;
    }

}
