package io.github.tofodroid.mods.mimi.common.api.event;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class AMidiEvent<E extends AMidiEvent<E>> {
    public static final Byte ALL_CHANNELS = Byte.MAX_VALUE;
    public static final Byte ALL_NOTES_OFF = Byte.MIN_VALUE;
    public static final Integer INF_RANGE = -1;

    public final @Nonnull MidiEventType type;
    public final @Nonnull Byte channel;
    public final @Nonnull Byte note;
    public final @Nonnull Byte velocity;
    public final @Nonnull Long eventTime;
    public final @Nonnull UUID senderId;
    public final @Nonnull ResourceKey<Level> dimension;
    public final @Nonnull BlockPos pos;
    public final @Nonnull Integer range;
    public @Nullable Integer extData;

    public AMidiEvent(E source) {
        this(source.type, source.channel, source.note, source.velocity, source.eventTime, source.senderId, source.dimension, source.pos, source.range);
    }

    protected AMidiEvent(MidiEventType type, Byte channel, Byte note, Byte velocity, Long eventTime, UUID senderId, ResourceKey<Level> dimension, BlockPos pos, Integer range) {
        this.channel = channel;
        this.type = type;
        this.note = note;
        this.velocity = velocity;
        this.eventTime = eventTime;
        this.range = range;
        this.pos = pos;
        this.senderId = senderId;
        this.dimension = dimension;
    }

    @SuppressWarnings("unchecked")
    protected final E getThis() {
        return (E)this;
    }

    public E withExtData(Integer extData) {
        this.extData = extData;
        return getThis();
    }
}
