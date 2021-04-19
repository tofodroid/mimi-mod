package io.github.tofodroid.mods.mimi.client;

import io.github.tofodroid.mods.mimi.client.gui.ScreenContainerDiskRecorder;
import io.github.tofodroid.mods.mimi.client.midi.MidiInputManager;
import io.github.tofodroid.mods.mimi.client.midi.MidiSynthManager;
import io.github.tofodroid.mods.mimi.client.renderer.EntitySeatRenderer;
import io.github.tofodroid.mods.mimi.common.Proxy;
import io.github.tofodroid.mods.mimi.common.container.ModContainers;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.keybind.ModBindings;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClientProxy implements Proxy {
    private MidiSynthManager MIDI_SYNTH;
    private MidiInputManager MIDI_INPUT;

    @Override
    public void init(final FMLCommonSetupEvent event) {
        // MIDI
        MIDI_SYNTH = new MidiSynthManager();
        MinecraftForge.EVENT_BUS.register(MIDI_SYNTH);
        
        MIDI_INPUT = new MidiInputManager();

        // Screens
        ScreenManager.registerFactory(ModContainers.CONTAINER_TYPE_CONTAINER_DISK_RECORDER, ScreenContainerDiskRecorder::new);

        // Rendering
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.SEAT, EntitySeatRenderer::new);

        // Keybinds
        ModBindings.register();
        MinecraftForge.EVENT_BUS.register(new ModBindings());
    }

    @Override
    public MidiSynthManager getMidiSynth() {
        return MIDI_SYNTH;
    }

    @Override
    public MidiInputManager getMidiInput() {
        return MIDI_INPUT;
    }
}
