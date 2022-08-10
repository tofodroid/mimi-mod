package io.github.tofodroid.mods.mimi.server;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
public class ServerEventHandler {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        MIMIMod.LOGGER.info("Preinit ServerMusicPlayerMidiManager");
        ServerMusicPlayerMidiManager.preInit();
    }
}
