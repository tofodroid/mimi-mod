package io.github.tofodroid.mods.mimi.server;

import io.github.tofodroid.mods.mimi.common.Proxy;
import io.github.tofodroid.mods.mimi.common.midi.AMidiInputManager;
import io.github.tofodroid.mods.mimi.common.midi.AMidiSynthManager;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ServerProxy implements Proxy {
    @Override
    public void init(FMLCommonSetupEvent event) {
    }

    @Override
    public AMidiSynthManager getMidiSynth() {
        return null;
    }

    @Override
    public AMidiInputManager getMidiInput() {
        return null;
    }
}
