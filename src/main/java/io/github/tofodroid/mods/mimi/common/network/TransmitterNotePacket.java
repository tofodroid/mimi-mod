package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.network.FriendlyByteBuf;

public class TransmitterNotePacket {
    public static final Byte ALL_CHANNELS = Byte.MAX_VALUE;
    private static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;
    
    public final Byte channel;
    public final Byte note;
    public final Byte velocity;
    public final TransmitMode transmitMode;
    public final Long noteServerTime;
    
    public static enum TransmitMode {
        PUBLIC,
        LINKED,
        SELF;

        public static TransmitMode fromInt(Integer val) {
            return val >=0 && TransmitMode.values().length > val ? TransmitMode.values()[val] : null;
        }
    }

    public static TransmitterNotePacket createNotePacket(Byte channel, Byte note, Byte velocity, TransmitMode transmitMode) {
        return new TransmitterNotePacket(channel, note, Integer.valueOf(0).byteValue(), transmitMode, MIMIMod.proxy.getServerTime());
    }

    public static TransmitterNotePacket createAllNotesOffPacket(Byte channel, TransmitMode transmitMode) {
        return new TransmitterNotePacket(channel, ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), transmitMode, MIMIMod.proxy.getServerTime());
    }
    
    public static TransmitterNotePacket createControllerPacket(Byte channel, Byte controller, Byte value, TransmitMode transmitMode) {
        return new TransmitterNotePacket(channel, Integer.valueOf(-controller).byteValue(), value, transmitMode, MIMIMod.proxy.getServerTime());
    }

    private TransmitterNotePacket(Byte channel, Byte note, Byte velocity, TransmitMode transmitMode, Long noteServerTime) {
        this.channel = channel;
        this.note = note;
        this.velocity = velocity;
        this.transmitMode = transmitMode;
        this.noteServerTime = noteServerTime;
    }

    public static TransmitterNotePacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte channel = buf.readByte();
            byte note = buf.readByte();
            byte velocity = buf.readByte();
            TransmitMode transmitMode = TransmitMode.values()[Byte.valueOf(buf.readByte()).intValue()];
            Long noteServerTime = buf.readLong();
            return new TransmitterNotePacket(channel, note, velocity, transmitMode, noteServerTime);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("TransmitterNotePacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(TransmitterNotePacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(pkt.channel);
        buf.writeByte(pkt.note);
        buf.writeByte(pkt.velocity);
        buf.writeByte(Integer.valueOf(pkt.transmitMode.ordinal()).byteValue());
        buf.writeLong(pkt.noteServerTime);
    }

    public Boolean isAllNotesOffPacket() {
        return this.note == ALL_NOTES_OFF;
    }

    public Boolean isControlPacket() {
        return this.note < 0 && !isAllNotesOffPacket();
    }

    public Byte getControllerNumber() {
        return isControlPacket() ? Integer.valueOf(-this.note).byteValue() : null;
    }

    public Byte getControllerValue() {
        return isControlPacket() ? this.velocity : null;
    }
}
