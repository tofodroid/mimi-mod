package io.github.tofodroid.mods.mimi.forge.client;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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