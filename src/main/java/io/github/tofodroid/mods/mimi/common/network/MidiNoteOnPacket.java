package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class MidiNoteOnPacket {
    public static final Byte NO_CHANNEL = Byte.MAX_VALUE;
    
    public final Byte note;
    public final Byte velocity;
    public final Byte instrumentId;
    public final Byte channel;
    public final UUID player;
    public final BlockPos pos;

    public MidiNoteOnPacket(Byte channel, Byte note, Byte velocity, Byte instrumentId, UUID player, BlockPos pos) {
        this.channel = channel;
        this.note = note;
        this.velocity = velocity;
        this.instrumentId = instrumentId;
        this.player = player;
        this.pos = pos;
    }

    public static MidiNoteOnPacket decodePacket(PacketBuffer buf) {
        try {
            byte channel = buf.readByte();
            byte note = buf.readByte();
            byte velocity = buf.readByte();
            byte instrumentId = buf.readByte();
            UUID player = buf.readUniqueId();
            BlockPos pos = buf.readBlockPos();
            return new MidiNoteOnPacket(channel, note, velocity, instrumentId, player, pos);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("MidiNoteOnPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(MidiNoteOnPacket pkt, PacketBuffer buf) {
        buf.writeByte(pkt.channel);
        buf.writeByte(pkt.note);
        buf.writeByte(pkt.velocity);
        buf.writeByte(pkt.instrumentId);
        buf.writeUniqueId(pkt.player);
        buf.writeBlockPos(pkt.pos);
    }
}
