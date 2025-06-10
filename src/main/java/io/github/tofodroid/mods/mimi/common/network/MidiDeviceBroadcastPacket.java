package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.api.event.MidiEventType;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.util.ResourceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class MidiDeviceBroadcastPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(MIMIMod.MODID, MidiDeviceBroadcastPacket.class.getSimpleName().toLowerCase());
    public static final CustomPacketPayload.Type<MidiDeviceBroadcastPacket> TYPE = new Type<>(ID);

    public final @Nonnull MidiEventType type;
    public final @Nonnull Byte channel;
    public final @Nonnull Byte note;
    public final @Nonnull Byte velocity;
    public final @Nonnull UUID player;
    public final @Nonnull BlockPos pos;
    public final @Nonnull Long noteServerTime;

    public static MidiDeviceBroadcastPacket fromBroadcastEvent(BroadcastEvent event) {
        return new MidiDeviceBroadcastPacket(event.type, event.channel, event.note, event.velocity, event.senderId, event.pos, event.eventTime);
    }
    
    protected MidiDeviceBroadcastPacket(MidiEventType type, Byte channel, Byte note, Byte velocity, UUID player, BlockPos pos, Long noteServerTime) {
        this.type = type;
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
            MidiEventType type = MidiEventType.fromByte(buf.readByte());
            byte channel = buf.readByte();
            byte note = buf.readByte();
            byte velocity = buf.readByte();
            UUID player = buf.readUUID();
            BlockPos pos = buf.readBlockPos();
            Long noteServerTime = buf.readLong();

            return new MidiDeviceBroadcastPacket(type, channel, note, velocity, player, pos, noteServerTime);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("MidiDeviceBroadcastPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(MidiDeviceBroadcastPacket pkt, FriendlyByteBuf buf) {
        buf.writeByte(pkt.type.toByte());
        buf.writeByte(pkt.channel);
        buf.writeByte(pkt.note);
        buf.writeByte(pkt.velocity);
        buf.writeUUID(pkt.player);
        buf.writeBlockPos(pkt.pos);
        buf.writeLong(pkt.noteServerTime);
    }
}
