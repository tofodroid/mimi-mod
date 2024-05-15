package io.github.tofodroid.mods.mimi.neoforge.client;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.keybind.ModBindings;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.InputEvent.Key;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = MIMIMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
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
    public static void onClientTick(ClientTickEvent.Post event) {
        ((ClientProxy)MIMIMod.getProxy()).onClientTick();
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer) {
            return;
        }
        ((ClientProxy)MIMIMod.getProxy()).onPlayerTick(event.getEntity());
    }
}
