package io.github.tofodroid.mods.mimi.common;

import io.github.tofodroid.mods.mimi.neoforge.common.CommonEventHooks;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CommonEventHooksProxy {
    public static void firePlayerCraftingEvent(Player player, ItemStack crafted, Container craftMatrix) {
        CommonEventHooks.firePlayerCraftingEvent(player, crafted, craftMatrix);
    }

    public static void setCraftingPlayer(Player player) {
        CommonEventHooks.setCraftingPlayer(player);
    }
}
