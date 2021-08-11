package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.common.container.ModContainers;
import io.github.tofodroid.mods.mimi.common.gui.GuiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ClientGuiWrapper implements GuiWrapper {
    @Override
    public void openPlaylistGui(World world, PlayerEntity player) {
        openGui(world, new GuiMidiPlaylist(player));
    }

    @Override
    public void openConfigGui(World world, PlayerEntity player) {
        openGui(world, new GuiMidiInputConfig(player));
    }

    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(ModContainers.RECEIVER, GuiReceiverContainerScreen::new);
        ScreenManager.registerFactory(ModContainers.LISTENER, GuiListenerContainerScreen::new);
        ScreenManager.registerFactory(ModContainers.INSTRUMENT, GuiInstrumentContainerScreen::new);
        ScreenManager.registerFactory(ModContainers.MECHANICALMAESTRO, GuiMechanicalMaestroContainerScreen::new);
    }
    
    private void openGui(World world, Screen screen) {
        // Only open screen on client thread
        if(world.isRemote) {
            Minecraft.getInstance().displayGuiScreen(screen);
        }
    }
}