package io.github.tofodroid.mods.mimi.forge.common;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.command.ModCommands;
import io.github.tofodroid.mods.mimi.common.mob.villager.ModVillagers;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonForgeRegistrationHandler {
    
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            ModCommands.COMMANDS
        ); 
	}
    
    @SubscribeEvent
    public static void register(VillagerTradesEvent event) {
        ModVillagers.registerTrades(event.getType(), event.getTrades());
    }
}
