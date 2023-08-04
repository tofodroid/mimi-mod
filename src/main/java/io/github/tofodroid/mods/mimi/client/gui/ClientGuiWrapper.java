package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentBlock;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentHandheld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ClientGuiWrapper {
    public static void openInstrumentGui(Level world, Player player, InteractionHand handIn) {
        if(handIn != null) {
            ItemStack stack = player.getItemInHand(handIn);

            if(stack.getItem() instanceof ItemInstrumentHandheld) {
                openGui(world, new GuiInstrument(player, stack, handIn));
            }
        } else {
            ItemStack stack = BlockInstrument.getTileInstrumentForEntity(player).getInstrumentStack();

            if(stack.getItem() instanceof ItemInstrumentBlock) {
                openGui(world, new GuiInstrument(player, stack, null));
            }
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