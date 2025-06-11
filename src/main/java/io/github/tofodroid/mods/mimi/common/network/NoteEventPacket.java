package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.api.event.MidiEventType;
import io.github.tofodroid.mods.mimi.common.api.event.note.NoteEvent;
import io.github.tofodroid.mods.mimi.util.ByteUtils;
import io.github.tofodroid.mods.mimi.util.NetworkUtils;
import io.github.tofodroid.mods.mimi.util.ResourceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;

public class NoteEventPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceUtils.newModLocation(NoteEventPacket.class.getSimpleName().toLowerCase());

    @Override
    public ResourceLocation id() {
        return NoteEventPacket.ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        NoteEventPacket.encodePacket(this, buf);
    }

    public final @Nonnull MidiEventType type;
    public final @Nonnull Byte data1;
    public final @Nonnull Byte data2;
    public final @Nonnull Long noteServerTime;
    public final @Nonnull UUID player;
    public final @Nonnull BlockPos pos;
    public final @Nonnull Byte instrumentId;
    public final @Nullable InteractionHand instrumentHand;
    
    public static NoteEventPacket createControlPacket(Byte controller, Byte value, Byte instrumentId, UUID player, BlockPos pos, InteractionHand instrumentHand) {
        return new NoteEventPacket(MidiEventType.CONTROL, controller, value, instrumentId, player, pos, MIMIMod.getProxy().getCurrentServerMillis(), instrumentHand);
    }

    public static NoteEventPacket createControlPacket(Byte controller, Byte value, Byte instrumentId, UUID player, BlockPos pos, Long noteServerTime, InteractionHand instrumentHand) {
        return new NoteEventPacket(MidiEventType.CONTROL, controller, value, instrumentId, player, pos, noteServerTime, instrumentHand);
    }
    
    public static NoteEventPacket createResetPacket(Byte instrumentId, UUID player, BlockPos pos, InteractionHand instrumentHand) {
        return new NoteEventPacket(MidiEventType.RESET, ByteUtils.ZERO, ByteUtils.ZERO, instrumentId, player, pos, MIMIMod.getProxy().getCurrentServerMillis(), instrumentHand);
    }

    public static NoteEventPacket createResetPacket(Byte instrumentId, UUID player, BlockPos pos, Long noteServerTime, InteractionHand instrumentHand) {
        return new NoteEventPacket(MidiEventType.RESET, ByteUtils.ZERO, ByteUtils.ZERO, instrumentId, player, pos, noteServerTime, instrumentHand);
    }

    public static NoteEventPacket createNotePacket(Byte note, Byte velocity, Byte instrumentId, UUID player, BlockPos pos, Long noteServerTime, InteractionHand instrumentHand) {
        return new NoteEventPacket(velocity == 0 ? MidiEventType.NOTE_OFF : MidiEventType.NOTE_ON, note, velocity, instrumentId, player, pos, noteServerTime, instrumentHand);
    }

    public static NoteEventPacket createNotePacket(Byte note, Byte velocity, Byte instrumentId, UUID player, BlockPos pos, InteractionHand instrumentHand) {
        return new NoteEventPacket(velocity == 0 ? MidiEventType.NOTE_OFF : MidiEventType.NOTE_ON, note, velocity, instrumentId, player, pos, MIMIMod.getProxy().getCurrentServerMillis(), instrumentHand);
    }

    public static NoteEventPacket fromNoteEvent(NoteEvent event) {
        return new NoteEventPacket(event.type, event.note, event.velocity, event.instrumentId, event.senderId, event.pos, event.eventTime, event.handIn);
    }

    public static NoteEventPacket fromNetMidiEvent(NetMidiEvent event, Long eventTime) {
        return new NoteEventPacket(event.type, event.note, event.velocity, event.instrumentId, event.playerId, event.pos, eventTime, event.instrumentHand);
    }

    public NoteEvent toNoteEvent(Boolean clientSource, UUID senderId, ServerLevel sourceLevel) {
        return new NoteEvent(type, clientSource, instrumentId, instrumentHand, data1, data2, senderId, sourceLevel.dimension(), pos, noteServerTime);
    }

    protected NoteEventPacket(MidiEventType type, Byte data1, Byte data2, Byte instrumentId, UUID player, BlockPos pos, Long noteServerTime, InteractionHand instrumentHand) {
        this.type = type;
        this.data1 = data1;
        this.data2 = data2;
        this.instrumentId = instrumentId;
        this.player = player;
        this.pos = pos;
        this.noteServerTime = noteServerTime;
        this.instrumentHand = instrumentHand;
    }
    
    public static NoteEventPacket decodePacket(FriendlyByteBuf buf) {
        try {
            MidiEventType type = MidiEventType.fromByte(buf.readByte());
            byte data1 = ByteUtils.ZERO;
            byte data2 = ByteUtils.ZERO;

            if(type != MidiEventType.RESET) {
                data1 = buf.readByte();
            }

            if(type != MidiEventType.RESET || type != MidiEventType.NOTE_OFF) {
                data2 = buf.readByte();
            }

            byte instrumentId = buf.readByte();
            UUID player = buf.readUUID();
            BlockPos pos = buf.readBlockPos();
            Long noteServerTime = buf.readLong();
            InteractionHand instrumentHand = NetworkUtils.decodeHand(buf.readByte());

            return new NoteEventPacket(type, data1, data2, instrumentId, player, pos, noteServerTime, instrumentHand);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("MidiNoteOnPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(NoteEventPacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(pkt.type.toByte());

        if(pkt.type != MidiEventType.RESET) {
            buf.writeByte(pkt.data1);
        }

        if(pkt.type != MidiEventType.RESET || pkt.type != MidiEventType.NOTE_OFF) {
            buf.writeByte(pkt.data2);
        }

        buf.writeByte(pkt.instrumentId);
        buf.writeUUID(pkt.player);
        buf.writeBlockPos(pkt.pos);
        buf.writeLong(pkt.noteServerTime);
        buf.writeByte(NetworkUtils.encodeHand(pkt.instrumentHand));
    }
}
