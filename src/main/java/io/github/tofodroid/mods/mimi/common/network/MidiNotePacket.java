package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class MidiNotePacket {
    public static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;
    
    public final Byte note;
    public final Byte velocity;
    public final Byte instrumentId;
    public final UUID player;
    public final Boolean mechanical;
    public final BlockPos pos;

    public MidiNotePacket(Byte note, Byte velocity, Byte instrumentId, UUID player, Boolean mechanical, BlockPos pos) {
        this.note = note;
        this.velocity = velocity;
        this.instrumentId = instrumentId;
        this.player = player;
        this.mechanical = mechanical;
        this.pos = pos;
    }

    public static MidiNotePacket decodePacket(FriendlyByteBuf buf) {
        try {
            byte note = buf.readByte();
            byte velocity = buf.readByte();
            byte instrumentId = buf.readByte();
            UUID player = buf.readUUID();
            Boolean mechanical = buf.readBoolean();
            BlockPos pos = buf.readBlockPos();
            return new MidiNotePacket(note, velocity, instrumentId, player, mechanical, pos);
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
        buf.writeBoolean(pkt.mechanical);
        buf.writeBlockPos(pkt.pos);
    }
}
