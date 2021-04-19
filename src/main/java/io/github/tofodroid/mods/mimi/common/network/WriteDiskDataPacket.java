package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.network.PacketBuffer;

public class WriteDiskDataPacket {
    public final String title;
    public final String url;
    
    public WriteDiskDataPacket(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public static WriteDiskDataPacket decodePacket(PacketBuffer buf) {
        try {
            String title = buf.readString(256);
            String url = buf.readString(256);
            return new WriteDiskDataPacket(title, url);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("WriteDiskDataPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(WriteDiskDataPacket pkt, PacketBuffer buf) {
        buf.writeString(pkt.title,256);
        buf.writeString(pkt.url,256);
    }
}
