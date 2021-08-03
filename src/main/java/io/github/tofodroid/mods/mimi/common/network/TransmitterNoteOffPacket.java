package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNoteOnPacket.TransmitMode;
import net.minecraft.network.PacketBuffer;

public class TransmitterNoteOffPacket {
    public static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;
    
    public final Byte channel;
    public final Byte note;
    public final TransmitMode transmitMode;
    
    public TransmitterNoteOffPacket(Byte channel, Byte note, TransmitMode transmitMode) {
        this.channel = channel;
        this.note = note;
        this.transmitMode = transmitMode;
    }

    public static TransmitterNoteOffPacket decodePacket(PacketBuffer buf) {
        try {
            byte channel = buf.readByte();
            byte note = buf.readByte();
            TransmitMode transmitMode = TransmitMode.values()[new Byte(buf.readByte()).intValue()];
            return new TransmitterNoteOffPacket(channel, note, transmitMode);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("SpeakerNoteOffPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(TransmitterNoteOffPacket pkt, PacketBuffer buf) {
        buf.writeByte(pkt.channel);
        buf.writeByte(pkt.note);
        buf.writeByte(new Integer(pkt.transmitMode.ordinal()).byteValue());
    }
}
