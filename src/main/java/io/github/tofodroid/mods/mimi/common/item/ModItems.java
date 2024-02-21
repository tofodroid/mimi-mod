package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockConductor;
import io.github.tofodroid.mods.mimi.common.block.BlockEffectEmitter;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.BlockListener;
import io.github.tofodroid.mods.mimi.common.block.BlockMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.block.BlockReceiver;
import io.github.tofodroid.mods.mimi.common.block.BlockTransmitter;
import io.github.tofodroid.mods.mimi.common.block.BlockTuningTable;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.block.legacycompat.BlockBroadcaster;
import io.github.tofodroid.mods.mimi.common.block.BlockLedCube;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.common.item.legacycompat.ItemFileCaster;

public final class ModItems {
    public static Map<ResourceLocation, Item> ITEMS = new HashMap<>();
    public static Map<ResourceLocation, CreativeModeTab> CREATIVE_TABS = new HashMap<>();

    // Instruments
    public static List<ItemInstrumentHandheld> INSTRUMENT_ITEMS = buildHandheldInstruments();
    public static List<ItemInstrumentBlock> BLOCK_INSTRUMENT_ITEMS = buildBlockInstruments();

    // Legacy Compat
    public static ItemFileCaster FILECASTER = create(ItemFileCaster.REGISTRY_NAME, new ItemFileCaster());
    public static BlockItem BROADCASTER = create(BlockBroadcaster.REGISTRY_NAME, new BlockItem(ModBlocks.BROADCASTER, new Item.Properties().stacksTo(64)));

    // Other
    public static ItemMidiDeviceConfig DEVICECONFIG = create(ItemMidiDeviceConfig.REGISTRY_NAME, new ItemMidiDeviceConfig());
    public static ItemTransmitter TRANSMITTER = create(ItemTransmitter.REGISTRY_NAME, new ItemTransmitter());

    // Blocks - Redstone
    public static BlockItem TRANSMITTERBLOCK = create(BlockTransmitter.REGISTRY_NAME, new BlockItem(ModBlocks.TRANSMITTERBLOCK, new Item.Properties().stacksTo(64)));
    public static BlockItem LISTENER = create(BlockListener.REGISTRY_NAME, new BlockItem(ModBlocks.LISTENER, new Item.Properties().stacksTo(64)));
    public static BlockItem RECEIVER = create(BlockReceiver.REGISTRY_NAME, new BlockItem(ModBlocks.RECEIVER, new Item.Properties().stacksTo(64)));
    public static BlockItem MECHANICALMAESTRO = create(BlockMechanicalMaestro.REGISTRY_NAME, new BlockItem(ModBlocks.MECHANICALMAESTRO, new Item.Properties().stacksTo(64)));
    public static BlockItem CONDUCTOR = create(BlockConductor.REGISTRY_NAME, new BlockItem(ModBlocks.CONDUCTOR, new Item.Properties().stacksTo(64)));
    public static BlockItem EFFECTEMITTER = create(BlockEffectEmitter.REGISTRY_NAME, new BlockItem(ModBlocks.EFFECTEMITTER, new Item.Properties().stacksTo(64)));
    
    // Blocks - LED Cubes
    public static BlockItem LEDCUBE_A = create(BlockLedCube.REGISTRY_NAME_A, new BlockItem(ModBlocks.LEDCUBE_A, new Item.Properties().stacksTo(64)));
    public static BlockItem LEDCUBE_B = create(BlockLedCube.REGISTRY_NAME_B, new BlockItem(ModBlocks.LEDCUBE_B, new Item.Properties().stacksTo(64)));
    public static BlockItem LEDCUBE_C = create(BlockLedCube.REGISTRY_NAME_C, new BlockItem(ModBlocks.LEDCUBE_C, new Item.Properties().stacksTo(64)));
    public static BlockItem LEDCUBE_D = create(BlockLedCube.REGISTRY_NAME_D, new BlockItem(ModBlocks.LEDCUBE_D, new Item.Properties().stacksTo(64)));
    public static BlockItem LEDCUBE_E = create(BlockLedCube.REGISTRY_NAME_E, new BlockItem(ModBlocks.LEDCUBE_E, new Item.Properties().stacksTo(64)));
    public static BlockItem LEDCUBE_F = create(BlockLedCube.REGISTRY_NAME_F, new BlockItem(ModBlocks.LEDCUBE_F, new Item.Properties().stacksTo(64)));
    public static BlockItem LEDCUBE_G = create(BlockLedCube.REGISTRY_NAME_G, new BlockItem(ModBlocks.LEDCUBE_G, new Item.Properties().stacksTo(64)));
    public static BlockItem LEDCUBE_H = create(BlockLedCube.REGISTRY_NAME_H, new BlockItem(ModBlocks.LEDCUBE_H, new Item.Properties().stacksTo(64)));

    // Blocks - Other
    public static BlockItem TUNINGTABLE = create(BlockTuningTable.REGISTRY_NAME, new BlockItem(ModBlocks.TUNINGTABLE, new Item.Properties().stacksTo(64)));

    // Creative Tabs
    public static CreativeModeTab CREATIVE_TAB = create("group", CreativeModeTab.builder()
        .title(Component.translatable("itemGroup." + MIMIMod.MODID + ".group"))
        // Set icon of creative tab
        .icon(() -> new ItemStack(ModBlocks.INSTRUMENTS.get(0)))
        // Add default items to tab
        .displayItems((parameters, output) -> {
            output.acceptAll(getStacksForItems(INSTRUMENT_ITEMS));
            output.acceptAll(getStacksForItems(BLOCK_INSTRUMENT_ITEMS));
            output.acceptAll(getStacksForItems(Arrays.asList(
                DEVICECONFIG,
                TRANSMITTER,
                LISTENER,
                RECEIVER,
                MECHANICALMAESTRO,
                EFFECTEMITTER,
                TRANSMITTERBLOCK,
                TUNINGTABLE,
                LEDCUBE_A,
                LEDCUBE_B,
                LEDCUBE_C,
                LEDCUBE_D,
                LEDCUBE_E,
                LEDCUBE_F,
                LEDCUBE_G,
                LEDCUBE_H
            )));
        }));
    
    public static List<ItemStack> getStacksForItems(List<? extends Item> items) {
        return items.stream().map(i -> new ItemStack(i)).collect(Collectors.toList());
    }

    public static List<ItemInstrumentHandheld> buildHandheldInstruments() {
        List<ItemInstrumentHandheld> list = new ArrayList<>();
        for(InstrumentSpec instrument : InstrumentConfig.getItemInstruments()) {
            list.add(create(instrument.registryName, new ItemInstrumentHandheld(instrument)));
        }
        return list;
    }

    public static List<ItemInstrumentBlock> buildBlockInstruments() {
        List<ItemInstrumentBlock> list = new ArrayList<>();
        for(InstrumentSpec instrument : InstrumentConfig.getBlockInstruments()) {
            Block block = ModBlocks.BLOCKS.get(new ResourceLocation(MIMIMod.MODID, instrument.registryName));

            if(block instanceof BlockInstrument) {
                list.add(create(instrument.registryName, new ItemInstrumentBlock((BlockInstrument)block, new Item.Properties().stacksTo(1), instrument.registryName)));
            } else {
                MIMIMod.LOGGER.error("Failed to create ItemInstrumentBlock for Instrument: " + instrument.registryName + " - Corresponding Registry Block is not a BlockInstrument!");
            }            
        }
        return list;
    }

    public static CreativeModeTab create(String id, CreativeModeTab.Builder builder) {
        CreativeModeTab tab = builder.build();
        CREATIVE_TABS.put(new ResourceLocation(MIMIMod.MODID, id), tab);
        return tab;
    }

    public static <T extends Item> T create(String id, T item) {
        ITEMS.put(new ResourceLocation(MIMIMod.MODID, id), item);
        return item;
    }
}