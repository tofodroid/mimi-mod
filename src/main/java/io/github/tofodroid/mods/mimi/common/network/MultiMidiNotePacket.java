package io.github.tofodroid.mods.mimi.common.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public class MultiMidiNotePacket {
    public final @Nonnull Map<UUID, List<MidiNotePacket>> packets;
    public final Byte note;
    public final Long noteServerTime;
    public final Boolean noteOff;

    public MultiMidiNotePacket(Map<UUID, List<MidiNotePacket>> packets, Byte note, Long noteServerTime, Boolean noteOff) {
        this.packets = packets;
        this.note = note;
        this.noteServerTime = noteServerTime;
        this.noteOff = noteOff;
    }

    public static MultiMidiNotePacket decodePacket(FriendlyByteBuf buf) {
        try {
            Map<UUID, List<MidiNotePacket>> packets = new HashMap<>();
            byte numPlayers = buf.readByte();
            byte note = buf.readByte();
            Long noteServerTime = buf.readLong();
            Boolean noteOff = buf.readBoolean();
            
            for(int i = 0; i < numPlayers; i++) {
                List<MidiNotePacket> playerPackets = new ArrayList<>();
                byte numNotesForPlayer = buf.readByte();
                UUID player = buf.readUUID();
                BlockPos pos = buf.readBlockPos();

                for(int j = 0; j < numNotesForPlayer; j++) {
                    byte velocity = buf.readByte();
                    byte instrumentId = buf.readByte();
                    Boolean hasHand = buf.readBoolean();
                    Boolean instrumentHand = null;
        
                    if(hasHand) {
                        instrumentHand = buf.readBoolean();
                    }
    
                    playerPackets.add(new MidiNotePacket(note, velocity, instrumentId, player, pos, noteServerTime, MidiNotePacket.boolTohand(instrumentHand)));
                }

                packets.put(player, playerPackets);
            }

            return new MultiMidiNotePacket(packets, note, noteServerTime, noteOff);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("MultiMidiNotePacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(MultiMidiNotePacket pkt, FriendlyByteBuf buf) {
        if(!pkt.packets.isEmpty()) {
            buf.writeByte(pkt.packets.size());

            Boolean first = true;
            for(Map.Entry<UUID, List<MidiNotePacket>> entry : pkt.packets.entrySet()) {
                if(first) {
                    buf.writeByte(pkt.note);
                    buf.writeLong(pkt.noteServerTime);
                    buf.writeBoolean(pkt.noteOff);
                }

                buf.writeByte(entry.getValue().size());
                buf.writeUUID(entry.getValue().get(0).player);
                buf.writeBlockPos(entry.getValue().get(0).pos);

                for(MidiNotePacket packet : entry.getValue()) {
                    buf.writeByte(packet.velocity);
                    buf.writeByte(packet.instrumentId);

                    InteractionHand hand = packet.instrumentHand;

                    if(hand != null) {
                        buf.writeBoolean(true);
                        buf.writeBoolean(InteractionHand.MAIN_HAND.equals(hand));
                    }
                    buf.writeBoolean(false);
                }
            }
        }
    }
}
