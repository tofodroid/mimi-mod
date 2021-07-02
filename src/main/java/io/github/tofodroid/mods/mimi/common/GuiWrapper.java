package io.github.tofodroid.mods.mimi.common;

import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public interface GuiWrapper {
    public void openInstrumentGui(World world, PlayerEntity player, Byte instrumentId, ItemStack instrumentItemStack, Hand handIn);
    public void openInstrumentGui(World world, PlayerEntity player, Byte instrumentId, TileInstrument instrumentTile);
    public void openPlaylistGui(World world, PlayerEntity player);
    public void openConfigGui(World world, PlayerEntity player);
}
