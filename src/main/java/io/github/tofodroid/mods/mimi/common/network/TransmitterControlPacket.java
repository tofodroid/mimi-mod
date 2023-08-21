package io.github.tofodroid.mods.mimi.common.network;

import java.util.Optional;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public class TransmitterControlPacket {
    public enum CONTROL {
        PLAY,
        PAUSE,
        STOP,
        SEEK,
        UNKNOWN;

        public static CONTROL fromByte(byte b) {
            try {
                return CONTROL.values()[b];
            } catch(Exception e) {}
            return CONTROL.UNKNOWN;
        }
    };
    
    public final CONTROL control;
    public final Optional<Integer> data;
    public final UUID transmitterId;

    public TransmitterControlPacket(UUID transmitterId, CONTROL control, Integer data) {
        this.transmitterId = transmitterId;
        this.control = control != null ? control : CONTROL.UNKNOWN;
        this.data = Optional.of(data);
    }

    public TransmitterControlPacket(UUID transmitterId, CONTROL control, Optional<Integer> data) {
        this.transmitterId = transmitterId;
        this.control = control != null ? control : CONTROL.UNKNOWN;
        this.data = data;
    }

    public TransmitterControlPacket(UUID transmitterId, CONTROL control) {
        this.transmitterId = transmitterId;
        this.control = control != null ? control : CONTROL.UNKNOWN;
        this.data = Optional.empty();
    }
    
    public static TransmitterControlPacket decodePacket(FriendlyByteBuf buf) {
        try {
            UUID transmitterId = buf.readUUID();
            byte control = buf.readByte();
            Optional<Integer> data = buf.readOptional(FriendlyByteBuf::readInt);
            return new TransmitterControlPacket(transmitterId, CONTROL.fromByte(control), data);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("TransmitterControlPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("TransmitterControlPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    @SuppressWarnings("null")
    public static void encodePacket(TransmitterControlPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.transmitterId);
        buf.writeByte(Integer.valueOf(pkt.control.ordinal()).byteValue());
        buf.writeOptional(pkt.data, FriendlyByteBuf::writeInt);
    }
}