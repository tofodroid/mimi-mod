package io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.instrument;

import java.util.UUID;
import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.api.event.MidiEventType;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.common.api.event.note.NoteEvent;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.AServerBroadcastConsumer;
import io.github.tofodroid.mods.mimi.server.events.note.consumer.ServerNoteConsumerManager;
import io.github.tofodroid.mods.mimi.util.ByteUtils;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InstrumentBroadcastConsumer extends AServerBroadcastConsumer {
    protected Byte instrumentId;
    protected Byte volume;
    protected InteractionHand handIn;

    public InstrumentBroadcastConsumer(Supplier<BlockPos> pos, Supplier<ResourceKey<Level>> dimension, UUID notePlayerId, ItemStack instrumentStack, InteractionHand handIn) {
        super(notePlayerId, MidiNbtDataUtils.getMidiSource(instrumentStack), MidiNbtDataUtils.getEnabledChannelsInt(instrumentStack), MidiNbtDataUtils.getEnabledChannelsList(instrumentStack), pos, dimension);
        this.instrumentId = MidiNbtDataUtils.getInstrumentId(instrumentStack);
        this.volume = MidiNbtDataUtils.getInstrumentVolume(instrumentStack);
        this.handIn = handIn;
    }

    public InstrumentBroadcastConsumer(BlockPos pos, ResourceKey<Level> dimension, UUID notePlayerId, ItemStack instrumentStack, InteractionHand handIn) {
        this(() -> pos, () -> dimension, notePlayerId, instrumentStack, handIn);
    }

    @Override
    public Boolean willHandleEvent(BroadcastEvent message) {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof InstrumentBroadcastConsumer) {
            InstrumentBroadcastConsumer o = (InstrumentBroadcastConsumer) other;

            return o.linkedId.equals(this.linkedId) &&
                o.instrumentId == this.instrumentId &&
                o.enabledChannels == this.enabledChannels &&
                o.volume == this.volume;
        }
        
        return false;
    }

    @Override
    public void doHandleEvent(BroadcastEvent message) {
        switch(message.type) {
            case NOTE_ON:
                ServerNoteConsumerManager.handleEvent(
                    new NoteEvent(MidiEventType.NOTE_ON, false, instrumentId, handIn, message.channel, message.note, MidiNbtDataUtils.applyVolume(this.volume, message.velocity), this.ownerId, this.getConsumeDimension(), getConsumePos(), message.eventTime)
                );
                break;
            case NOTE_OFF:
                ServerNoteConsumerManager.handleEvent(
                    new NoteEvent(MidiEventType.NOTE_OFF, false, instrumentId, handIn, message.channel, message.note, ByteUtils.ZERO, this.ownerId, this.getConsumeDimension(), getConsumePos(), message.eventTime)
                );
                break;
            case CONTROL:
                ServerNoteConsumerManager.handleEvent(
                    new NoteEvent(MidiEventType.CONTROL, false, instrumentId, handIn, message.channel, message.note, message.velocity, this.ownerId, this.getConsumeDimension(), getConsumePos(), message.eventTime)
                );
                break;
            case RESET:
                this.sendReset();
            case PITCH_BEND:
                ServerNoteConsumerManager.handleEvent(
                    new NoteEvent(MidiEventType.PITCH_BEND, false, instrumentId, handIn, message.channel, message.note, message.velocity, this.ownerId, this.getConsumeDimension(), getConsumePos(), message.eventTime)
                );
                break;
            default:
                break;
        }
    }

    @Override
    public void onConsumerRemoved() {
        this.sendReset();
    }

    public void sendReset() {
        ServerNoteConsumerManager.handleEvent(
            NoteEvent.reset(instrumentId, handIn, linkedId, getConsumeDimension(), getConsumePos(), MIMIMod.getProxy().getCurrentServerMillis())
        );
    }
}
