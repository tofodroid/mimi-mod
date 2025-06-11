package io.github.tofodroid.mods.mimi.util;

import io.github.tofodroid.mods.mimi.common.item.ItemTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class EntityUtils {
    public static Boolean playerHasActiveTransmitter(Player player) {
        // Mainhand
        if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ItemTransmitter) {
            return true;
        }

        // Offhand
        if(player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ItemTransmitter) {
            return true;
        }

        // Hotbar
        for(int i = 0; i < 9; i++) {
            if(player.getInventory().getItem(i).getItem() instanceof ItemTransmitter) {
                return true;
            }
        }
        
        return false;
    }

    public static BlockPos getEntityHeadPos(LivingEntity entity) {
        return new BlockPos(entity.getX(), entity.isPassenger() ? (entity.getVehicle().getPassengersRidingOffset() + entity.getEyeY()) : entity.getEyeY(), entity.getZ());
    }
}
