package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

public class ActiveTransmitterIdPacket {
    public static final UUID NONE_ID = new UUID(0,0);
    public UUID activeTransmitterId;

    public ActiveTransmitterIdPacket(UUID id) {
        this.activeTransmitterId = id;
    }
    
    public static ActiveTransmitterIdPacket decodePacket(FriendlyByteBuf buf) {
        try {
            UUID id = buf.readUUID();

            if(NONE_ID.equals(id)) {
                id = null;
            }

            return new ActiveTransmitterIdPacket(id);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ActiveTransmitterIdPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("ActiveTransmitterIdPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(ActiveTransmitterIdPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.activeTransmitterId != null ? pkt.activeTransmitterId : NONE_ID);
    }
}
