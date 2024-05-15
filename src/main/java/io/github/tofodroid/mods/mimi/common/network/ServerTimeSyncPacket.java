package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ServerTimeSyncPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(MIMIMod.MODID, ServerTimeSyncPacket.class.getSimpleName().toLowerCase());
    public static final CustomPacketPayload.Type<ServerTimeSyncPacket> TYPE = new Type<>(ID);
    public final long currentServerMilli;
    public final Boolean firstRequest;

    public ServerTimeSyncPacket() {
        this(0l, true);
    }

    public ServerTimeSyncPacket(long currentServerMilli, Boolean firstRequest) {
        this.currentServerMilli = currentServerMilli;
        this.firstRequest = firstRequest;
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
       return TYPE;
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