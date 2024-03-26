package io.github.tofodroid.mods.mimi.server.events.broadcast.consumer;

import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiNoteResponsiveTile;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;

public class ReceiverBroadcastConsumer extends ABroadcastConsumer {
    AConfigurableMidiNoteResponsiveTile tile;

    public ReceiverBroadcastConsumer(TileReceiver tile) {        
        super(tile.getUUID(), MidiNbtDataUtils.getMidiSource(tile.getSourceStack()), tile.getEnabledChannelsInt(), tile.getEnabledChannelsList(), tile.getBlockPos(), tile.getLevel().dimension());
        this.tile = tile;
    }

    @Override
    protected void doHandleNoteOn(BroadcastEvent message) {
        tile.onNoteOn(message.channel, message.note, message.velocity, null, message.eventTime);
    }
    
    @Override
    protected void doHandleNoteOff(BroadcastEvent message) {
        tile.onNoteOff(message.channel, message.note, message.velocity, null);
    }

    @Override
    protected void doHandleAllNotesOff(BroadcastEvent message) {
        tile.onAllNotesOff(message.channel, null);
    }
    @Override
    protected Boolean willHandleNoteOn(BroadcastEvent message) {
        return tile.shouldTriggerFromNoteOn(message.channel, message.note, message.velocity, null);
    }

    @Override
    protected Boolean willHandleNoteOff(BroadcastEvent message) {
        return tile.shouldTriggerFromNoteOff(message.channel, message.note, message.velocity, null);
    }

    @Override
    protected Boolean willHandleAllNotesOff(BroadcastEvent message) {
        return tile.shouldTriggerFromAllNotesOff(message.channel, null);
    }

    @Override
    public void onRemoved() {
        // no-op
    }
}
