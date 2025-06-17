package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.api.event.MidiEventType;
import io.github.tofodroid.mods.mimi.common.api.event.note.NoteEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;

public class NetMidiEvent {
    public final MidiEventType type;
    public final UUID playerId;
    public final BlockPos pos;
    public final Byte instrumentId;
    public final Byte channel;
    public final Byte note;
    public final Byte velocity;
    public final InteractionHand instrumentHand;

    public NetMidiEvent(MidiEventType type, UUID playerId, BlockPos pos, Byte instrumentId, Byte channel, Byte note, Byte velocity, InteractionHand instrumentHand) {
        this.type = type;
        this.playerId = playerId;
        this.pos = pos;
        this.instrumentId = instrumentId;
        this.channel = channel;
        this.note = note;
        this.velocity = velocity;
        this.instrumentHand = instrumentHand;
    }

    public NetMidiEvent(NoteEvent event) {
        this.type = event.type;
        this.playerId = event.senderId;
        this.pos = event.pos;
        this.instrumentId = event.instrumentId;
        this.channel = event.channel;
        this.note = event.note;
        this.velocity = event.velocity;
        this.instrumentHand = event.handIn;
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
        return 7 * (17*this.instrumentId + 31*this.note + 53*this.velocity + 3 * this.type.ordinal());
    }
}