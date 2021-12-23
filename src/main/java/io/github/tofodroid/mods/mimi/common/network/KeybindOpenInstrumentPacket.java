package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public class KeybindOpenInstrumentPacket {
    public final Boolean handheld;
    public final InteractionHand handIn;

    public KeybindOpenInstrumentPacket(Boolean handheld, InteractionHand handIn) {
        this.handheld = handheld;
        this.handIn = handIn;
    }

    public static KeybindOpenInstrumentPacket decodePacket(FriendlyByteBuf buf) {
        try {
            return new KeybindOpenInstrumentPacket(buf.readBoolean(), buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("KeybindOpenInstrumentPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(KeybindOpenInstrumentPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.handheld);
        buf.writeBoolean(InteractionHand.MAIN_HAND.equals(pkt.handIn));
    }
}
