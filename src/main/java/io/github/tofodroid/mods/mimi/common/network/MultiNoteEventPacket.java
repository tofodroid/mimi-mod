package io.github.tofodroid.mods.mimi.common.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.api.event.MidiEventType;
import io.github.tofodroid.mods.mimi.util.ByteUtils;
import io.github.tofodroid.mods.mimi.util.NetworkUtils;
import io.github.tofodroid.mods.mimi.util.ResourceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public class MultiNoteEventPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceUtils.newModLocation(MultiNoteEventPacket.class.getSimpleName().toLowerCase());
    private final Map<Long, ArrayList<NetMidiEvent>> sourceMap;
    public final TreeMap<Long, List<NoteEventPacket>> resultPackets;

    @Override
    public ResourceLocation id() {
        return MultiNoteEventPacket.ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        MultiNoteEventPacket.encodePacket(this, buf);
    }

    public MultiNoteEventPacket(Map<Long, ArrayList<NetMidiEvent>> sourceMap) {
        this.sourceMap = new HashMap<>(sourceMap);
        this.resultPackets = new TreeMap<>();
        
        for(Map.Entry<Long, ArrayList<NetMidiEvent>> sourceEntry : sourceMap.entrySet()) {
            List<NoteEventPacket> packets = new ArrayList<>();

            for(NetMidiEvent event : sourceEntry.getValue()) {
                packets.add(NoteEventPacket.fromNetMidiEvent(event, sourceEntry.getKey()));
            }
            resultPackets.put(sourceEntry.getKey(), packets);
        }
    }

    public MultiNoteEventPacket(TreeMap<Long, List<NoteEventPacket>> packets) {
        this.resultPackets = packets;
        this.sourceMap = Map.of();
    }

    public static MultiNoteEventPacket decodePacket(FriendlyByteBuf buf) {
        try {
            TreeMap<Long, List<NoteEventPacket>> resultMap = new TreeMap<>();
            // META - Number of Times
            Integer numTimes = buf.readInt();
            
            // Second order
            for(Integer timeIndex = 0; timeIndex < numTimes; timeIndex++) {
                Long noteServerTime = buf.readLong();
                List<NoteEventPacket> timePackets = resultMap.computeIfAbsent(noteServerTime, (time) -> new ArrayList<>());

                // META - Number of Instruments
                Integer numEvents = buf.readInt();

                for(Integer eventIndex = 0; eventIndex < numEvents; eventIndex++) {
                    MidiEventType type = MidiEventType.fromByte(buf.readByte());
                    Byte data1 = ByteUtils.ZERO;
                    Byte data2 = ByteUtils.ZERO;

                    if(type != MidiEventType.RESET) {
                        data1 = buf.readByte();
                    }

                    if(type != MidiEventType.RESET || type != MidiEventType.NOTE_OFF) {
                        data2 = buf.readByte();
                    }

                    UUID playerId = buf.readUUID();
                    BlockPos pos = buf.readBlockPos();
                    Byte instrumentId = buf.readByte();
                    InteractionHand instrumentHand = NetworkUtils.decodeHand(buf.readByte());

                    timePackets.add(new NoteEventPacket(type, data1, data2, instrumentId, playerId, pos, noteServerTime, instrumentHand));
                }
            }
            return new MultiNoteEventPacket(resultMap);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("MultiNoteEventPacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(MultiNoteEventPacket pkt, FriendlyByteBuf buf) {
        // META - Number of Times
        buf.writeInt(pkt.resultPackets.size());

        // Second order
        for(Map.Entry<Long, ArrayList<NetMidiEvent>> timeEntry : pkt.sourceMap.entrySet()) {
            if(timeEntry.getValue() != null && !timeEntry.getValue().isEmpty()) {
                buf.writeLong(timeEntry.getKey());

                // META - Number of Events
                buf.writeInt(timeEntry.getValue().size());

                // Third Order
                for(NetMidiEvent noteEvent : timeEntry.getValue()) {
                    buf.writeByte(noteEvent.type.toByte());

                    if(noteEvent.type != MidiEventType.RESET) {
                        buf.writeByte(noteEvent.note);
                    }

                    if(noteEvent.type != MidiEventType.RESET || noteEvent.type != MidiEventType.NOTE_OFF) {
                        buf.writeByte(noteEvent.velocity);
                    }

                    buf.writeUUID(noteEvent.playerId);
                    buf.writeBlockPos(noteEvent.pos);
                    buf.writeByte(noteEvent.instrumentId);
                    buf.writeByte(NetworkUtils.encodeHand(noteEvent.instrumentHand));
                }
            }
        }
    }
}
