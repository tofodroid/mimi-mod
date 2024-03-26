package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import net.minecraft.core.BlockPos;

public class NetMidiEvent {
    public final UUID playerId;
    public final BlockPos pos;
    public final Byte instrumentId;
    public final Byte note;
    public final Byte velocity;

    public NetMidiEvent(UUID playerId, BlockPos pos, Byte instrumentId, Byte note, Byte velocity) {
        this.playerId = playerId;
        this.pos = pos;
        this.instrumentId = instrumentId;
        this.note = note;
        this.velocity = velocity;
    }

    public NetMidiEvent(MidiNotePacket packet) {
        this.playerId = packet.player;
        this.pos = packet.pos;
        this.instrumentId = packet.instrumentId;
        this.note = packet.note;
        this.velocity = packet.velocity;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null || !(other instanceof NetMidiEvent)) return false;
        return other == this || (
            ((NetMidiEvent)other).instrumentId == this.instrumentId &&
            ((NetMidiEvent)other).note == this.note &&
            ((NetMidiEvent)other).velocity == this.velocity
        );
    }

    @Override
    public int hashCode() {
        return 7 * (17*this.instrumentId + 31*this.note + 53*this.velocity);
    }
}