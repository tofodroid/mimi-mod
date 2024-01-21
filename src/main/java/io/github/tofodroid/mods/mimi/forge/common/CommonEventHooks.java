package io.github.tofodroid.mods.mimi.forge.common;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

public class CommonEventHooks {
    public static void firePlayerCraftingEvent(Player player, ItemStack crafted, Container craftMatrix) {
        ForgeEventFactory.firePlayerCraftingEvent(player, crafted, craftMatrix);
    }

    public static void setCraftingPlayer(Player player) {
        ForgeHooks.setCraftingPlayer(player);
    }
}
