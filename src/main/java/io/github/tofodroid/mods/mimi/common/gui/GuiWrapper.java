package io.github.tofodroid.mods.mimi.common.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public interface GuiWrapper {
    public void openPlaylistGui(World world, PlayerEntity player);
    public void openConfigGui(World world, PlayerEntity player);
}
