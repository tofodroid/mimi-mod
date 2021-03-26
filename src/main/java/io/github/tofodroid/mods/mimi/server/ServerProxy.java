package io.github.tofodroid.mods.mimi.server;

import io.github.tofodroid.mods.mimi.client.midi.MidiInputManager;
import io.github.tofodroid.mods.mimi.client.midi.MidiSynthManager;
import io.github.tofodroid.mods.mimi.common.Proxy;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ServerProxy implements Proxy {
    @Override
    public void init(FMLCommonSetupEvent event) {
    }

    @Override
    public MidiSynthManager getMidiSynth() {
        return null;
    }

    @Override
    public MidiInputManager getMidiInput() {
        return null;
    }
}
