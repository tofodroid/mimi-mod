package io.github.tofodroid.mods.mimi.common.network;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.network.PacketBuffer;

public class MaestroNoteOnPacket {
    public final Byte channel;
    public final Byte note;
    public final Byte velocity;
    
    public MaestroNoteOnPacket(Byte channel, Byte note, Byte velocity) {
        this.channel = channel;
        this.note = note;
        this.velocity = velocity;
    }

    public static MaestroNoteOnPacket decodePacket(PacketBuffer buf) {
        try {
            byte channel = buf.readByte();
            byte note = buf.readByte();
            byte velocity = buf.readByte();
            return new MaestroNoteOnPacket(channel, note, velocity);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("SpeakerNoteOnPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(MaestroNoteOnPacket pkt, PacketBuffer buf) {
        buf.writeByte(pkt.channel);
        buf.writeByte(pkt.note);
        buf.writeByte(pkt.velocity);
    }
}
