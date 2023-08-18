package io.github.tofodroid.mods.mimi.client.midi;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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
