package io.github.tofodroid.mods.mimi.common.config;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModConfigs {
    public static ClientConfig CLIENT;
    private static ForgeConfigSpec CLIENTSPEC;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = clientPair.getLeft();
        CLIENTSPEC = clientPair.getRight();
    }

    public static void preInit(ModLoadingContext context) { 
        context.registerConfig(Type.CLIENT, ModConfigs.CLIENTSPEC);
    }
    
    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading event) {
        if(event.getConfig().getSpec() == CLIENTSPEC) {
            MIMIMod.LOGGER.info("Config reload detected!");
            MIMIMod.proxy.getMidiSynth().close();
            MIMIMod.proxy.getMidiSynth().init();
        }
    }
}
