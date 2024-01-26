package io.github.tofodroid.mods.mimi.server;

import io.github.tofodroid.mods.mimi.common.Proxy;
import io.github.tofodroid.mods.mimi.common.midi.FilesystemMidiFileProvider;
import net.minecraft.Util;

public class ServerProxy implements Proxy {
    private Boolean initialized = false;
    private final Long serverStartEpoch = Util.getEpochMillis();
    private FilesystemMidiFileProvider MIDI_FILES = new FilesystemMidiFileProvider(true, 1);

    @Override
    public void init() {
        MIDI_FILES.refresh(true);
        this.initialized = true;
    }

    @Override
    public Boolean isClient() {
        return false;
    }

    @Override
    public Long getServerStartEpoch() {
        return serverStartEpoch;
    }

    @Override
    public FilesystemMidiFileProvider serverMidiFiles() {
        return MIDI_FILES;
    }

    @Override
    public FilesystemMidiFileProvider clientMidiFiles() {
        return MIDI_FILES;
    }

    @Override
    public Boolean isInitialized() {
        return this.initialized;
    }
}
