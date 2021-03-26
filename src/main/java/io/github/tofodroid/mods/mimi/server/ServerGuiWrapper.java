package io.github.tofodroid.mods.mimi.server;

import io.github.tofodroid.mods.mimi.common.gui.GuiWrapper;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ServerGuiWrapper implements GuiWrapper {

    @Override
    public void openInstrumentGui(World world, PlayerEntity player, Byte instrument, ItemStack instrumentItemStack) { }
    
    @Override
    public void openInstrumentGui(World world, PlayerEntity player, Byte instrumentId, TileInstrument instrumentTile) { }
    
    @Override
    public void openConfigGui(World world, PlayerEntity player) { }
}
