package io.github.tofodroid.mods.mimi.common.tile;

import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.midi.BasicMidiEvent;
import io.github.tofodroid.mods.mimi.common.midi.MidiEventType;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.BroadcastConsumerInventoryHolder;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.BroadcastConsumerMapping;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.IBroadcastConsumer;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.IBroadcastProducer;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileRelay extends AConfigurableMidiNoteResponsiveTile implements IBroadcastProducer, IBroadcastConsumer {
    public static final String REGISTRY_NAME = "relay";

    protected UUID linkedId;
    protected Byte broadcastRange;
    protected Byte[] channelMap;

    protected BroadcastConsumerMapping consumerCache;

    public TileRelay(BlockPos pos, BlockState state) {
        super(ModTiles.RELAY, pos, state);
    }

    @Override
    protected void onFirstTick(ServerLevel world, BlockPos pos, BlockState state) {
        BroadcastManager.registerProducer(this);
    };

    @Override
    public void cacheMidiSettings() {
        super.cacheMidiSettings();

        // Stop all notes
        this.allNotesOff();

        // Remove old consumers before changing linked ID
        if(this.hasLevel() && !this.getLevel().isClientSide) {

            BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
        }

        this.linkedId = MidiNbtDataUtils.getMidiSource(this.getSourceStack());
        this.broadcastRange = MidiNbtDataUtils.getBroadcastRange(this.getSourceStack());
        this.channelMap = MidiNbtDataUtils.getChannelMap(this.getSourceStack());

        if(this.hasLevel() && !this.getLevel().isClientSide) {
            BroadcastConsumerInventoryHolder holder = new BroadcastConsumerInventoryHolder(this.getUUID());
    
            if(this.getLinkedId() != null) {
                holder.putConsumer(0, this);
            }
            BroadcastManager.registerConsumers(holder);
        }
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();

        if(!this.getLevel().isClientSide()) {
            BroadcastManager.removeBroadcastProducer(this.getUUID());
            BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
        }
    }
 
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    
        if(!this.getLevel().isClientSide()) {
            BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
            BroadcastManager.removeBroadcastProducer(this.getUUID());
        }
    }

    @Override
    public void onNoteOn(Byte channel, Byte note, Byte velocity, Byte instrumentId, Long noteTime) {
        this.broadcast(mapEvent(MidiEventType.NOTE_ON, channel, note, velocity, noteTime));
    }

    @Override
    public void onNoteOff(Byte channel, Byte note, Byte velocity, Byte instrumentId, Long noteTime) {
        this.broadcast(mapEvent(MidiEventType.NOTE_OFF, channel, note, velocity, noteTime));
    }

    @Override
    public void onAllNotesOff(Byte channel, Byte instrumentId, Long noteTime) {
        this.broadcast(BasicMidiEvent.allNotesOff(channel, noteTime));
    }

    public BasicMidiEvent mapEvent(MidiEventType type, Byte channel, Byte note, Byte velocity, Long noteTime) {
        return new BasicMidiEvent(type, channelMap[channel], note, velocity, noteTime);
    }

    @Override
    public Boolean shouldTriggerFromNoteOn(Byte channel, Byte note, Byte velocity, Byte instrumentId) {
        return true;        
    }

    @Override
    public Boolean shouldTriggerFromNoteOff(Byte channel, Byte note, Byte velocity, Byte instrumentId) {
        return true;        
    }

    @Override
    public Boolean shouldTriggerFromAllNotesOff(Byte channel, Byte instrumentId) {
        return true;
    }

    @Override
    public UUID getLinkedId() {
        return this.linkedId;
    }

    @Override
    public UUID getOwnerId() {
        return this.getUUID();
    }

    @Override
    public Integer getBroadcastRange() {
        return this.broadcastRange != null ? 16 * this.broadcastRange : 64;
    }

    @Override
    public ResourceKey<Level> getDimension() {
        return this.getLevel().dimension();
    }

    @Override
    public void tickConsumer() { /* No-op */ }

    @Override
    public void onConsumerRemoved() { /* No-op */ }

    @Override
    public void close() throws Exception {
        this.onConsumerRemoved();
    }

    @Override
    public void doHandleNoteOn(BroadcastEvent message) {
        this.onNoteOn(message.channel, message.note, message.velocity, null, message.eventTime);
    }
    
    @Override
    public void doHandleNoteOff(BroadcastEvent message) {
        this.onNoteOff(message.channel, message.note, message.velocity, null, message.eventTime);
    }

    @Override
    public void doHandleAllNotesOff(BroadcastEvent message) {
        this.onAllNotesOff(message.channel, null, message.eventTime);
    }
    @Override
    public Boolean willHandleNoteOn(BroadcastEvent message) {
        return this.shouldTriggerFromNoteOn(message.channel, message.note, message.velocity, null);
    }

    @Override
    public Boolean willHandleNoteOff(BroadcastEvent message) {
        return this.shouldTriggerFromNoteOff(message.channel, message.note, message.velocity, null);
    }

    @Override
    public Boolean willHandleAllNotesOff(BroadcastEvent message) {
        return this.shouldTriggerFromAllNotesOff(message.channel, null);
    }

    @Override
    public BroadcastConsumerMapping getConsumers() {
        return this.consumerCache;
    }

    @Override
    public void linkConsumers(List<IBroadcastConsumer> consumers) {
        this.consumerCache = new BroadcastConsumerMapping(this.getUUID(), consumers);
    }

    @Override
    public void tickProducer() {
        // No-op
    }

    @Override
    public void onProducerRemoved() {
        // No-op
    }
}
