package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public class TransmitterStackUpdatePacket {
    public final TransmitMode transmitMode;

    public TransmitterStackUpdatePacket(TransmitMode mode) {
        this.transmitMode = mode != null ? mode : TransmitMode.SELF;
    }
    
    public static TransmitterStackUpdatePacket decodePacket(FriendlyByteBuf buf) {
        try {
            Integer transmitMode = buf.readInt();
            return new TransmitterStackUpdatePacket(TransmitMode.fromInt(transmitMode));
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("TransmitterStackUpdatePacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("TransmitterStackUpdatePacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(TransmitterStackUpdatePacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.transmitMode.ordinal());
    }
}
