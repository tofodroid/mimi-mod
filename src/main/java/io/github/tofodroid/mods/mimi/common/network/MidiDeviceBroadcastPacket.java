package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class MidiDeviceBroadcastPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(MIMIMod.MODID, MidiDeviceBroadcastPacket.class.getSimpleName().toLowerCase());
    public static final CustomPacketPayload.Type<MidiDeviceBroadcastPacket> TYPE = new Type<>(ID);
    private static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;

    public final @Nonnull Byte channel;
    public final @Nonnull Byte note;
    public final @Nonnull Byte velocity;
    public final @Nonnull UUID player;
    public final @Nonnull BlockPos pos;
    public final @Nonnull Long noteServerTime;
    
    public static MidiDeviceBroadcastPacket createControlPacket(Byte channel, Byte controller, Byte value, UUID player, BlockPos pos) {
        return new MidiDeviceBroadcastPacket(channel, Integer.valueOf(-controller).byteValue(), value, player, pos, MIMIMod.getProxy().getCurrentServerMillis());
    }
    
    public static MidiDeviceBroadcastPacket createAllNotesOffPacket(Byte channel, UUID player, BlockPos pos) {
        return new MidiDeviceBroadcastPacket(channel, ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), player, pos, MIMIMod.getProxy().getCurrentServerMillis());
    }

    public static MidiDeviceBroadcastPacket createNotePacket(Byte channel, Byte note, Byte velocity, UUID player, BlockPos pos) {
        return new MidiDeviceBroadcastPacket(channel, note, velocity, player, pos, MIMIMod.getProxy().getCurrentServerMillis());
    }

    public static MidiDeviceBroadcastPacket createControlPacket(Byte channel, Byte controller, Byte value, UUID player, BlockPos pos, Long noteServerTime) {
        return new MidiDeviceBroadcastPacket(channel, Integer.valueOf(-controller).byteValue(), value, player, pos, noteServerTime);
    }
    
    public static MidiDeviceBroadcastPacket createAllNotesOffPacket(Byte channel, UUID player, BlockPos pos, Long noteServerTime) {
        return new MidiDeviceBroadcastPacket(channel, ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), player, pos, noteServerTime);
    }

    public static MidiDeviceBroadcastPacket createNotePacket(Byte channel, Byte note, Byte velocity, UUID player, BlockPos pos, Long noteServerTime) {
        return new MidiDeviceBroadcastPacket(channel, note, velocity, player, pos, noteServerTime);
    }

    protected MidiDeviceBroadcastPacket(Byte channel, Byte note, Byte velocity, UUID player, BlockPos pos, Long noteServerTime) {
        this.channel = channel;
        this.note = note;
        this.velocity = velocity;
        this.player = player;
        this.pos = pos;
        this.noteServerTime = noteServerTime;
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
       return TYPE;
    }

    public static MidiDeviceBroadcastPacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte channel = buf.readByte();
            byte note = buf.readByte();
            byte velocity = buf.readByte();
            UUID player = buf.readUUID();
            BlockPos pos = buf.readBlockPos();
            Long noteServerTime = buf.readLong();

            return new MidiDeviceBroadcastPacket(channel, note, velocity, player, pos, noteServerTime);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("MidiNoteOnPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(MidiDeviceBroadcastPacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(pkt.channel);
        buf.writeByte(pkt.note);
        buf.writeByte(pkt.velocity);
        buf.writeUUID(pkt.player);
        buf.writeBlockPos(pkt.pos);
        buf.writeLong(pkt.noteServerTime);
    }

    public Boolean isAllNotesOffPacket() {
        return this.note == ALL_NOTES_OFF;
    }

    public Boolean isControlPacket() {
        return this.note < 0 && this.note != ALL_NOTES_OFF;
    }

    public Boolean isNoteOnPacket() {
        return this.note >= 0 && this.velocity > 0;
    }

    public Boolean isNoteOffPacket() {
        return this.note >= 0  && this.velocity <= 0;
    }

    public Byte getControllerNumber() {
        return isControlPacket() ? Integer.valueOf(-this.note).byteValue() : null;
    }

    public Byte getControllerValue() {
        return isControlPacket() ? this.velocity : null;
    }
}
