package io.github.tofodroid.mods.mimi.client.gui;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class ClientGuiWrapper {
    public static void openConductorGui(Level world, BlockPos tilePos, ItemStack conductorStack) {
        if(conductorStack.getItem().equals(ModBlocks.CONDUCTOR.asItem())) {
            openGui(world, new GuiConductor(tilePos, conductorStack));
        }
    }

    public static void openTransmitterGui(Level world, Player player) {
        openGui(world, new GuiTransmitterItem(player.getUUID()));
    }

    public static void openTransmitterBlockGui(Level world, UUID transmitterTileId) {
        openGui(world, new GuiTransmitterBlock(transmitterTileId));
    }

    public static void openConfigGui(Level world, Player player) {
        openGui(world, new GuiDeviceConfig(player));
    }

    // Configurable Block GUIs
    public static void openEffectEmitterGui(Level world, Player player, BlockPos tilePos, InteractionHand handIn, ItemStack emitterStack) {
        if(emitterStack.getItem().equals(ModBlocks.EFFECTEMITTER.asItem()) || emitterStack.getItem().equals(ModItems.SETTINGSSYNC)) {
            openGui(world, new GuiEffectEmitter(world, tilePos, handIn, emitterStack));
        }
    }

    public static void openRelayGui(Level world, Player player, BlockPos tilePos, InteractionHand handIn, ItemStack relayStack) {
        if(relayStack.getItem().equals(ModBlocks.RELAY.asItem()) || relayStack.getItem().equals(ModItems.SETTINGSSYNC)) {
            openGui(world, new GuiRelay(player, tilePos, handIn, relayStack));
        }
    }

    public static void openListenerGui(Level world, Player player, BlockPos tilePos, InteractionHand handIn, ItemStack listenerStack) {
        if(listenerStack.getItem().equals(ModBlocks.LISTENER.asItem()) || listenerStack.getItem().equals(ModItems.SETTINGSSYNC)) {
            openGui(world, new GuiListener(tilePos, handIn, listenerStack));
        }
    }

    public static void openReceiverGui(Level world, Player player, BlockPos tilePos, InteractionHand handIn, ItemStack receiverStack) {
        if(receiverStack.getItem().equals(ModBlocks.RECEIVER.asItem()) || receiverStack.getItem().equals(ModItems.SETTINGSSYNC)) {
            openGui(world, new GuiReceiver(player, tilePos, handIn, receiverStack));
        }
    }

    public static void openInstrumentGui(Level world, Player player, BlockPos tilePos, InteractionHand handIn, ItemStack instrumentStack) {
        Boolean settingsItem = instrumentStack.getItem().equals(ModItems.SETTINGSSYNC);
        if(instrumentStack.getItem() instanceof IInstrumentItem || settingsItem) {
            openGui(world, new GuiInstrument(player, instrumentStack, handIn, settingsItem));
        }
    }
    
    public static void openGui(Level world, Screen screen) {
        if(world.isClientSide) {
            Minecraft.getInstance().setScreen(screen);
        }
    }
}