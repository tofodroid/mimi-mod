package io.github.tofodroid.mods.mimi.server;

import io.github.tofodroid.mods.mimi.common.GuiWrapper;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ServerGuiWrapper implements GuiWrapper {

    @Override
    public void openInstrumentGui(World world, PlayerEntity player, Byte instrument, ItemStack instrumentItemStack, Hand handIn) { }
    
    @Override
    public void openInstrumentGui(World world, PlayerEntity player, Byte instrumentId, TileInstrument instrumentTile) { }
    
    @Override
    public void openPlaylistGui(World world, PlayerEntity player) { }

    @Override
    public void openConfigGui(World world, PlayerEntity player) { }
}
