package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockBroadcaster;
import io.github.tofodroid.mods.mimi.common.block.BlockConductor;
import io.github.tofodroid.mods.mimi.common.block.BlockDiskWriter;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.BlockListener;
import io.github.tofodroid.mods.mimi.common.block.BlockMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.block.BlockReceiver;
import io.github.tofodroid.mods.mimi.common.block.BlockTuningTable;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;

public final class ModItems {
    // Instruments
    public static List<ItemInstrument> INSTRUMENT_ITEMS;
    public static List<ItemInstrumentBlock> BLOCK_INSTRUMENT_ITEMS;

    // Other
    public static ItemMidiDeviceConfig DEVICECONFIG;
    public static ItemTransmitter TRANSMITTER;
    public static ItemFileCaster FILECASTER;
    public static ItemMidiSwitchboard SWITCHBOARD;
    public static ItemFloppyDisk FLOPPYDISK;

    // Blocks - Redstone
    public static Item LISTENER;
    public static Item RECEIVER;
    public static Item MECHANICALMAESTRO;
    public static Item CONDUCTOR;

    // Blocks - Other
    public static Item TUNINGTABLE;
    public static Item DISKWRITER;
    public static Item BROADCASTER;

    public static MIMIModItemGroup ITEM_GROUP;

    public static void submitRegistrations(final RegistryEvent.Register<Item> event) {
        ITEM_GROUP = new MIMIModItemGroup();

        // Other Items
        DEVICECONFIG = new ItemMidiDeviceConfig();
        event.getRegistry().register(DEVICECONFIG);

        TRANSMITTER = new ItemTransmitter();
        event.getRegistry().register(TRANSMITTER);

        FILECASTER = new ItemFileCaster();
        event.getRegistry().register(FILECASTER);

        SWITCHBOARD = new ItemMidiSwitchboard();
        event.getRegistry().register(SWITCHBOARD);

        FLOPPYDISK = new ItemFloppyDisk();
        event.getRegistry().register(FLOPPYDISK);

        // Redstone Blocks
        LISTENER = new BlockItem(ModBlocks.LISTENER.get(), new Item.Properties().tab(ITEM_GROUP).stacksTo(64)).setRegistryName(BlockListener.REGISTRY_NAME);
        event.getRegistry().register(LISTENER);

        RECEIVER = new BlockItem(ModBlocks.RECEIVER.get(), new Item.Properties().tab(ITEM_GROUP).stacksTo(64)).setRegistryName(BlockReceiver.REGISTRY_NAME);
        event.getRegistry().register(RECEIVER);

        MECHANICALMAESTRO = new BlockItem(ModBlocks.MECHANICALMAESTRO.get(), new Item.Properties().tab(ITEM_GROUP).stacksTo(64)).setRegistryName(BlockMechanicalMaestro.REGISTRY_NAME);
        event.getRegistry().register(MECHANICALMAESTRO);
        
        CONDUCTOR = new BlockItem(ModBlocks.CONDUCTOR.get(), new Item.Properties().tab(ITEM_GROUP).stacksTo(64)).setRegistryName(BlockConductor.REGISTRY_NAME);
        event.getRegistry().register(CONDUCTOR);

        // Other Blocks
        TUNINGTABLE = new BlockItem(ModBlocks.TUNINGTABLE.get(), new Item.Properties().tab(ITEM_GROUP).stacksTo(64)).setRegistryName(BlockTuningTable.REGISTRY_NAME);
        event.getRegistry().register(TUNINGTABLE);
        
        DISKWRITER = new BlockItem(ModBlocks.DISKWRITER.get(), new Item.Properties().tab(ITEM_GROUP).stacksTo(64)).setRegistryName(BlockDiskWriter.REGISTRY_NAME);
        event.getRegistry().register(DISKWRITER);

        BROADCASTER = new BlockItem(ModBlocks.BROADCASTER.get(), new Item.Properties().tab(ITEM_GROUP).stacksTo(64)).setRegistryName(BlockBroadcaster.REGISTRY_NAME);
        event.getRegistry().register(BROADCASTER);

        // Instrument Items
        INSTRUMENT_ITEMS = buildInstruments();
        INSTRUMENT_ITEMS.forEach((ItemInstrument instrument) -> {
            event.getRegistry().register(instrument);
        });

        // Instrument Block Items
        BLOCK_INSTRUMENT_ITEMS = buildBlockInstruments();
        BLOCK_INSTRUMENT_ITEMS.forEach((ItemInstrumentBlock instrument) -> {
            event.getRegistry().register(instrument);
        });
    }

    public static List<ItemInstrument> buildInstruments() {
        List<ItemInstrument> list = new ArrayList<>();
        for(InstrumentSpec instrument : InstrumentConfig.getItemInstruments()) {
            list.add(new ItemInstrument(instrument.registryName, instrument.instrumentId, instrument.isDyeable(), instrument.defaultColor()));
        }
        return list;
    }

    public static List<ItemInstrumentBlock> buildBlockInstruments() {
        List<ItemInstrumentBlock> list = new ArrayList<>();
        for(InstrumentSpec instrument : InstrumentConfig.getBlockInstruments()) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(MIMIMod.MODID, instrument.registryName));

            if(block instanceof BlockInstrument) {
                list.add((ItemInstrumentBlock)new ItemInstrumentBlock((BlockInstrument)block, new Item.Properties().tab(ITEM_GROUP).stacksTo(1), instrument.registryName));
            } else {
                MIMIMod.LOGGER.error("Failed to create ItemInstrumentBlock for Instrument: " + instrument.registryName + " - Corresponding Registry Block is not a BlockInstrument!");
            }            
        }
        return list;
    }
}