package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public class WriteDiskPacket {
    public final String midiUrl;
    public final String diskTitle;

    public WriteDiskPacket(String midiUrl, String diskTitle) {
        this.midiUrl = midiUrl != null ? midiUrl : "";
        this.diskTitle = diskTitle != null ? diskTitle : "";
    }
    
    public static WriteDiskPacket decodePacket(FriendlyByteBuf buf) {
        try {
            String midiUrl = buf.readUtf(256);
            if(midiUrl.trim().isEmpty()) {
                midiUrl = null;
            }
            
            String diskTitle = buf.readUtf(64);
            if(diskTitle.trim().isEmpty()) {
                diskTitle = null;
            }
            
            return new WriteDiskPacket(midiUrl, diskTitle);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("WriteDiskPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("WriteDiskPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(WriteDiskPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.midiUrl, 256);
        buf.writeUtf(pkt.diskTitle, 64);
    }
}
