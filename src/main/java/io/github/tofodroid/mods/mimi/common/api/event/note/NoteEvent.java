package io.github.tofodroid.mods.mimi.common.api.event.note;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.api.event.AMidiEvent;
import io.github.tofodroid.mods.mimi.common.api.event.MidiEventType;
import io.github.tofodroid.mods.mimi.util.ByteUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class NoteEvent extends AMidiEvent {
    public static final Integer MIMI_NOTE_PLAYING_LEVEL_EVENT_ID = 11320;
    public static final Integer NOTE_DEF_RANGE = 32;

    public final @Nonnull Boolean clientSource;
    public final @Nonnull Byte instrumentId;
    public final @Nullable InteractionHand handIn;

    public NoteEvent(MidiEventType type, Boolean clientSource, Byte instrumentId, InteractionHand handIn, Byte note, Byte velocity, UUID senderId, ResourceKey<Level> dimension, BlockPos pos, Long eventTime) {
        super(type, note, velocity, eventTime, senderId, dimension, pos, NoteEvent.getRange(type));
        this.clientSource = clientSource;
        this.instrumentId = instrumentId;
        this.handIn = handIn;
    }

    public static NoteEvent reset(Byte instrumentId, InteractionHand handIn, UUID senderId, ResourceKey<Level> dimension, BlockPos pos, Long eventTime) {
        return new NoteEvent(MidiEventType.RESET, false, instrumentId, handIn, ByteUtils.ZERO, ByteUtils.ZERO, senderId, dimension, pos, eventTime);
    }

    private static Integer getRange(MidiEventType type) {
        switch(type) {
            case NOTE_OFF:
            case RESET:
                return NOTE_DEF_RANGE * 2;
            default: return NOTE_DEF_RANGE;
        }
    }
}
