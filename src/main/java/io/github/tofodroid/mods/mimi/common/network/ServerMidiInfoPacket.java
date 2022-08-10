package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public class ServerMidiInfoPacket {
    public enum STATUS_CODE {
        INFO,
        EMPTY,
        ERROR_URL,
        ERROR_HOST,
        ERROR_DISABLED,
        ERROR_OTHER,
        ERROR_NOT_FOUND,
        UNKNOWN;

        public static STATUS_CODE fromByte(byte b) {
            try {
                return STATUS_CODE.values()[b];
            } catch(Exception e) {}
            return STATUS_CODE.UNKNOWN;
        }
    };

    public final STATUS_CODE status;
    public final byte[] channelMapping;
    public final Integer songLengthSeconds;
    public final Integer songPositionSeconds;
    
    public ServerMidiInfoPacket(STATUS_CODE status, byte[] channelMapping, Integer songLengthSeconds, Integer songPositionSeconds) {
        this.status = status != null ? status : STATUS_CODE.UNKNOWN;
        this.channelMapping = channelMapping != null ? channelMapping : new byte[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        this.songLengthSeconds = songLengthSeconds != null ? songLengthSeconds : -1;
        this.songPositionSeconds = songPositionSeconds != null ? songPositionSeconds : -1;
    }
    
    public static ServerMidiInfoPacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte status = buf.readByte();

            byte[] channelMapping = buf.readByteArray(16);
            if(channelMapping.length == 0) {
                channelMapping = null;
            }
            
            Integer songLengthSeconds = buf.readInt();
            if(songLengthSeconds < 1) {
                songLengthSeconds = null;
            }

            Integer songPositionSeconds = buf.readInt();
            if(songPositionSeconds < 0) {
                songPositionSeconds = null;
            }
            
            return new ServerMidiInfoPacket(STATUS_CODE.fromByte(status), channelMapping, songLengthSeconds, songPositionSeconds);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerMidiInfoPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("ServerMidiInfoPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerMidiInfoPacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(Integer.valueOf(pkt.status.ordinal()).byteValue());
        buf.writeByteArray(pkt.channelMapping);
        buf.writeInt(pkt.songLengthSeconds);
        buf.writeInt(pkt.songPositionSeconds);
    }
}
