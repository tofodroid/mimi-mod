package io.github.tofodroid.mods.mimi.common.api.event.broadcast;

import java.util.UUID;

import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.api.event.AMidiEvent;
import io.github.tofodroid.mods.mimi.common.api.event.MidiEventType;
import io.github.tofodroid.mods.mimi.util.ByteUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class BroadcastEvent extends AMidiEvent<BroadcastEvent> {
    public BroadcastEvent(MidiEventType type, Byte channel, Byte note, Byte velocity, UUID senderId, ResourceKey<Level> dimension, BlockPos pos, Integer range, Long eventTime) {
        super(type, channel, note, velocity, eventTime, senderId, dimension, pos, BroadcastEvent.getRealRange(type, range));
    }

    public static final BroadcastEvent fromShortMessage(ShortMessage message, UUID senderId, ResourceKey<Level> dimension, BlockPos pos, Integer range, Integer overrideVelocity) {
        MidiEventType type = MidiEventType.fromShortMessage(message);

        if(type != MidiEventType.OTHER || type != MidiEventType.UNKNOWN) {
            return new BroadcastEvent(
                type,
                (byte)message.getChannel(),
                Integer.valueOf(message.getData1()).byteValue(),
                type != MidiEventType.NOTE_OFF ? overrideVelocity.byteValue() : 0,
                senderId,
                dimension,
                pos,
                BroadcastEvent.getRealRange(type, range),
                MIMIMod.getProxy().getCurrentServerMillis() + (type == MidiEventType.NOTE_ON ? 1 : 0)
            );
        } else {
            return null;
        }
    }

    public static final BroadcastEvent fromShortMessage(ShortMessage message, UUID senderId, ResourceKey<Level> dimension, BlockPos pos, Integer range) {
        return fromShortMessage(message, senderId, dimension, pos, range, message.getData2());
    }

    public static BroadcastEvent reset(Byte channel, UUID senderId, ResourceKey<Level> dimension, BlockPos pos, Long eventTime) {
        return new BroadcastEvent(MidiEventType.RESET, channel, ByteUtils.ZERO, ByteUtils.ZERO, senderId, dimension, pos, INF_RANGE, eventTime);
    }

    public static BroadcastEvent reset(UUID senderId, ResourceKey<Level> dimension, BlockPos pos, Long eventTime) {
        return new BroadcastEvent(MidiEventType.RESET, ALL_CHANNELS, ByteUtils.ZERO, ByteUtils.ZERO, senderId, dimension, pos, INF_RANGE, eventTime);
    }

    private static Integer getRealRange(MidiEventType type, Integer sourceRange) {
        switch(type) {
            case NOTE_OFF:
            case RESET:
                return INF_RANGE;
            default: return sourceRange;
        }
    }
}
