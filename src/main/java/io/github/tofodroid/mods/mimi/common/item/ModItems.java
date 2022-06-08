package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockConductor;
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
    public static ItemTransmitter TRANSMITTER;
    public static ItemMidiSwitchboard SWITCHBOARD;

    // Blocks - Redstone
    public static BlockItem LISTENER;
    public static BlockItem RECEIVER;
    public static BlockItem MECHANICALMAESTRO;
    public static BlockItem CONDUCTOR;

    // Blocks - Other
    public static BlockItem TUNINGTABLE;

    public static MIMIModItemGroup ITEM_GROUP;

    public static void submitRegistrations(final RegisterEvent.RegisterHelper<Item> event) {
        ITEM_GROUP = new MIMIModItemGroup();

        // Other Items
        TRANSMITTER = new ItemTransmitter();
        event.register(ItemTransmitter.REGISTRY_NAME, TRANSMITTER);

        SWITCHBOARD = new ItemMidiSwitchboard();
        event.register(ItemMidiSwitchboard.REGISTRY_NAME, SWITCHBOARD);

        // Redstone Blocks
        LISTENER = new BlockItem(ModBlocks.LISTENER, new Item.Properties().tab(ITEM_GROUP).stacksTo(64));
        event.register(BlockListener.REGISTRY_NAME, LISTENER);

        RECEIVER = new BlockItem(ModBlocks.RECEIVER, new Item.Properties().tab(ITEM_GROUP).stacksTo(64));
        event.register(BlockReceiver.REGISTRY_NAME, RECEIVER);

        MECHANICALMAESTRO = new BlockItem(ModBlocks.MECHANICALMAESTRO, new Item.Properties().tab(ITEM_GROUP).stacksTo(64));
        event.register(BlockMechanicalMaestro.REGISTRY_NAME, MECHANICALMAESTRO);
        
        CONDUCTOR = new BlockItem(ModBlocks.CONDUCTOR, new Item.Properties().tab(ITEM_GROUP).stacksTo(64));
        event.register(BlockConductor.REGISTRY_NAME, CONDUCTOR);

        // Village Blocks
        TUNINGTABLE = new BlockItem(ModBlocks.TUNINGTABLE, new Item.Properties().tab(ITEM_GROUP).stacksTo(64));
        event.register(BlockTuningTable.REGISTRY_NAME, TUNINGTABLE);

        // Instrument Items
        INSTRUMENT_ITEMS = buildInstruments();
        INSTRUMENT_ITEMS.forEach((ItemInstrument instrument) -> {
            event.register(instrument.REGISTRY_NAME, instrument);
        });

        // Instrument Block Items
        BLOCK_INSTRUMENT_ITEMS = buildBlockInstruments();
        BLOCK_INSTRUMENT_ITEMS.forEach((ItemInstrumentBlock instrument) -> {
            event.register(instrument.REGISTRY_NAME, instrument);
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