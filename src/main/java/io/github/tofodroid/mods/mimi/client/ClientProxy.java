package io.github.tofodroid.mods.mimi.client;

import java.util.List;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.client.midi.MidiInputManager;
import io.github.tofodroid.mods.mimi.client.midi.MidiSynthManager;
import io.github.tofodroid.mods.mimi.client.renderer.EntitySeatRenderer;
import io.github.tofodroid.mods.mimi.client.renderer.EntityNoteResponseTileRenderer;
import io.github.tofodroid.mods.mimi.common.Proxy;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.keybind.ModBindings;
import io.github.tofodroid.mods.mimi.common.item.IDyeableInstrumentItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
        MinecraftForge.EVENT_BUS.register(MIDI_INPUT);

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

    @Mod.EventBusSubscriber(value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientRegistrationHandler {
        @SubscribeEvent
        public static void register(ColorHandlerEvent.Item event) {
            registerItemColors(event, ModItems.INSTRUMENT_ITEMS.stream().filter(i -> i.isDyeable()).collect(Collectors.toList()));
            registerItemColors(event, ModItems.BLOCK_INSTRUMENT_ITEMS.stream().filter(i -> i.isDyeable()).collect(Collectors.toList()));
        }

        @SubscribeEvent
        public static void register(ColorHandlerEvent.Block event) {
            registerBlockColors(event, ModBlocks.INSTRUMENTS.stream().filter(i -> i.isDyeable()).collect(Collectors.toList()));
        }

        @SubscribeEvent
        public static void register(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.SEAT, EntitySeatRenderer::new);
            event.registerEntityRenderer(ModEntities.NOTERESPONSIVETILE, EntityNoteResponseTileRenderer::new);
        }
    }

    protected static void registerItemColors(ColorHandlerEvent.Item event, List<? extends Item> items) {
        event.getItemColors().register((stack, color) ->
                    color > 0 ? -1 : ((IDyeableInstrumentItem) stack.getItem()).getColor(stack), items.toArray(new Item[items.size()]));
    }

    protected static void registerBlockColors(ColorHandlerEvent.Block event, List<? extends Block> blocks) {
        event.getBlockColors().register((state, reader, pos, color) -> {
            return reader != null && pos != null && reader.getBlockEntity(pos) != null && reader.getBlockEntity(pos) instanceof TileInstrument ? 
                ((TileInstrument)reader.getBlockEntity(pos)).getColor() : -1;
         }, blocks.toArray(new Block[blocks.size()]));
    }
}
