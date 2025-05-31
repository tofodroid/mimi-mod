package io.github.tofodroid.mods.mimi.util;

import net.minecraft.world.InteractionHand;

public abstract class NetworkUtils {
    public static Byte encodeHand(InteractionHand handIn) {
        if(handIn == InteractionHand.MAIN_HAND) {
            return 0;
        } else if(handIn == InteractionHand.OFF_HAND) {
            return 1;
        }
        return 2;
    }

    public static InteractionHand decodeHand(Byte byteIn) {
        if(byteIn == 0) {
            return InteractionHand.MAIN_HAND;
        } else if(byteIn == 1) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }
}
