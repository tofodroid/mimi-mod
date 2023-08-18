package io.github.tofodroid.mods.mimi.common.network;

import java.util.Optional;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public class TransmitterControlPacket {
    public enum CONTROL {
        PLAY,
        PAUSE,
        STOP,
        SEEK,
        SELECT,
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

    public TransmitterControlPacket(CONTROL control, Integer data) {
        this.control = control != null ? control : CONTROL.UNKNOWN;
        this.data = Optional.of(data);
    }

    public TransmitterControlPacket(CONTROL control, Optional<Integer> data) {
        this.control = control != null ? control : CONTROL.UNKNOWN;
        this.data = data;
    }

    public TransmitterControlPacket(CONTROL control) {
        this.control = control != null ? control : CONTROL.UNKNOWN;
        this.data = Optional.empty();
    }
    
    public static TransmitterControlPacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte control = buf.readByte();
            Optional<Integer> data = buf.readOptional(FriendlyByteBuf::readInt);
            return new TransmitterControlPacket(CONTROL.fromByte(control), data);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("TransmitterControlPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("TransmitterControlPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(TransmitterControlPacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(Integer.valueOf(pkt.control.ordinal()).byteValue());
        buf.writeOptional(pkt.data, FriendlyByteBuf::writeInt);
    }
}