package io.github.tofodroid.mods.mimi.common;

import io.github.tofodroid.mods.mimi.common.midi.MidiFileManager;
import net.minecraft.Util;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public interface Proxy {
    public Boolean isClient();
    public Long getServerStartEpoch();
    public MidiFileManager defaultMidiFiles();
    public MidiFileManager customMidiFiles();

    default public void init(FMLCommonSetupEvent event) {
        defaultMidiFiles().initFromConfigFolder();
    }

    default public Long getBaselineBufferMs() {
        return 100l;
    }

    default public Long getCurrentServerMillis() {
        return Util.getEpochMillis() - getServerStartEpoch();
    }
}
