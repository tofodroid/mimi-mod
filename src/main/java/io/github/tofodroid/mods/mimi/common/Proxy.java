package io.github.tofodroid.mods.mimi.common;

import io.github.tofodroid.mods.mimi.common.midi.FilesystemMidiFileProvider;
import io.github.tofodroid.mods.mimi.util.TimeUtils;

public interface Proxy {
    public Boolean isInitialized();
    public Boolean isClient();
    public Long getServerStartEpoch();
    public FilesystemMidiFileProvider serverMidiFiles();
    public FilesystemMidiFileProvider clientMidiFiles();
    public void init();

    default public Long getBaselineBufferMs() {
        return 75l;
    }

    default public Long getCurrentServerMillis() {
        return TimeUtils.getNowTime() - getServerStartEpoch();
    }
}
