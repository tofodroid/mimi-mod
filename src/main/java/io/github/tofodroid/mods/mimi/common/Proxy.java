package io.github.tofodroid.mods.mimi.common;

import io.github.tofodroid.mods.mimi.common.midi.FilesystemMidiFileProvider;
import net.minecraft.Util;

public interface Proxy {
    public Boolean isInitialized();
    public Boolean isClient();
    public Long getServerStartEpoch();
    public FilesystemMidiFileProvider serverMidiFiles();
    public FilesystemMidiFileProvider clientMidiFiles();
    public void init();

    default public Long getBaselineBufferMs() {
        return 100l;
    }

    default public Long getCurrentServerMillis() {
        return Util.getEpochMillis() - getServerStartEpoch();
    }
}
