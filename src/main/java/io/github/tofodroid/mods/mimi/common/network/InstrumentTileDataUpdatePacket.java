package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class InstrumentTileDataUpdatePacket extends InstrumentDataUpdatePacket {
    public final BlockPos tilePos;

    public InstrumentTileDataUpdatePacket(BlockPos tilePos, UUID maestroId, String acceptedChannelString) {
        super(maestroId, acceptedChannelString);
        this.tilePos = tilePos;
    }
    
    public static InstrumentTileDataUpdatePacket decodePacket(PacketBuffer buf) {
        try {
            BlockPos tilePos = BlockPos.fromLong(buf.readLong());

            String acceptedChannelString = buf.readString(38);
            if(acceptedChannelString.trim().isEmpty()) {
                acceptedChannelString = null;
            }

            UUID maestroId = buf.readUniqueId();
            if(NULL_MAESTRO_VAL.equals(maestroId)) {
                maestroId = null;
            }

            return new InstrumentTileDataUpdatePacket(tilePos, maestroId, acceptedChannelString);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("InstrumentTileUpdatePacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("InstrumentTileUpdatePacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(InstrumentTileDataUpdatePacket pkt, PacketBuffer buf) {
        buf.writeLong(pkt.tilePos.toLong());
        buf.writeString(pkt.acceptedChannelString != null ? pkt.acceptedChannelString : "", 38);
        buf.writeUniqueId(pkt.maestroId != null ? pkt.maestroId : NULL_MAESTRO_VAL);
    }
}
