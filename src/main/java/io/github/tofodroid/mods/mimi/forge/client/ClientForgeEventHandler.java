package io.github.tofodroid.mods.mimi.forge.client;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.keybind.ModBindings;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventHandler {
    @SubscribeEvent
    public static void onKey(Key event) {
        ModBindings.onKeyInput(event);
    }
    
    @SubscribeEvent
    public static void onLoggingOut(LoggingOut event) {
        ((ClientProxy)MIMIMod.proxy).getMidiData().handleLoginLogout();   
        ((ClientProxy)MIMIMod.proxy).getMidiSynth().handleLogout();    
    }

    @SubscribeEvent
    public static void onLoggingIn(LoggingIn event) {
        ((ClientProxy)MIMIMod.proxy).getMidiData().handleLoginLogout();
        ((ClientProxy)MIMIMod.proxy).getMidiSynth().handleLogin();
    }
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT) {
            return;
        }
        ((ClientProxy)MIMIMod.proxy).getMidiData().handleClientTick();
        ((ClientProxy)MIMIMod.proxy).getMidiSynth().handleClientTick();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT) {
            return;
        }
        ((ClientProxy)MIMIMod.proxy).getMidiData().handlePlayerTick(event.player);
    }
}
