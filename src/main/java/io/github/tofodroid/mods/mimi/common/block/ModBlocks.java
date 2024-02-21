package io.github.tofodroid.mods.mimi.common.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.legacycompat.BlockBroadcaster;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class ModBlocks {
    public static final Map<ResourceLocation, Block> BLOCKS = new HashMap<>();

    // Redstone Blocks
    public static final BlockTransmitter TRANSMITTERBLOCK = create(BlockTransmitter.REGISTRY_NAME, new BlockTransmitter());
    public static final BlockListener LISTENER = create(BlockListener.REGISTRY_NAME, new BlockListener());
    public static final BlockReceiver RECEIVER = create(BlockReceiver.REGISTRY_NAME, new BlockReceiver());
    public static final BlockMechanicalMaestro MECHANICALMAESTRO = create(BlockMechanicalMaestro.REGISTRY_NAME, new BlockMechanicalMaestro());
    public static final BlockConductor CONDUCTOR = create(BlockConductor.REGISTRY_NAME, new BlockConductor());
    public static final BlockEffectEmitter EFFECTEMITTER = create(BlockEffectEmitter.REGISTRY_NAME, new BlockEffectEmitter());

    // Legacy Compat
    public static final BlockBroadcaster BROADCASTER = create(BlockBroadcaster.REGISTRY_NAME, new BlockBroadcaster());


    // Village Blocks
    public static final BlockTuningTable TUNINGTABLE = create(BlockTuningTable.REGISTRY_NAME, new BlockTuningTable());

    // LED Cube Blocks
    public static final BlockLedCube LEDCUBE_A = create(BlockLedCube.REGISTRY_NAME_A, new BlockLedCube());
    public static final BlockLedCube LEDCUBE_B = create(BlockLedCube.REGISTRY_NAME_B, new BlockLedCube());
    public static final BlockLedCube LEDCUBE_C = create(BlockLedCube.REGISTRY_NAME_C, new BlockLedCube());
    public static final BlockLedCube LEDCUBE_D = create(BlockLedCube.REGISTRY_NAME_D, new BlockLedCube());
    public static final BlockLedCube LEDCUBE_E = create(BlockLedCube.REGISTRY_NAME_E, new BlockLedCube());
    public static final BlockLedCube LEDCUBE_F = create(BlockLedCube.REGISTRY_NAME_F, new BlockLedCube());
    public static final BlockLedCube LEDCUBE_G = create(BlockLedCube.REGISTRY_NAME_G, new BlockLedCube());
    public static final BlockLedCube LEDCUBE_H = create(BlockLedCube.REGISTRY_NAME_H, new BlockLedCube());

    // Instrument Blocks
    public static final List<BlockInstrument> INSTRUMENTS = buildInstruments();

    public static List<BlockInstrument> buildInstruments()  {
        List<BlockInstrument> result = new ArrayList<>();
        for(InstrumentSpec instrument : InstrumentConfig.getBlockInstruments()) {
            result.add(create(instrument.registryName, new BlockInstrument(instrument)));
        }
        return result;
    }

    public static <T extends Block> T create(String id, T block) {
        BLOCKS.put(new ResourceLocation(MIMIMod.MODID, id), block);
        return block;
    }
}
