package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiInfoPacket.STATUS_CODE;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public class ServerValidateRemoteMidiPacket {
    public STATUS_CODE status;

    public ServerValidateRemoteMidiPacket(STATUS_CODE status) {
        this.status = status != null ? status : STATUS_CODE.UNKNOWN;
    }
    
    public static ServerValidateRemoteMidiPacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte status = buf.readByte();            
            return new ServerValidateRemoteMidiPacket(STATUS_CODE.fromByte(status));
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerValidateRemoteMidiPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("ServerValidateRemoteMidiPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerValidateRemoteMidiPacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(Integer.valueOf(pkt.status.ordinal()).byteValue());
    }
}
