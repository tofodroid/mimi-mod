package io.github.tofodroid.mods.mimi.server;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.Proxy;
import io.github.tofodroid.mods.mimi.common.midi.FilesystemMidiFileProvider;
import net.minecraft.Util;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ServerProxy implements Proxy {
    private final Long serverStartEpoch = Util.getEpochMillis();
    private FilesystemMidiFileProvider MIDI_FILES = new FilesystemMidiFileProvider(true);

    @Override
    public void init(FMLCommonSetupEvent event) { }

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
    public FilesystemMidiFileProvider serverMidiFiles() {
        return MIDI_FILES;
    }

    @Override
    public FilesystemMidiFileProvider clientMidiFiles() {
        return MIDI_FILES;
    }
}
