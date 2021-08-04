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
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.midi.MidiInstrument;

@ObjectHolder(MIMIMod.MODID)
public final class ModItems {
    // Instruments
    public static List<ItemInstrument> INSTRUMENT_ITEMS = null;
    public static List<ItemInstrumentBlock> BLOCK_INSTRUMENT_ITEMS = null;

    // Other
    public static final ItemTransmitter TRANSMITTER = null;
    public static final ItemMidiSwitchboard SWITCHBOARD = null;

    // Blocks - Other
    public static final BlockItem LISTENER = null;
    public static final BlockItem ADVLISTENER = null;
    public static final BlockItem RECEIVER = null;

    public static MIMIModItemGroup ITEM_GROUP;

    @Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            ITEM_GROUP = new MIMIModItemGroup();

            // Other Items
            event.getRegistry().register(new ItemTransmitter());
            event.getRegistry().register(new ItemMidiSwitchboard());

            // Other Blocks
            event.getRegistry().register(new BlockItem(ModBlocks.LISTENER, new Item.Properties().group(ITEM_GROUP).maxStackSize(64)).setRegistryName("listener"));
            event.getRegistry().register(new BlockItem(ModBlocks.ADVLISTENER, new Item.Properties().group(ITEM_GROUP).maxStackSize(64)).setRegistryName("advlistener"));
            event.getRegistry().register(new BlockItem(ModBlocks.RECEIVER, new Item.Properties().group(ITEM_GROUP).maxStackSize(64)).setRegistryName("receiver"));
            event.getRegistry().register(new BlockItem(ModBlocks.MECHANICALMAESTRO, new Item.Properties().group(ITEM_GROUP).maxStackSize(64)).setRegistryName("mechanicalmaestro"));

            // Instrument Items
            INSTRUMENT_ITEMS = buildInstruments();
            event.getRegistry().registerAll(INSTRUMENT_ITEMS.toArray(new ItemInstrument[INSTRUMENT_ITEMS.size()]));

            // Instrument Block Items
            BLOCK_INSTRUMENT_ITEMS = buildBlockInstruments();
            event.getRegistry().registerAll(BLOCK_INSTRUMENT_ITEMS.toArray(new ItemInstrumentBlock[BLOCK_INSTRUMENT_ITEMS.size()]));
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

    public static ItemInstrument getInstrumentItemById(Byte instrumentId) {
        for(ItemInstrument item : INSTRUMENT_ITEMS) {
            if(item.getInstrumentId().equals(instrumentId)) {
                return item;
            }
        }

        return null;
    }
    
    public static List<ItemInstrumentBlock> buildBlockInstruments() {
        List<ItemInstrumentBlock> list = new ArrayList<>();
        for(BlockInstrument instrument : ModBlocks.INSTRUMENTS) {
            list.add((ItemInstrumentBlock)new ItemInstrumentBlock(instrument, new Item.Properties().group(ITEM_GROUP).maxStackSize(1)).setRegistryName(instrument.getRegistryName()));
        }
        return list;
    }
}