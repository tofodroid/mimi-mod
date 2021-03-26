package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.network.PacketBuffer;

public class SpeakerNoteOffPacket {
    public static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;
    
    public final Byte channel;
    public final Byte note;
    public final UUID maestro;
    
    public SpeakerNoteOffPacket(Byte channel, Byte note, UUID maestro) {
        this.channel = channel;
        this.note = note;
        this.maestro = maestro;
    }

    public static SpeakerNoteOffPacket decodePacket(PacketBuffer buf) {
        try {
            byte channel = buf.readByte();
            byte note = buf.readByte();
            UUID maestro = buf.readUniqueId();
            return new SpeakerNoteOffPacket(channel, note, maestro);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("SpeakerNoteOffPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(SpeakerNoteOffPacket pkt, PacketBuffer buf) {
        buf.writeByte(pkt.channel);
        buf.writeByte(pkt.note);
        buf.writeUniqueId(pkt.maestro);
    }
}
