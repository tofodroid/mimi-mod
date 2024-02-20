package io.github.tofodroid.mods.mimi.client.gui;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class ClientGuiWrapper {
    public static void openInstrumentGui(Level world, Player player, InteractionHand handIn, ItemStack instrumentStack) {
        if(instrumentStack.getItem() instanceof IInstrumentItem) {
            openGui(world, new GuiInstrument(player, instrumentStack, handIn));
        }
    }

    public static void openListenerGui(Level world, BlockPos tilePos, ItemStack listenerStack) {
        if(listenerStack.getItem().equals(ModBlocks.LISTENER.asItem())) {
            openGui(world, new GuiListener(tilePos, listenerStack));
        }
    }

    public static void openReceiverGui(Level world, Player player, BlockPos tilePos, ItemStack receiverStack) {
        if(receiverStack.getItem().equals(ModBlocks.RECEIVER.asItem())) {
            openGui(world, new GuiReceiver(player, tilePos, receiverStack));
        }
    }

    public static void openConductorGui(Level world, BlockPos tilePos, ItemStack conductorStack) {
        if(conductorStack.getItem().equals(ModBlocks.CONDUCTOR.asItem())) {
            openGui(world, new GuiConductor(tilePos, conductorStack));
        }
    }

    public static void openEnderTransmitterGui(Level world, Player player) {
        openGui(world, new GuiEnderTransmitter(player.getUUID()));
    }

    public static void openTransmitterGui(Level world, UUID transmitterTileId) {
        openGui(world, new GuiTransmitterBlock(transmitterTileId));
    }

    public static void openConfigGui(Level world, Player player) {
        openGui(world, new GuiMidiInputConfig(player));
    }

    public static void openEffectEmitterGui(Level world, BlockPos tilePos, ItemStack emitterStack) {
        openGui(world, new GuiEffectEmitter(world, tilePos, emitterStack));
    }
    
    public static void openGui(Level world, Screen screen) {
        if(world.isClientSide) {
            Minecraft.getInstance().setScreen(screen);
        }
    }
}