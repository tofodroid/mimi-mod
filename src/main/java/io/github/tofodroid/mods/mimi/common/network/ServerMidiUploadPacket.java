package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ServerMidiUploadPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(MIMIMod.MODID, ServerMidiUploadPacket.class.getSimpleName().toLowerCase());
    public static final CustomPacketPayload.Type<ServerMidiUploadPacket> TYPE = new Type<>(ID);
    public static final int MAX_DATA_SIZE = 30000;
    public static final Byte UPLOAD_SUCCESS = Integer.valueOf(0).byteValue();
    public static final Byte UPLOAD_RESEND = Integer.valueOf(1).byteValue();
    public static final Byte UPLOAD_FAIL = Integer.valueOf(2).byteValue();

    private Boolean failed = false;
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

    public ServerMidiUploadPacket markFailed() {
        this.failed = true;
        return this;
    }

    public Boolean failed() {
        return this.failed;
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
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
       return TYPE;
    }
    
    public static ServerMidiUploadPacket decodePacket(FriendlyByteBuf buf) {
        try {
            Boolean failed = buf.readBoolean();
            UUID fileId = buf.readUUID();

            if(!failed) {
                Byte totalParts = buf.readByte();
                Byte part = buf.readByte();
                byte[] data = buf.readByteArray(MAX_DATA_SIZE);
                return new ServerMidiUploadPacket(fileId, totalParts, part, data);
            }

            return new ServerMidiUploadPacket(fileId).markFailed();
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerMidiUploadPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("ServerMidiUploadPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerMidiUploadPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.failed);
        buf.writeUUID(pkt.fileId);

        if(!pkt.failed) {
            buf.writeByte(pkt.totalParts);
            buf.writeByte(pkt.part);
            buf.writeByteArray(pkt.data);
        }
    }
}
