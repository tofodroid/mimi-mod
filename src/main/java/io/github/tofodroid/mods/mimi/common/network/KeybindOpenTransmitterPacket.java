package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.network.FriendlyByteBuf;

public class KeybindOpenTransmitterPacket {
    public static KeybindOpenTransmitterPacket decodePacket(FriendlyByteBuf buf) {
        try {
            if(!buf.readBoolean()) {
                throw new IndexOutOfBoundsException("Invalid zero value data byte for packet.");
            }
            return new KeybindOpenTransmitterPacket();
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("KeybindOpenTransmitterPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(KeybindOpenTransmitterPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(true);
    }
}
