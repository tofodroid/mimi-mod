package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.midi.MidiInstrument;

@ObjectHolder(MIMIMod.MODID)
public final class ModItems {
    // Other
    public static final ItemTransmitter TRANSMITTER = null;

    // Instruments
    public static List<ItemInstrument> INSTRUMENT_ITEMS = null;

    // Blocks - Instruments
    public static final BlockItem PIANO = null;
    public static final BlockItem DRUMS = null;

    public static MIMIModItemGroup ITEM_GROUP;

    @Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            ITEM_GROUP = new MIMIModItemGroup();

            // Other Items
            event.getRegistry().register(new ItemTransmitter());

            // Instrument Items
            INSTRUMENT_ITEMS = buildInstruments();
            event.getRegistry().registerAll(INSTRUMENT_ITEMS.toArray(new ItemInstrument[INSTRUMENT_ITEMS.size()]));

            // Instrument Block Items
            event.getRegistry().register(new BlockItem(ModBlocks.PIANO, new Item.Properties().group(ITEM_GROUP).maxStackSize(1)).setRegistryName("piano"));
            event.getRegistry().register(new BlockItem(ModBlocks.DRUMS, new Item.Properties().group(ITEM_GROUP).maxStackSize(1)).setRegistryName("drums"));
        }
    }

    public static List<ItemInstrument> buildInstruments() {
        List<ItemInstrument> list = new ArrayList<>();
        for(MidiInstrument instrument : MidiInstrument.values()) {
            if(!instrument.isBlock()) {
                list.add(new ItemInstrument(instrument.toString().toLowerCase(), instrument.getId()));
            }
        }
        return list;
    }
}