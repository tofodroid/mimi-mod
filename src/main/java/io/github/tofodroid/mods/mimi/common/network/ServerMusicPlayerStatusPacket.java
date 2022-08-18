package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiStatus.STATUS_CODE;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public class ServerMusicPlayerStatusPacket {
    public final STATUS_CODE status;
    public final Integer songPositionSeconds;
    public final Boolean running;

    public static ServerMusicPlayerStatusPacket requestPacket() {
        return new ServerMusicPlayerStatusPacket(null,null,null);
    }
    
    public ServerMusicPlayerStatusPacket(STATUS_CODE status, Integer songPositionSeconds, Boolean running) {
        this.status = status != null ? status : STATUS_CODE.UNKNOWN;
        this.songPositionSeconds = songPositionSeconds != null ? songPositionSeconds : 0;
        this.running = running != null ? running : false;
    }
    
    public static ServerMusicPlayerStatusPacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte status = buf.readByte();
            
            Integer songPositionSeconds = buf.readInt();
            if(songPositionSeconds < 0) {
                songPositionSeconds = null;
            }

            Boolean running = buf.readBoolean();
            
            return new ServerMusicPlayerStatusPacket(STATUS_CODE.fromByte(status), songPositionSeconds, running);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerMidiInfoPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("ServerMidiInfoPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerMusicPlayerStatusPacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(Integer.valueOf(pkt.status.ordinal()).byteValue());
        buf.writeInt(pkt.songPositionSeconds);
        buf.writeBoolean(pkt.running);
    }
    
    public Boolean isValid() {
        return STATUS_CODE.SUCCESS.equals(status);
    }
}
