package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.Builder;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.Arrays;
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
    public static List<ItemInstrumentHandheld> INSTRUMENT_ITEMS;
    public static List<ItemInstrumentBlock> BLOCK_INSTRUMENT_ITEMS;

    // Other
    public static ItemMidiDeviceConfig DEVICECONFIG;
    public static ItemFileCaster FILECASTER;

    // Blocks - Redstone
    public static BlockItem LISTENER;
    public static BlockItem RECEIVER;
    public static BlockItem MECHANICALMAESTRO;
    public static BlockItem CONDUCTOR;

    // Blocks - Other
    public static BlockItem TUNINGTABLE;

    public static void registerCreativeTab(final RegisterEvent.RegisterHelper<CreativeModeTab> event) {
        Builder builder = CreativeModeTab.builder();
        // Set name of tab to display
        builder.title(Component.translatable("itemGroup." + MIMIMod.MODID + ".group"))
        // Set icon of creative tab
        .icon(() -> new ItemStack(ModBlocks.getBlockInstruments().get(0)))
        // Add default items to tab
        .displayItems((parameters, output) -> {
            output.acceptAll(getStacksForItems(INSTRUMENT_ITEMS));
            output.acceptAll(getStacksForItems(BLOCK_INSTRUMENT_ITEMS));
            output.acceptAll(getStacksForItems(Arrays.asList(
                DEVICECONFIG,
                FILECASTER,
                LISTENER,
                RECEIVER,
                MECHANICALMAESTRO,
                CONDUCTOR,
                TUNINGTABLE
            )));
        });
        event.register(new ResourceLocation(MIMIMod.MODID, "group"), builder.build());
    }

    public static List<ItemStack> getStacksForItems(List<? extends Item> items) {
        return items.stream().map(i -> new ItemStack(i)).toList();
    }

    public static void submitRegistrations(final RegisterEvent.RegisterHelper<Item> event) {
        // Other Items
        DEVICECONFIG = new ItemMidiDeviceConfig();
        event.register(ItemMidiDeviceConfig.REGISTRY_NAME, DEVICECONFIG);

        FILECASTER = new ItemFileCaster();
        event.register(ItemFileCaster.REGISTRY_NAME, FILECASTER);

        // Redstone Blocks
        LISTENER = new BlockItem(ModBlocks.LISTENER.get(), new Item.Properties().stacksTo(64));
        event.register(BlockListener.REGISTRY_NAME, LISTENER);

        RECEIVER = new BlockItem(ModBlocks.RECEIVER.get(), new Item.Properties().stacksTo(64));
        event.register(BlockReceiver.REGISTRY_NAME, RECEIVER);

        MECHANICALMAESTRO = new BlockItem(ModBlocks.MECHANICALMAESTRO.get(), new Item.Properties().stacksTo(64));
        event.register(BlockMechanicalMaestro.REGISTRY_NAME, MECHANICALMAESTRO);
        
        CONDUCTOR = new BlockItem(ModBlocks.CONDUCTOR.get(), new Item.Properties().stacksTo(64));
        event.register(BlockConductor.REGISTRY_NAME, CONDUCTOR);

        // Other Blocks
        TUNINGTABLE = new BlockItem(ModBlocks.TUNINGTABLE.get(), new Item.Properties().stacksTo(64));
        event.register(BlockTuningTable.REGISTRY_NAME, TUNINGTABLE);
        
        // Instrument Items
        INSTRUMENT_ITEMS = buildHandheldInstruments();
        INSTRUMENT_ITEMS.forEach((ItemInstrumentHandheld instrument) -> {
            event.register(instrument.REGISTRY_NAME, instrument);
        });

        // Instrument Block Items
        BLOCK_INSTRUMENT_ITEMS = buildBlockInstruments();
        BLOCK_INSTRUMENT_ITEMS.forEach((ItemInstrumentBlock instrument) -> {
            event.register(instrument.REGISTRY_NAME, instrument);
        });
    }

    public static List<ItemInstrumentHandheld> buildHandheldInstruments() {
        List<ItemInstrumentHandheld> list = new ArrayList<>();
        for(InstrumentSpec instrument : InstrumentConfig.getItemInstruments()) {
            list.add(new ItemInstrumentHandheld(instrument));
        }
        return list;
    }

    public static List<ItemInstrumentBlock> buildBlockInstruments() {
        List<ItemInstrumentBlock> list = new ArrayList<>();
        for(InstrumentSpec instrument : InstrumentConfig.getBlockInstruments()) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(MIMIMod.MODID, instrument.registryName));

            if(block instanceof BlockInstrument) {
                list.add((ItemInstrumentBlock)new ItemInstrumentBlock((BlockInstrument)block, new Item.Properties().stacksTo(1), instrument.registryName));
            } else {
                MIMIMod.LOGGER.error("Failed to create ItemInstrumentBlock for Instrument: " + instrument.registryName + " - Corresponding Registry Block is not a BlockInstrument!");
            }            
        }
        return list;
    }
}