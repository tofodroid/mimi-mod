package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(MIMIMod.MODID)
public class ModBlocks {
    // Instrument Blocks
    public static final BlockPiano PIANO = null;
    public static final BlockDrums DRUMS = null;
    public static final BlockDiskRecorder DISKRECORDER = null;

    @Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event) {
            event.getRegistry().registerAll(
                // Other Blocks
                new BlockDiskRecorder(),

                // Instrument Blocks
                new BlockPiano(),
                new BlockDrums()
            );
        }
    }

}
