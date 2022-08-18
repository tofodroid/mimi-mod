package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiStatus.STATUS_CODE;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public class ServerMidiInfoPacket {
    public final STATUS_CODE status;
    public final String midiUrl;
    public final byte[] channelMapping;
    public final Integer songLengthSeconds;

    public ServerMidiInfoPacket(STATUS_CODE status, String midiUrl, byte[] channelMapping, Integer songLengthSeconds) {
        this.status = status != null ? status : STATUS_CODE.UNKNOWN;
        this.midiUrl = midiUrl != null ? midiUrl : "";
        this.channelMapping = channelMapping != null ? channelMapping : new byte[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        this.songLengthSeconds = songLengthSeconds != null ? songLengthSeconds : -1;
    }

    public ServerMidiInfoPacket(STATUS_CODE status, byte[] channelMapping, Integer songLengthSeconds) {
        this(status, null, channelMapping, songLengthSeconds);
    }

    public ServerMidiInfoPacket(String midiUrl) {
        this(null, midiUrl, null, null);
    }

    public Boolean isValid() {
        return STATUS_CODE.SUCCESS.equals(status);
    }
    
    public static ServerMidiInfoPacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte status = buf.readByte();

            byte[] channelMapping = buf.readByteArray(16);
            if(channelMapping.length == 0) {
                channelMapping = null;
            }

            Integer songLengthSeconds = buf.readInt();

            String midiUrl = buf.readUtf(256);
            if(midiUrl.trim().isEmpty()) {
                midiUrl = null;
            }
            return new ServerMidiInfoPacket(STATUS_CODE.fromByte(status), midiUrl, channelMapping, songLengthSeconds);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerValidateRemoteMidiPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("ServerValidateRemoteMidiPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerMidiInfoPacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(Integer.valueOf(pkt.status.ordinal()).byteValue());
        buf.writeByteArray(pkt.channelMapping);
        buf.writeInt(pkt.songLengthSeconds);
        buf.writeUtf(pkt.midiUrl, 256);
    }
}
