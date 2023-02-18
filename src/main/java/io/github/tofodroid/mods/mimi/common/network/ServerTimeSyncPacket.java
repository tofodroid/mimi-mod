package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.network.FriendlyByteBuf;

public class ServerTimeSyncPacket {
    public final long clientTime;
    public final long offset;

    public ServerTimeSyncPacket(long clientTime, long offset) {
        this.clientTime = clientTime;
        this.offset = offset;
    }

    public static ServerTimeSyncPacket decodePacket(FriendlyByteBuf buf) {
        try {
            Long clientTime = buf.readLong();
            Long offset = buf.readLong();
            return new ServerTimeSyncPacket(clientTime, offset);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerTimeSyncPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerTimeSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeLong(pkt.clientTime);
        buf.writeLong(pkt.offset);
    }
}
