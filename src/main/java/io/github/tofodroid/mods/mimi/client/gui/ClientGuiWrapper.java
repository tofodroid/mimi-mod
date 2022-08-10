package io.github.tofodroid.mods.mimi.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public abstract class ClientGuiWrapper {
    public static void openPlaylistGui(Level world, Player player) {
        openGui(world, new GuiMidiPlaylist(player));
    }

    public static void openConfigGui(Level world, Player player) {
        openGui(world, new GuiMidiInputConfig(player));
    }
    
    public static void openGui(Level world, Screen screen) {
        // Only open screen on client thread
        if(world.isClientSide) {
            Minecraft.getInstance().setScreen(screen);
        }
    }
}