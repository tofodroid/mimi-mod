package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public class ServerMusicPlayerStatusPacket {
    public final Integer songPositionSeconds;
    public final Boolean running;
    public final Boolean complete;

    public static ServerMusicPlayerStatusPacket requestPacket() {
        return new ServerMusicPlayerStatusPacket(null, null, null);
    }
    
    public ServerMusicPlayerStatusPacket(Integer songPositionSeconds, Boolean running, Boolean complete) {
        this.songPositionSeconds = songPositionSeconds != null ? songPositionSeconds : 0;
        this.running = running != null ? running : false;
        this.complete = complete != null ? complete : false;
    }
    
    public static ServerMusicPlayerStatusPacket decodePacket(FriendlyByteBuf buf) {
        try {            
            Integer songPositionSeconds = buf.readInt();
            if(songPositionSeconds < 0) {
                songPositionSeconds = null;
            }

            Boolean running = buf.readBoolean();
            Boolean complete = buf.readBoolean();
            
            return new ServerMusicPlayerStatusPacket(songPositionSeconds, running, complete);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ServerMusicPlayerStatusPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("ServerMusicPlayerStatusPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ServerMusicPlayerStatusPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.songPositionSeconds);
        buf.writeBoolean(pkt.running);
        buf.writeBoolean(pkt.complete);
    }
}
