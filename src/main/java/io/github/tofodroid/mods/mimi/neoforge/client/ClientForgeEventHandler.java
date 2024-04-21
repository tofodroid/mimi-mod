package io.github.tofodroid.mods.mimi.neoforge.client;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.keybind.ModBindings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.InputEvent.Key;
import net.neoforged.neoforge.event.TickEvent.ClientTickEvent;
import net.neoforged.neoforge.event.TickEvent.Phase;
import net.neoforged.neoforge.event.TickEvent.PlayerTickEvent;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventHandler {
    @SubscribeEvent
    public static void onKey(Key event) {
        ModBindings.onKeyInput();
    }

    @SubscribeEvent
    public static void onLoggingIn(LoggingIn event) {
        ((ClientProxy)MIMIMod.getProxy()).onLocalPlayerLogin();
    }
    
    @SubscribeEvent
    public static void onLoggingOut(LoggingOut event) {
        ((ClientProxy)MIMIMod.getProxy()).onLocalPlayerLogout();
    }
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT) {
            return;
        }
        ((ClientProxy)MIMIMod.getProxy()).onClientTick();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT) {
            return;
        }
        ((ClientProxy)MIMIMod.getProxy()).onPlayerTick(event.player);
    }
}
