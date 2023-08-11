package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ClientGuiWrapper {
    public static void openInstrumentGui(Level world, Player player, InteractionHand handIn, ItemStack instrumentStack) {
        if(instrumentStack.getItem() instanceof IInstrumentItem) {
            openGui(world, new GuiInstrument(player, instrumentStack, handIn));
        }
    }

    public static void openListenerGui(Level world, BlockPos tilePos, ItemStack listenerStack) {
        if(listenerStack.getItem().equals(ModBlocks.LISTENER.get().asItem())) {
            openGui(world, new GuiListener(tilePos, listenerStack));
        }
    }

    public static void openPlaylistGui(Level world, Player player) {
        openGui(world, new GuiMidiFileCaster(player));
    }

    public static void openConfigGui(Level world, Player player) {
        openGui(world, new GuiMidiInputConfig(player));
    }
    
    public static void openGui(Level world, Screen screen) {
        if(world.isClientSide) {
            Minecraft.getInstance().setScreen(screen);
        }
    }
}