package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.network.PacketBuffer;

public class SpeakerNoteOnPacket {
    public final Byte channel;
    public final Byte note;
    public final Byte velocity;
    public final UUID maestro;
    
    public SpeakerNoteOnPacket(Byte channel, Byte note, Byte velocity, UUID maestro) {
        this.channel = channel;
        this.note = note;
        this.velocity = velocity;
        this.maestro = maestro;
    }

    public static SpeakerNoteOnPacket decodePacket(PacketBuffer buf) {
        try {
            byte channel = buf.readByte();
            byte note = buf.readByte();
            byte velocity = buf.readByte();
            UUID maestro = buf.readUniqueId();
            return new SpeakerNoteOnPacket(channel, note, velocity, maestro);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("SpeakerNoteOnPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(SpeakerNoteOnPacket pkt, PacketBuffer buf) {
        buf.writeByte(pkt.channel);
        buf.writeByte(pkt.note);
        buf.writeByte(pkt.velocity);
        buf.writeUniqueId(pkt.maestro);
    }
}
