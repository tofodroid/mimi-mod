package io.github.tofodroid.mods.mimi.common.block;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(MIMIMod.MODID)
public class ModBlocks {
    // Redstone Blocks
    public static final BlockListener LISTENER = null;
    public static final BlockReceiver RECEIVER = null;
    public static final BlockMechanicalMaestro MECHANICALMAESTRO = null;
    public static final BlockConductor CONDUCTOR = null;

    // Village Blocks
    public static final BlockTuningTable TUNINGTABLE = null;

    // Instrument Blocks
    public static List<BlockInstrument> INSTRUMENTS = null;

    @Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event) {
            event.getRegistry().registerAll(
                new BlockListener(),
                new BlockReceiver(),
                new BlockMechanicalMaestro(),
                new BlockConductor(),
                new BlockTuningTable()
            );
            INSTRUMENTS = buildInstruments();
            event.getRegistry().registerAll(INSTRUMENTS.toArray(new BlockInstrument[INSTRUMENTS.size()]));
        }
    }

    public static List<BlockInstrument> buildInstruments()  {
        List<BlockInstrument> result = new ArrayList<>();
        for(InstrumentSpec instrument : InstrumentConfig.getBlockInstruments()) {
            result.add(new BlockInstrument(instrument.instrumentId, instrument.registryName, instrument.isDyeable(), instrument.defaultColor(), VoxelShapeUtils.loadFromStrings(instrument.collisionShapes)));
        }
        return result;
    }
}
