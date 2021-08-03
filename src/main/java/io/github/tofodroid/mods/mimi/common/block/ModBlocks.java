package io.github.tofodroid.mods.mimi.common.block;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.MidiInstrument;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(MIMIMod.MODID)
public class ModBlocks {
    // Other Blocks
    public static final BlockListener LISTENER = null;
    public static final BlockAdvListener ADVLISTENER = null;
    public static final BlockReceiver RECEIVER = null;

    // Instrument Blocks
    public static List<BlockInstrument> INSTRUMENTS = null;

    @Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event) {
            event.getRegistry().registerAll(
                new BlockListener(),
                new BlockAdvListener(),
                new BlockReceiver()
            );
            INSTRUMENTS = buildInstruments();
            event.getRegistry().registerAll(INSTRUMENTS.toArray(new BlockInstrument[INSTRUMENTS.size()]));
        }
    }

    public static List<BlockInstrument> buildInstruments()  {
        List<BlockInstrument> result = new ArrayList<>();
        for(MidiInstrument instrument : MidiInstrument.values()) {
            if(instrument.isBlock()) {
                try {
                    result.add(instrument.getBlockClass().newInstance());
                } catch(InstantiationException | IllegalAccessException e) {
                    // TODO
                }
            }
        }

        return result;
    }
}
