package io.github.tofodroid.mods.mimi.common;

import io.github.tofodroid.mods.mimi.client.midi.MidiInputManager;
import io.github.tofodroid.mods.mimi.client.midi.MidiSynthManager;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public interface Proxy {
    public MidiSynthManager getMidiSynth();
    public MidiInputManager getMidiInput();
    public void init(final FMLCommonSetupEvent event);
}
