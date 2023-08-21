package io.github.tofodroid.mods.mimi.client.midi;

import net.minecraftforge.common.MinecraftForge;

public class MidiInputManager {
    public final MidiInputDeviceManager inputDeviceManager;
    public final EnderTransmitterManager enderTransmitterManager;

    public MidiInputManager() {
        this.inputDeviceManager = new MidiInputDeviceManager();
        MinecraftForge.EVENT_BUS.register(this.inputDeviceManager);
        this.enderTransmitterManager = new EnderTransmitterManager();
        this.inputDeviceManager.open();
    }
}
