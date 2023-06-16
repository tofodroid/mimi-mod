package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.network.FriendlyByteBuf;

public class ServerTimeSyncPacket {
    public final long timestamp;
    public final Boolean isRequest;

    public ServerTimeSyncPacket(long timestamp, Boolean isRequest) {
        this.timestamp = timestamp;
        this.isRequest = isRequest;
    }

    public static ServerTimeSyncPacket decodePacket(FriendlyByteBuf buf) {
        try {
            Long timestamp = buf.readLong();
            Boolean isRequest = buf.readBoolean();
            return new ServerTimeSyncPacket(timestamp, isRequest);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerTimeSyncPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerTimeSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeLong(pkt.timestamp);
        buf.writeBoolean(pkt.isRequest);
    }
}