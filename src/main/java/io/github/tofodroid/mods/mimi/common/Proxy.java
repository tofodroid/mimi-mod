package io.github.tofodroid.mods.mimi.common;

import io.github.tofodroid.mods.mimi.common.midi.MidiInputManager;
import io.github.tofodroid.mods.mimi.common.midi.AMidiSynthManager;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public interface Proxy {
    public AMidiSynthManager getMidiSynth();
    public MidiInputManager getMidiInput();
    public void init(final FMLCommonSetupEvent event);
}
