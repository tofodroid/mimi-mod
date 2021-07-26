package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ReceiverTileDataUpdatePacket {
    public static final UUID NULL_SOURCE_VALUE = new UUID(0,0);

    public final String acceptedChannelString;
    public final UUID midiSource;
    public final BlockPos tilePos;
    public final String filterNoteString;

    public ReceiverTileDataUpdatePacket(BlockPos tilePos, UUID midiSource, String acceptedChannelString, String filterNoteString) {
        this.acceptedChannelString = acceptedChannelString;
        this.midiSource = midiSource;
        this.tilePos = tilePos;
        this.filterNoteString = filterNoteString;
    }
    
    public static ReceiverTileDataUpdatePacket decodePacket(PacketBuffer buf) {
        try {
            BlockPos tilePos = BlockPos.fromLong(buf.readLong());

            String acceptedChannelString = buf.readString(38);
            if(acceptedChannelString.trim().isEmpty()) {
                acceptedChannelString = null;
            }

            UUID midiSource = buf.readUniqueId();
            if(NULL_SOURCE_VALUE.equals(midiSource)) {
                midiSource = null;
            }

            String filterNoteString = buf.readString(47);
            if(filterNoteString.trim().isEmpty()) {
                filterNoteString = null;
            }

            return new ReceiverTileDataUpdatePacket(tilePos, midiSource, acceptedChannelString, filterNoteString);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ReceiverTileDataUpdatePacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("ReceiverTileDataUpdatePacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ReceiverTileDataUpdatePacket pkt, PacketBuffer buf) {
        buf.writeLong(pkt.tilePos.toLong());
        buf.writeString(pkt.acceptedChannelString != null ? pkt.acceptedChannelString : "", 38);
        buf.writeUniqueId(pkt.midiSource != null ? pkt.midiSource : NULL_SOURCE_VALUE);
        buf.writeString(pkt.filterNoteString != null ? pkt.filterNoteString : "", 47);
    }
}
