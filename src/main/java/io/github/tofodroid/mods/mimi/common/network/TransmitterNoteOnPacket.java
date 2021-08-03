package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.network.PacketBuffer;

public class TransmitterNoteOnPacket {
    public final Byte channel;
    public final Byte note;
    public final Byte velocity;
    public final TransmitMode transmitMode;
    
    public static enum TransmitMode {
        PUBLIC,
        LINKED,
        SELF;
    }
    
    public TransmitterNoteOnPacket(Byte channel, Byte note, Byte velocity, TransmitMode transmitMode) {
        this.channel = channel;
        this.note = note;
        this.velocity = velocity;
        this.transmitMode = transmitMode;
    }

    public static TransmitterNoteOnPacket decodePacket(PacketBuffer buf) {
        try {
            byte channel = buf.readByte();
            byte note = buf.readByte();
            byte velocity = buf.readByte();
            TransmitMode transmitMode = TransmitMode.values()[new Byte(buf.readByte()).intValue()];
            return new TransmitterNoteOnPacket(channel, note, velocity, transmitMode);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("SpeakerNoteOnPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(TransmitterNoteOnPacket pkt, PacketBuffer buf) {
        buf.writeByte(pkt.channel);
        buf.writeByte(pkt.note);
        buf.writeByte(pkt.velocity);
        buf.writeByte(new Integer(pkt.transmitMode.ordinal()).byteValue());
    }
}
