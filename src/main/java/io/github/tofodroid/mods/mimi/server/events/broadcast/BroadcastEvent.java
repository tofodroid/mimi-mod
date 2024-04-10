package io.github.tofodroid.mods.mimi.server.events.broadcast;

import java.util.UUID;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiEvent;
import io.github.tofodroid.mods.mimi.common.midi.MidiEventType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class BroadcastEvent extends BasicMidiEvent {
    public final @Nonnull BlockPos pos;
    public final @Nonnull ResourceKey<Level> dimension;
    public final @Nonnull UUID broadcasterId;

    public static BroadcastEvent createAllNotesOffEvent(Byte channel, UUID broadcasterId, ResourceKey<Level> dimension, BlockPos pos) {
        return new BroadcastEvent(MidiEventType.ALL_NOTES_OFF, channel, ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), broadcasterId, dimension, pos, MIMIMod.getProxy().getCurrentServerMillis());
    }

    public static BroadcastEvent createAllNotesOffEvent(UUID broadcasterId, ResourceKey<Level> dimension, BlockPos pos) {
        return new BroadcastEvent(MidiEventType.ALL_NOTES_OFF, ALL_CHANNELS, ALL_NOTES_OFF, Integer.valueOf(0).byteValue(), broadcasterId, dimension, pos, MIMIMod.getProxy().getCurrentServerMillis());
    }

    public BroadcastEvent(BasicMidiEvent source, UUID broadcasterId, ResourceKey<Level> dimension, BlockPos pos) {
        super(source);
        this.dimension = dimension;
        this.broadcasterId = broadcasterId;
        this.pos = pos;
    }

    private BroadcastEvent(MidiEventType type, Byte channel, Byte note, Byte velocity, UUID broadcasterId, ResourceKey<Level> dimension, BlockPos pos, Long eventTime) {
        super(type, channel, note, velocity, eventTime);
        this.dimension = dimension;
        this.broadcasterId = broadcasterId;
        this.pos = pos;
    }

    public Boolean isAllNotesOffEvent() {
        return this.note == ALL_NOTES_OFF;
    }

    public Boolean isControlEvent() {
        return this.note < 0 && this.note != ALL_NOTES_OFF;
    }

    public Boolean isNoteOnEvent() {
        return this.note >= 0 && velocity > 0;
    }

    public Boolean isNoteOffEvent() {
        return this.note >= 0 && velocity <= 0;
    }

    public Byte getControllerNumber() {
        return isControlEvent() ? Integer.valueOf(-this.note).byteValue() : null;
    }

    public Byte getControllerValue() {
        return isControlEvent() ? this.velocity : null;
    }
}
