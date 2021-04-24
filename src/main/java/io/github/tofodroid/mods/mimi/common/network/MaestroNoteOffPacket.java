package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.network.PacketBuffer;

public class MaestroNoteOffPacket {
    public static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;
    
    public final Byte channel;
    public final Byte note;
    
    public MaestroNoteOffPacket(Byte channel, Byte note) {
        this.channel = channel;
        this.note = note;
    }

    public static MaestroNoteOffPacket decodePacket(PacketBuffer buf) {
        try {
            byte channel = buf.readByte();
            byte note = buf.readByte();
            return new MaestroNoteOffPacket(channel, note);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("SpeakerNoteOffPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(MaestroNoteOffPacket pkt, PacketBuffer buf) {
        buf.writeByte(pkt.channel);
        buf.writeByte(pkt.note);
    }
}
