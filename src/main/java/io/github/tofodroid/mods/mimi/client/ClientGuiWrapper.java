package io.github.tofodroid.mods.mimi.client;

import io.github.tofodroid.mods.mimi.client.gui.GuiInstrument;
import io.github.tofodroid.mods.mimi.client.gui.GuiMidiInputConfig;
import io.github.tofodroid.mods.mimi.client.gui.GuiMidiPlaylist;
import io.github.tofodroid.mods.mimi.common.GuiWrapper;
import io.github.tofodroid.mods.mimi.common.instruments.EntityInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.instruments.ItemInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ClientGuiWrapper implements GuiWrapper {

    @Override
    public void openInstrumentGui(World world, PlayerEntity player, Byte instrumentId, ItemStack instrumentItemStack, Hand handIn) {
        openGui(world, new GuiInstrument<ItemStack>(player, world, instrumentId, instrumentItemStack, ItemInstrumentDataUtil.INSTANCE, handIn));
    }

    @Override
    public void openInstrumentGui(World world, PlayerEntity player, Byte instrumentId, TileInstrument instrumentTile) {
        openGui(world, new GuiInstrument<TileInstrument>(player, world, instrumentId, instrumentTile, EntityInstrumentDataUtil.INSTANCE, null));
    }

    @Override
    public void openPlaylistGui(World world, PlayerEntity player) {
        openGui(world, new GuiMidiPlaylist(player));
    }

    @Override
    public void openConfigGui(World world, PlayerEntity player) {
        openGui(world, new GuiMidiInputConfig(player));
    }
    
    private void openGui(World world, Screen screen) {
        // Only open screen on client thread
        if(world.isRemote) {
            Minecraft.getInstance().displayGuiScreen(screen);
        }
    }
}