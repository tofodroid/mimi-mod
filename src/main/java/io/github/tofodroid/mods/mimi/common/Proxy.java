package io.github.tofodroid.mods.mimi.common;

import io.github.tofodroid.mods.mimi.common.midi.FilesystemMidiFileProvider;
import net.minecraft.Util;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public interface Proxy {
    public Boolean isClient();
    public Long getServerStartEpoch();
    public FilesystemMidiFileProvider serverMidiFiles();
    public FilesystemMidiFileProvider clientMidiFiles();
    public void init(FMLCommonSetupEvent event);

    default public Long getBaselineBufferMs() {
        return 100l;
    }

    default public Long getCurrentServerMillis() {
        return Util.getEpochMillis() - getServerStartEpoch();
    }
}
