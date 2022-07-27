package io.github.tofodroid.mods.mimi.client;

import io.github.tofodroid.mods.mimi.client.midi.MidiInputManager;
import io.github.tofodroid.mods.mimi.client.midi.synth.MidiMultiSynthManager;
import io.github.tofodroid.mods.mimi.common.Proxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClientProxy implements Proxy {
    private MidiMultiSynthManager MIDI_SYNTH;
    private MidiInputManager MIDI_INPUT;

    @Override
    public void init(final FMLCommonSetupEvent event) {
        // MIDI
        MIDI_SYNTH = new MidiMultiSynthManager();
        MinecraftForge.EVENT_BUS.register(MIDI_SYNTH);
        
        MIDI_INPUT = new MidiInputManager();
        MinecraftForge.EVENT_BUS.register(MIDI_INPUT);
    }

    @Override
    public MidiMultiSynthManager getMidiSynth() {
        return MIDI_SYNTH;
    }

    @Override
    public MidiInputManager getMidiInput() {
        return MIDI_INPUT;
    }
}
