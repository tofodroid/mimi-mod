package io.github.tofodroid.mods.mimi.neoforge.common;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.command.ModCommands;
import io.github.tofodroid.mods.mimi.common.mob.villager.ModVillagers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

@EventBusSubscriber(modid = MIMIMod.MODID, bus = EventBusSubscriber.Bus.GAME)
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
