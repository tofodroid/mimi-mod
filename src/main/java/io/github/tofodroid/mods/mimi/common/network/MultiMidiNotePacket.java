package io.github.tofodroid.mods.mimi.common.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.util.ResourceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class MultiMidiNotePacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceUtils.newModLocation(MultiMidiNotePacket.class.getSimpleName().toLowerCase());
    public static final CustomPacketPayload.Type<MultiMidiNotePacket> TYPE = new Type<>(ID);

    private final Map<Long, ArrayList<NetMidiEvent>> sourceMap;
    public final TreeMap<Long, List<MidiNotePacket>> resultPackets;

    public MultiMidiNotePacket(Map<Long, ArrayList<NetMidiEvent>> sourceMap) {
        this.sourceMap = new HashMap<>(sourceMap);
        this.resultPackets = new TreeMap<>();
        
        for(Map.Entry<Long, ArrayList<NetMidiEvent>> sourceEntry : sourceMap.entrySet()) {
            List<MidiNotePacket> packets = new ArrayList<>();

            for(NetMidiEvent event : sourceEntry.getValue()) {
                packets.add(MidiNotePacket.fromNetMidiEvent(event, sourceEntry.getKey()));
            }
            resultPackets.put(sourceEntry.getKey(), packets);
        }
    }

    public MultiMidiNotePacket(TreeMap<Long, List<MidiNotePacket>> packets) {
        this.resultPackets = packets;
        this.sourceMap = Map.of();
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
       return TYPE;
    }

    public static MultiMidiNotePacket decodePacket(FriendlyByteBuf buf) {
        try {
            TreeMap<Long, List<MidiNotePacket>> resultMap = new TreeMap<>();
            // META - Number of Times
            Integer numTimes = buf.readInt();
            
            // Second order
            for(Integer timeIndex = 0; timeIndex < numTimes; timeIndex++) {
                Long noteServerTime = buf.readLong();
                List<MidiNotePacket> timePackets = resultMap.computeIfAbsent(noteServerTime, (time) -> new ArrayList<>());

                // META - Number of Instruments
                Integer numEvents = buf.readInt();

                for(Integer eventIndex = 0; eventIndex < numEvents; eventIndex++) {
                    UUID playerId = buf.readUUID();
                    BlockPos pos = buf.readBlockPos();
                    Byte instrumentId = buf.readByte();
                    Byte note = buf.readByte();
                    Byte velocity = buf.readByte();

                    timePackets.add(new MidiNotePacket(note, velocity, instrumentId, playerId, pos, noteServerTime, null));
                }
            }
            return new MultiMidiNotePacket(resultMap);
        } catch (IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("MultiMidiNotePacket did not contain enough bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(MultiMidiNotePacket pkt, FriendlyByteBuf buf) {
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
                    buf.writeUUID(noteEvent.playerId);
                    buf.writeBlockPos(noteEvent.pos);
                    buf.writeByte(noteEvent.instrumentId);
                    buf.writeByte(noteEvent.note);
                    buf.writeByte(noteEvent.velocity);
                }
            }
        }
    }
}
