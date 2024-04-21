package io.github.tofodroid.mods.mimi.neoforge.client;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void handleSoundReload(SoundEngineLoadEvent event) {
        if(MIMIMod.getProxy().isClient() && ((ClientProxy)MIMIMod.getProxy()).getMidiSynth() != null) {
            ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().audioDeviceManager.refreshDevice();
            ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().reloadSynths();
        }
    }
}