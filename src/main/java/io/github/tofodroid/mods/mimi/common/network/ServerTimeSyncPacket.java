package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.network.FriendlyByteBuf;

public class ServerTimeSyncPacket {
    public final long currentServerMilli;
    public final Boolean firstRequest;

    public ServerTimeSyncPacket() {
        this(0l, true);
    }

    public ServerTimeSyncPacket(long currentServerMilli, Boolean firstRequest) {
        this.currentServerMilli = currentServerMilli;
        this.firstRequest = firstRequest;
    }

    public static ServerTimeSyncPacket decodePacket(FriendlyByteBuf buf) {
        try {
            Long currentServerMilli = buf.readLong();
            Boolean firstRequest = buf.readBoolean();
            return new ServerTimeSyncPacket(currentServerMilli, firstRequest);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerTimeSyncPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerTimeSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeLong(pkt.currentServerMilli);
        buf.writeBoolean(pkt.firstRequest);
    }
}
