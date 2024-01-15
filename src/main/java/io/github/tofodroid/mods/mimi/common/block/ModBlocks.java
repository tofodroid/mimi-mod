package io.github.tofodroid.mods.mimi.common.block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MIMIMod.MODID);

    // Redstone Blocks
    public static final RegistryObject<BlockTransmitter> TRANSMITTER = BLOCKS.register(BlockTransmitter.REGISTRY_NAME, () -> new BlockTransmitter());
    public static final RegistryObject<BlockListener> LISTENER = BLOCKS.register(BlockListener.REGISTRY_NAME, () -> new BlockListener());
    public static final RegistryObject<BlockReceiver> RECEIVER = BLOCKS.register(BlockReceiver.REGISTRY_NAME, () -> new BlockReceiver());
    public static final RegistryObject<BlockMechanicalMaestro> MECHANICALMAESTRO = BLOCKS.register(BlockMechanicalMaestro.REGISTRY_NAME, () -> new BlockMechanicalMaestro());
    public static final RegistryObject<BlockConductor> CONDUCTOR = BLOCKS.register(BlockConductor.REGISTRY_NAME, () -> new BlockConductor());

    // Village Blocks
    public static final RegistryObject<BlockTuningTable> TUNINGTABLE = BLOCKS.register(BlockTuningTable.REGISTRY_NAME, () -> new BlockTuningTable());

    // LED Cube Blocks
    public static final RegistryObject<BlockLedCube> LEDCUBE_A = BLOCKS.register(BlockLedCube.REGISTRY_NAME_A, () -> new BlockLedCube());
    public static final RegistryObject<BlockLedCube> LEDCUBE_B = BLOCKS.register(BlockLedCube.REGISTRY_NAME_B, () -> new BlockLedCube());
    public static final RegistryObject<BlockLedCube> LEDCUBE_C = BLOCKS.register(BlockLedCube.REGISTRY_NAME_C, () -> new BlockLedCube());
    public static final RegistryObject<BlockLedCube> LEDCUBE_D = BLOCKS.register(BlockLedCube.REGISTRY_NAME_D, () -> new BlockLedCube());
    public static final RegistryObject<BlockLedCube> LEDCUBE_E = BLOCKS.register(BlockLedCube.REGISTRY_NAME_E, () -> new BlockLedCube());
    public static final RegistryObject<BlockLedCube> LEDCUBE_F = BLOCKS.register(BlockLedCube.REGISTRY_NAME_F, () -> new BlockLedCube());
    public static final RegistryObject<BlockLedCube> LEDCUBE_G = BLOCKS.register(BlockLedCube.REGISTRY_NAME_G, () -> new BlockLedCube());
    public static final RegistryObject<BlockLedCube> LEDCUBE_H = BLOCKS.register(BlockLedCube.REGISTRY_NAME_H, () -> new BlockLedCube());

    // Instrument Blocks
    public static final List<RegistryObject<BlockInstrument>> INSTRUMENTS = buildInstruments();

    public static <T extends Block> List<T> getBlocksFromRegistryList(List<RegistryObject<T>> regList) {
        return regList.stream().map(r -> r.get()).collect(Collectors.toList());
    }

    public static List<BlockInstrument> getBlockInstruments() {
        return getBlocksFromRegistryList(INSTRUMENTS);
    }

    public static List<RegistryObject<BlockInstrument>> buildInstruments()  {
        List<RegistryObject<BlockInstrument>> result = new ArrayList<>();
        for(InstrumentSpec instrument : InstrumentConfig.getBlockInstruments()) {
            MIMIMod.LOGGER.info("Reg instrument - " + instrument.registryName);
            result.add(
                BLOCKS.register(
                    instrument.registryName, 
                    () -> new BlockInstrument(instrument)
                )
            );
        }
        return result;
    }
}
