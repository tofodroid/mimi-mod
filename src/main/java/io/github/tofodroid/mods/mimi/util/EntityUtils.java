package io.github.tofodroid.mods.mimi.util;

import io.github.tofodroid.mods.mimi.common.item.ItemEnderTransmitter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public abstract class EntityUtils {
    public static Boolean playerHasActiveTransmitter(Player player) {
        // Mainhand
        if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemEnderTransmitter) {
            return true;
        }

        // Offhand
        if(player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ItemEnderTransmitter) {
            return true;
        }

        // Hotbar
        for(int i = 0; i < 9; i++) {
            if(player.getInventory().getItem(i).getItem() instanceof ItemEnderTransmitter) {
                return true;
            }
        }
        
        return false;
    }
}
