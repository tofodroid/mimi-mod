package io.github.tofodroid.mods.mimi.common.network;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.network.FriendlyByteBuf;

public class TransmitterNotePacket {
    public static final Byte ALL_CHANNELS = Byte.MAX_VALUE;
    private static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;
    
    public final @Nonnull Byte channel;
    public final @Nonnull Byte note;
    public final @Nonnull Byte velocity;
    public final @Nonnull Long noteServerTime;
    
    public static TransmitterNotePacket createNotePacket(Byte channel, Byte note, Byte velocity) {
        return new TransmitterNotePacket(channel, note, velocity, MIMIMod.proxy.getCurrentServerMillis());
    }

    public static TransmitterNotePacket createAllNotesOffPacket(Byte channel) {
        return new TransmitterNotePacket(channel, ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), MIMIMod.proxy.getCurrentServerMillis());
    }
    
    public static TransmitterNotePacket createControllerPacket(Byte channel, Byte controller, Byte value) {
        return new TransmitterNotePacket(channel, Integer.valueOf(-controller).byteValue(), value, MIMIMod.proxy.getCurrentServerMillis());
    }

    @SuppressWarnings("null")
    private TransmitterNotePacket(Byte channel, Byte note, Byte velocity, Long noteServerTime) {
        this.channel = channel;
        this.note = note;
        this.velocity = velocity;
        this.noteServerTime = noteServerTime;
    }

    public static TransmitterNotePacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte channel = buf.readByte();
            byte note = buf.readByte();
            byte velocity = buf.readByte();
            Long noteServerTime = buf.readLong();
            return new TransmitterNotePacket(channel, note, velocity, noteServerTime);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("TransmitterNotePacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(TransmitterNotePacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(pkt.channel);
        buf.writeByte(pkt.note);
        buf.writeByte(pkt.velocity);
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
