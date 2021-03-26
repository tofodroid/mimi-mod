package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.PacketBuffer;

public class InstrumentItemDataUpdatePacket extends InstrumentDataUpdatePacket {

    public InstrumentItemDataUpdatePacket(UUID maestroId, Integer inputMode, String acceptedChannelString) {
        super(maestroId, inputMode, acceptedChannelString);
    }
    
    public static InstrumentItemDataUpdatePacket decodePacket(PacketBuffer buf) {
        try {
            Integer inputMode = buf.readInt();
            if(inputMode == NULL_INPUT_MODE_VAL) {
                inputMode = null;
            }

            String acceptedChannelString = buf.readString(38);
            if(acceptedChannelString.trim().isEmpty()) {
                acceptedChannelString = null;
            }

            UUID maestroId = buf.readUniqueId();
            if(NULL_MAESTRO_VAL.equals(maestroId)) {
                maestroId = null;
            }

            return new InstrumentItemDataUpdatePacket(maestroId, inputMode, acceptedChannelString);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("InstrumentDataUpdatePacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("InstrumentDataUpdatePacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(InstrumentItemDataUpdatePacket pkt, PacketBuffer buf) {
        buf.writeInt(pkt.inputMode != null ? pkt.inputMode : NULL_INPUT_MODE_VAL);
        buf.writeString(pkt.acceptedChannelString != null ? pkt.acceptedChannelString : "", 38);
        buf.writeUniqueId(pkt.maestroId != null ? pkt.maestroId : NULL_MAESTRO_VAL);
    }
}
