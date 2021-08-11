package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.PacketBuffer;

public class SyncItemInstrumentSwitchboardPacket {
    public final Boolean doRemove;

    public SyncItemInstrumentSwitchboardPacket(Boolean doRemove) {
        this.doRemove = doRemove;
    }
    
    public static SyncItemInstrumentSwitchboardPacket decodePacket(PacketBuffer buf) {
        try {
            return new SyncItemInstrumentSwitchboardPacket(buf.readBoolean());
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("SyncItemInstrumentSwitchboardPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("SyncItemInstrumentSwitchboardPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(SyncItemInstrumentSwitchboardPacket pkt, PacketBuffer buf) {
        buf.writeBoolean(pkt.doRemove);
    }
}
