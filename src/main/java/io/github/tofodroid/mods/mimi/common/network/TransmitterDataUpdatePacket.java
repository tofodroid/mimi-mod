package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.network.PacketBuffer;

public class TransmitterDataUpdatePacket {
    public final Boolean enabled;
    public final Boolean mode;
    
    public TransmitterDataUpdatePacket(Boolean enabled, Boolean mode) {
        this.enabled = enabled;
        this.mode = mode;
    }

    public static TransmitterDataUpdatePacket decodePacket(PacketBuffer buf) {
        try {
            Boolean enabled = buf.readBoolean();
            Boolean mode = buf.readBoolean();
            return new TransmitterDataUpdatePacket(enabled, mode);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("TransmitterDataUpdatePacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(TransmitterDataUpdatePacket pkt, PacketBuffer buf) {
        buf.writeBoolean(pkt.enabled);
        buf.writeBoolean(pkt.mode);
    }
}
