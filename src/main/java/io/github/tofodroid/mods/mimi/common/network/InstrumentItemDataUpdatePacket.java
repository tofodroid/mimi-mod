package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.PacketBuffer;

public class InstrumentItemDataUpdatePacket extends InstrumentDataUpdatePacket {

    public InstrumentItemDataUpdatePacket(UUID maestroId, Boolean midiEnabled, String acceptedChannelString) {
        super(maestroId, midiEnabled, acceptedChannelString);
    }
    
    public static InstrumentItemDataUpdatePacket decodePacket(PacketBuffer buf) {
        try {
            Boolean midiEnabled = buf.readBoolean();

            String acceptedChannelString = buf.readString(38);
            if(acceptedChannelString.trim().isEmpty()) {
                acceptedChannelString = null;
            }

            UUID maestroId = buf.readUniqueId();
            if(NULL_MAESTRO_VAL.equals(maestroId)) {
                maestroId = null;
            }

            return new InstrumentItemDataUpdatePacket(maestroId, midiEnabled, acceptedChannelString);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("InstrumentDataUpdatePacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("InstrumentDataUpdatePacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(InstrumentItemDataUpdatePacket pkt, PacketBuffer buf) {
        buf.writeBoolean(pkt.midiEnabled != null ? pkt.midiEnabled : false);
        buf.writeString(pkt.acceptedChannelString != null ? pkt.acceptedChannelString : "", 38);
        buf.writeUniqueId(pkt.maestroId != null ? pkt.maestroId : NULL_MAESTRO_VAL);
    }
}
