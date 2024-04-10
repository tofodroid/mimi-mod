package io.github.tofodroid.mods.mimi.server.events.broadcast.consumer;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.server.events.note.consumer.ServerNoteConsumerManager;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InstrumentBroadcastConsumer extends ABroadcastConsumer {
    protected Byte instrumentId;
    protected Byte volume;
    protected InteractionHand handIn;

    public InstrumentBroadcastConsumer(Supplier<BlockPos> pos, Supplier<ResourceKey<Level>> dimension, UUID notePlayerId, ItemStack instrumentStack, InteractionHand handIn) {
        super(notePlayerId, MidiNbtDataUtils.getMidiSource(instrumentStack), MidiNbtDataUtils.getEnabledChannelsInt(instrumentStack), new ArrayList<>(MidiNbtDataUtils.getEnabledChannelsSet(instrumentStack)), pos, dimension);
        this.instrumentId = MidiNbtDataUtils.getInstrumentId(instrumentStack);
        this.volume = MidiNbtDataUtils.getInstrumentVolume(instrumentStack);
        this.handIn = handIn;
    }

    public InstrumentBroadcastConsumer(BlockPos pos, ResourceKey<Level> dimension, UUID notePlayerId, ItemStack instrumentStack, InteractionHand handIn) {
        this(() -> pos, () -> dimension, notePlayerId, instrumentStack, handIn);
    }

    public void sendAllNotesOff() {
        ServerNoteConsumerManager.handleBroadcastPacket(MidiNotePacket.createAllNotesOffPacket(instrumentId, this.ownerId, this.blockPos.get(), this.handIn), this.getDimension());
    }

    @Override
    protected Boolean willHandleNoteOn(BroadcastEvent message) {
        return true;
    }

    @Override
    protected Boolean willHandleNoteOff(BroadcastEvent message) {
        return true;
    }

    @Override
    protected Boolean willHandleAllNotesOff(BroadcastEvent message) {
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
    protected void doHandleNoteOn(BroadcastEvent message) {
        ServerNoteConsumerManager.handleBroadcastPacket(MidiNotePacket.createNotePacket(message.note, MidiNbtDataUtils.applyVolume(this.volume, message.velocity), instrumentId, this.ownerId, blockPos.get(), message.eventTime, handIn), this.getDimension());
    }

    @Override
    protected void doHandleNoteOff(BroadcastEvent message) {
        ServerNoteConsumerManager.handleBroadcastPacket(MidiNotePacket.createNotePacket(message.note, Integer.valueOf(0).byteValue(), instrumentId, this.ownerId, blockPos.get(), message.eventTime, handIn), this.getDimension());
    }

    @Override
    protected void doHandleAllNotesOff(BroadcastEvent message) {
        this.sendAllNotesOff();
    }

    @Override
    public void onRemoved() {
        this.sendAllNotesOff();
    }
}
