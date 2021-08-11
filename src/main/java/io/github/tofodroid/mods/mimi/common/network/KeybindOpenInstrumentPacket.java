package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

public class KeybindOpenInstrumentPacket {
    public final Boolean handheld;
    public final Hand handIn;

    public KeybindOpenInstrumentPacket(Boolean handheld, Hand handIn) {
        this.handheld = handheld;
        this.handIn = handIn;
    }

    public static KeybindOpenInstrumentPacket decodePacket(PacketBuffer buf) {
        try {
            return new KeybindOpenInstrumentPacket(buf.readBoolean(), buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("KeybindOpenInstrumentPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(KeybindOpenInstrumentPacket pkt, PacketBuffer buf) {
        buf.writeBoolean(pkt.handheld);
        buf.writeBoolean(Hand.MAIN_HAND.equals(pkt.handIn));
    }
}
