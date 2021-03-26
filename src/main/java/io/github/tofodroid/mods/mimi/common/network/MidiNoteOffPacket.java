package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.network.PacketBuffer;

public class MidiNoteOffPacket {
    public static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;

    public final Byte note;
    public final UUID player;
    public final Byte instrumentId;

    public MidiNoteOffPacket(Byte note, Byte instrumentId, UUID player) {
        this.note = note;
        this.instrumentId = instrumentId;
        this.player = player;
    }

    public static MidiNoteOffPacket decodePacket(PacketBuffer buf) {
        try {
            byte note = buf.readByte();
            byte instrumentId = buf.readByte();
            UUID player = buf.readUniqueId();
            return new MidiNoteOffPacket(note, instrumentId, player);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("MidiNoteOffPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(MidiNoteOffPacket pkt, PacketBuffer buf) {
        buf.writeByte(pkt.note);
        buf.writeByte(pkt.instrumentId);
        buf.writeUniqueId(pkt.player);
    }
}
