package io.github.tofodroid.mods.mimi.client;

import io.github.tofodroid.mods.mimi.client.gui.GuiInstrument;
import io.github.tofodroid.mods.mimi.client.gui.GuiMidiInputConfig;
import io.github.tofodroid.mods.mimi.client.gui.GuiTransmitter;
import io.github.tofodroid.mods.mimi.common.GuiWrapper;
import io.github.tofodroid.mods.mimi.common.instruments.EntityInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.instruments.ItemInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ClientGuiWrapper implements GuiWrapper {

    @Override
    public void openInstrumentGui(World world, PlayerEntity player, Byte instrumentId, ItemStack instrumentItemStack) {
        openGui(world, new GuiInstrument<ItemStack>(player, world, instrumentId, instrumentItemStack, ItemInstrumentDataUtil.INSTANCE));
    }

    @Override
    public void openInstrumentGui(World world, PlayerEntity player, Byte instrumentId, TileInstrument instrumentTile) {
        openGui(world, new GuiInstrument<TileInstrument>(player, world, instrumentId, instrumentTile, EntityInstrumentDataUtil.INSTANCE));
    }

    @Override
    public void openConfigGui(World world, PlayerEntity player) {
        openGui(world, new GuiMidiInputConfig(player));
    }
    
    @Override
    public void openTransmitterGui(World world, PlayerEntity player, ItemStack transmitterStack) { 
        openGui(world, new GuiTransmitter(player, transmitterStack));
    }

    private void openGui(World world, Screen screen) {
        // Only open screen on client thread
        if(world.isRemote) {
            Minecraft.getInstance().displayGuiScreen(screen);
        }
    }
}