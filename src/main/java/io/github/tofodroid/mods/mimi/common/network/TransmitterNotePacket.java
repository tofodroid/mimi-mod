package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.network.FriendlyByteBuf;

public class TransmitterNotePacket {
    public static final Byte NO_CHANNEL = Byte.MAX_VALUE;
    public static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;
    
    public final Byte channel;
    public final Byte note;
    public final Byte velocity;
    public final TransmitMode transmitMode;
    
    public static enum TransmitMode {
        PUBLIC,
        LINKED,
        SELF;
    }
    
    public TransmitterNotePacket(Byte channel, Byte note, Byte velocity, TransmitMode transmitMode) {
        this.channel = channel;
        this.note = note;
        this.velocity = velocity;
        this.transmitMode = transmitMode;
    }

    public static TransmitterNotePacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte channel = buf.readByte();
            byte note = buf.readByte();
            byte velocity = buf.readByte();
            TransmitMode transmitMode = TransmitMode.values()[Byte.valueOf(buf.readByte()).intValue()];
            return new TransmitterNotePacket(channel, note, velocity, transmitMode);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("SpeakerNoteOnPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(TransmitterNotePacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(pkt.channel);
        buf.writeByte(pkt.note);
        buf.writeByte(pkt.velocity);
        buf.writeByte(Integer.valueOf(pkt.transmitMode.ordinal()).byteValue());
    }
}
