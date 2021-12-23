package io.github.tofodroid.mods.mimi.common.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface GuiWrapper {
    public void openPlaylistGui(Level world, Player player);
    public void openConfigGui(Level world, Player player);
}
