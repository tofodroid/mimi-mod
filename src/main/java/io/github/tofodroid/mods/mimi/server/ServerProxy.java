package io.github.tofodroid.mods.mimi.server;

import io.github.tofodroid.mods.mimi.common.Proxy;
import io.github.tofodroid.mods.mimi.common.midi.FilesystemMidiFileProvider;
import io.github.tofodroid.mods.mimi.util.TimeUtils;

public class ServerProxy implements Proxy {
    private Boolean initialized = false;
    private final Long serverStartEpoch = TimeUtils.getNowTime();
    private FilesystemMidiFileProvider MIDI_FILES;

    @Override
    public void init() {
        MIDI_FILES = new FilesystemMidiFileProvider(true);
        MIDI_FILES.loadSongs();
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
