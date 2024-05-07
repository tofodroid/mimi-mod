package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public class MidiNotePacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(MIMIMod.MODID, MidiNotePacket.class.getSimpleName().toLowerCase());
    private static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;

    public final @Nonnull Byte note;
    public final @Nonnull Byte velocity;
    public final @Nonnull Byte instrumentId;
    public final @Nonnull UUID player;
    public final @Nonnull BlockPos pos;
    public final @Nonnull Long noteServerTime;
    public final @Nullable InteractionHand instrumentHand;
    
    public static MidiNotePacket createControlPacket(Byte controller, Byte value, Byte instrumentId, UUID player, BlockPos pos, InteractionHand instrumentHand) {
        return new MidiNotePacket(Integer.valueOf(-controller).byteValue(), value, instrumentId, player, pos, MIMIMod.getProxy().getCurrentServerMillis(), instrumentHand);
    }
    
    public static MidiNotePacket createAllNotesOffPacket(Byte instrumentId, UUID player, BlockPos pos, InteractionHand instrumentHand) {
        return new MidiNotePacket(ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), instrumentId, player, pos, MIMIMod.getProxy().getCurrentServerMillis(), instrumentHand);
    }

    public static MidiNotePacket createNotePacket(Byte note, Byte velocity, Byte instrumentId, UUID player, BlockPos pos, InteractionHand instrumentHand) {
        return new MidiNotePacket(note, velocity, instrumentId, player, pos, MIMIMod.getProxy().getCurrentServerMillis(), instrumentHand);
    }

    public static MidiNotePacket createControlPacket(Byte controller, Byte value, Byte instrumentId, UUID player, BlockPos pos, Long noteServerTime, InteractionHand instrumentHand) {
        return new MidiNotePacket(Integer.valueOf(-controller).byteValue(), value, instrumentId, player, pos, noteServerTime, instrumentHand);
    }
    
    public static MidiNotePacket createAllNotesOffPacket(Byte instrumentId, UUID player, BlockPos pos, Long noteServerTime, InteractionHand instrumentHand) {
        return new MidiNotePacket(ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), instrumentId, player, pos, noteServerTime, instrumentHand);
    }

    public static MidiNotePacket createNotePacket(Byte note, Byte velocity, Byte instrumentId, UUID player, BlockPos pos, Long noteServerTime, InteractionHand instrumentHand) {
        return new MidiNotePacket(note, velocity, instrumentId, player, pos, noteServerTime, instrumentHand);
    }

    public static MidiNotePacket fromNetMidiEvent(NetMidiEvent event, Long eventTime) {
        return new MidiNotePacket(event.note, event.velocity, event.instrumentId, event.playerId, event.pos, eventTime, null);
    }

    protected MidiNotePacket(Byte note, Byte velocity, Byte instrumentId, UUID player, BlockPos pos, Long noteServerTime, InteractionHand instrumentHand) {
        this.note = note;
        this.velocity = velocity;
        this.instrumentId = instrumentId;
        this.player = player;
        this.pos = pos;
        this.noteServerTime = noteServerTime;
        this.instrumentHand = instrumentHand;
    }

    @Override
    public ResourceLocation id() {
        return MidiNotePacket.ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        MidiNotePacket.encodePacket(this, buf);
    }

    public static MidiNotePacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte note = buf.readByte();
            byte velocity = buf.readByte();
            byte instrumentId = buf.readByte();
            UUID player = buf.readUUID();
            BlockPos pos = buf.readBlockPos();
            Long noteServerTime = buf.readLong();
            Boolean hasHand = buf.readBoolean();
            Boolean instrumentHand = null;

            if(hasHand) {
                instrumentHand = buf.readBoolean();
            }

            return new MidiNotePacket(note, velocity, instrumentId, player, pos, noteServerTime, boolTohand(instrumentHand));
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
        buf.writeLong(pkt.noteServerTime);
        buf.writeBoolean(pkt.instrumentHand != null);

        if(pkt.instrumentHand != null) {
            buf.writeBoolean(handToBool(pkt.instrumentHand));
        }
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

    public static Boolean handToBool(InteractionHand hand) {
        if(hand == null) {
            return null;
        }
        return InteractionHand.MAIN_HAND.equals(hand);
    }

    public static InteractionHand boolTohand(Boolean bool) {
        if(bool == null) {
            return null;
        }
        
        return bool ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }
}
