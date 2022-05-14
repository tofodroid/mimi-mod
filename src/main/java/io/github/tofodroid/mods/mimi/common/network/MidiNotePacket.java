package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class MidiNotePacket {
    private static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;

    public final Byte note;
    public final Byte velocity;
    public final Byte instrumentId;
    public final UUID player;
    public final BlockPos pos;

    public static MidiNotePacket createAllNotesOffPacket(Byte instrumentId, UUID player, BlockPos pos) {
        return new MidiNotePacket(ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), instrumentId, player, pos);
    }

    public static MidiNotePacket createControlPacket(Byte controller, Byte value, Byte instrumentId, UUID player, BlockPos pos) {
        return new MidiNotePacket(Integer.valueOf(-controller).byteValue(), value, instrumentId, player, pos);
    }
    
    public MidiNotePacket(Byte note, Byte velocity, Byte instrumentId, UUID player, BlockPos pos) {
        this.note = note;
        this.velocity = velocity;
        this.instrumentId = instrumentId;
        this.player = player;
        this.pos = pos;
    }

    public static MidiNotePacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte note = buf.readByte();
            byte velocity = buf.readByte();
            byte instrumentId = buf.readByte();
            UUID player = buf.readUUID();
            BlockPos pos = buf.readBlockPos();
            return new MidiNotePacket(note, velocity, instrumentId, player, pos);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("MidiNoteOnPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(MidiNotePacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(pkt.note);
        buf.writeByte(pkt.velocity);
        buf.writeByte(pkt.instrumentId);
        buf.writeUUID(pkt.player);
        buf.writeBlockPos(pkt.pos);
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
