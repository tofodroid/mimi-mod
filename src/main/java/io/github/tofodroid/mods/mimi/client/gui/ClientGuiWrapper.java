package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.common.container.ModContainers;
import io.github.tofodroid.mods.mimi.common.gui.GuiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ClientGuiWrapper implements GuiWrapper {
    @Override
    public void openPlaylistGui(Level world, Player player) {
        openGui(world, new GuiMidiPlaylist(player));
    }

    @Override
    public void openConfigGui(Level world, Player player) {
        openGui(world, new GuiMidiInputConfig(player));
    }

    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        MenuScreens.register(ModContainers.RECEIVER, GuiReceiverContainerScreen::new);
        MenuScreens.register(ModContainers.LISTENER, GuiListenerContainerScreen::new);
        MenuScreens.register(ModContainers.INSTRUMENT, GuiInstrumentContainerScreen::new);
        MenuScreens.register(ModContainers.MECHANICALMAESTRO, GuiMechanicalMaestroContainerScreen::new);
        MenuScreens.register(ModContainers.CONDUCTOR, GuiConductorContainerScreen::new);
        MenuScreens.register(ModContainers.TUNINGTABLE, GuiTuningTableContainerScreen::new);
    }
    
    private void openGui(Level world, Screen screen) {
        // Only open screen on client thread
        if(world.isClientSide) {
            Minecraft.getInstance().setScreen(screen);
        }
    }
}