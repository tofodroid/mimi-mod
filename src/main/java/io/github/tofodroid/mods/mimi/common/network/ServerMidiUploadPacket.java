package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ServerMidiUploadPacket {
    public static final int MAX_DATA_SIZE = 30000;
    public static final Byte UPLOAD_SUCCESS = Integer.valueOf(0).byteValue();
    public static final Byte UPLOAD_RESEND = Integer.valueOf(1).byteValue();
    public static final Byte UPLOAD_FAIL = Integer.valueOf(2).byteValue();

    private Boolean retry = false;
    private ServerPlayer sender = null;
    public final UUID fileId;
    public final Byte part;
    public final Byte totalParts;
    public final byte[] data;

    public ServerMidiUploadPacket(UUID fileId, byte[] missingParts) {
        this(fileId, UPLOAD_RESEND, UPLOAD_RESEND, missingParts);
    }

    public ServerMidiUploadPacket(UUID fileId) {
        this(fileId, Integer.valueOf(0).byteValue(), Integer.valueOf(0).byteValue(), new byte[]{});
    }

    public ServerMidiUploadPacket(Byte totalParts, Byte part, byte[] data) {
        this(new UUID(0,0), totalParts, part, data);
    }

    public Byte getResponseStatus() {
        return this.totalParts;
    }

    public ServerPlayer getSender() {
        return this.sender;
    }

    public ServerMidiUploadPacket withSender(ServerPlayer sender) {
        this.sender = sender;
        return this;
    }

    private ServerMidiUploadPacket withRetry(Boolean retry) {
        this.retry = retry;
        return this;
    }

    public ServerMidiUploadPacket markRetry() {
        return this.withRetry(true);
    }

    public Boolean isRetry() {
        return this.retry;
    }

    public ServerMidiUploadPacket(UUID fileId, Byte totalParts, Byte part, byte[] data) {
        this.fileId = fileId;
        this.part = part;
        this.totalParts = totalParts;

        if(data.length > MAX_DATA_SIZE) {
            MIMIMod.LOGGER.error("ServerMidiUploadPacket data contained too many bytes!");
            this.data = new byte[]{};
        } else {
            this.data = data;
        }
    }
    
    public static ServerMidiUploadPacket decodePacket(FriendlyByteBuf buf) {
        try {
            Boolean retry = buf.readBoolean();
            UUID fileId = buf.readUUID();
            Byte totalParts = buf.readByte();
            Byte part = buf.readByte();
            byte[] data = buf.readByteArray(MAX_DATA_SIZE);

            return new ServerMidiUploadPacket(fileId, totalParts, part, data).withRetry(retry);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerMidiUploadPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("ServerMidiUploadPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerMidiUploadPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.retry);
        buf.writeUUID(pkt.fileId);
        buf.writeByte(pkt.totalParts);
        buf.writeByte(pkt.part);
        buf.writeByteArray(pkt.data);
    }
}
