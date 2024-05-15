package io.github.tofodroid.mods.mimi.common.network;

import java.util.Optional;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class TransmitterControlPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(MIMIMod.MODID, TransmitterControlPacket.class.getSimpleName().toLowerCase());
    public static final CustomPacketPayload.Type<TransmitterControlPacket> TYPE = new Type<>(ID);

    public enum CONTROL {
        PLAY,
        PAUSE,
        STOP,
        RESTART,
        SEEK,
        NEXT,
        PREV,
        LOOP_M,
        FAVE_M,
        SOURCE_M,
        MARKFAVE,
        SHUFFLE,
        UNKNOWN;

        public static CONTROL fromByte(byte b) {
            try {
                return CONTROL.values()[b];
            } catch(Exception e) {}
            return CONTROL.UNKNOWN;
        }
    };
    
    public final CONTROL control;
    public final Optional<Integer> controlData;
    public final Optional<UUID> songId;
    public final UUID transmitterId;

    public TransmitterControlPacket(UUID transmitterId, CONTROL control, Integer controlData, UUID songId) {
        this.transmitterId = transmitterId;
        this.control = control != null ? control : CONTROL.UNKNOWN;
        this.controlData = Optional.of(controlData);
        this.songId = Optional.of(songId);
    }

    public TransmitterControlPacket(UUID transmitterId, CONTROL control, Optional<Integer> controlData, Optional<UUID> songId) {
        this.transmitterId = transmitterId;
        this.control = control != null ? control : CONTROL.UNKNOWN;
        this.controlData = controlData;
        this.songId = songId;
    }

    public TransmitterControlPacket(UUID transmitterId, CONTROL control, Integer controlData) {
        this.transmitterId = transmitterId;
        this.control = control != null ? control : CONTROL.UNKNOWN;
        this.controlData = Optional.of(controlData);
        this.songId = Optional.empty();
    }

    public TransmitterControlPacket(UUID transmitterId, CONTROL control, UUID songId) {
        this.transmitterId = transmitterId;
        this.control = control != null ? control : CONTROL.UNKNOWN;
        this.controlData = Optional.empty();
        this.songId = Optional.of(songId);
    }

    public TransmitterControlPacket(UUID transmitterId, CONTROL control) {
        this.transmitterId = transmitterId;
        this.control = control != null ? control : CONTROL.UNKNOWN;
        this.controlData = Optional.empty();
        this.songId = Optional.empty();
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
       return TYPE;
    }

    public static UUID readUUID(FriendlyByteBuf buf) {
        return FriendlyByteBuf.readUUID(buf);
    }

    public static void writeUUID(FriendlyByteBuf buf, UUID id) {
        FriendlyByteBuf.writeUUID(buf, id);
    }
    
    public static TransmitterControlPacket decodePacket(FriendlyByteBuf buf) {
        try {
            UUID transmitterId = buf.readUUID();
            byte control = buf.readByte();
            Optional<Integer> controlData = buf.readOptional(FriendlyByteBuf::readInt);
            Optional<UUID> songId = buf.readOptional(TransmitterControlPacket::readUUID);
            return new TransmitterControlPacket(transmitterId, CONTROL.fromByte(control), controlData, songId);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("TransmitterControlPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("TransmitterControlPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(TransmitterControlPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.transmitterId);
        buf.writeByte(Integer.valueOf(pkt.control.ordinal()).byteValue());
        buf.writeOptional(pkt.controlData, FriendlyByteBuf::writeInt);
        buf.writeOptional(pkt.songId, TransmitterControlPacket::writeUUID);
    }
}