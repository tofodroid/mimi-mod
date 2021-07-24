package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.common.container.ModContainers;
import io.github.tofodroid.mods.mimi.common.data.EntityInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.data.ItemInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.data.ReceiverDataUtil;
import io.github.tofodroid.mods.mimi.common.gui.GuiWrapper;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
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
    public void openReceiverBlockGui(World world, PlayerEntity player, TileReceiver receiver) {
        openGui(world, new GuiReceiverBlock(player, world, receiver, ReceiverDataUtil.INSTANCE));
    }

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
    }
    
    private void openGui(World world, Screen screen) {
        // Only open screen on client thread
        if(world.isRemote) {
            Minecraft.getInstance().displayGuiScreen(screen);
        }
    }
}