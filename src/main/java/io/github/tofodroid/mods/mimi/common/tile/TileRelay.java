package io.github.tofodroid.mods.mimi.common.tile;

import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.api.event.MidiEventType;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.BroadcastConsumerInventoryHolder;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.BroadcastConsumerMapping;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.IBroadcastConsumer;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.IBroadcastProducer;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileRelay extends AConfigurableMidiNoteResponsiveTile implements IBroadcastProducer, IBroadcastConsumer {
    public static final String REGISTRY_NAME = "relay";

    protected UUID linkedId;
    protected List<Byte> enabledChannelsList;
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

        // Remove old consumers before changing linked ID
        if(this.hasLevel() && !this.getLevel().isClientSide) {
            // Stop all notes
            this.reset();
            BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
        }

        UUID newLinkedId = MidiNbtDataUtils.getMidiSource(this.getSourceStack());

        if(newLinkedId != null && newLinkedId.toString().equals(this.getOwnerId().toString())) {
            this.linkedId = UUID.randomUUID();
        } else {
            this.linkedId = newLinkedId;
        }

        this.enabledChannelsList = MidiNbtDataUtils.getEnabledChannelsList(getSourceStack());
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
            this.reset();
            BroadcastManager.removeBroadcastProducer(this.getUUID());
            BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
        }
    }
 
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    
        if(!this.getLevel().isClientSide()) {
            this.reset();
            BroadcastManager.removeBroadcastProducer(this.getUUID());
            BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
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
    public void onReset(Byte channel, Byte instrumentId, Long noteTime) {
        this.broadcast(BroadcastEvent.reset(channel, this.getUUID(), this.getDimension(), this.getBlockPos(), noteTime));
    }

    public BroadcastEvent mapEvent(MidiEventType type, Byte channel, Byte note, Byte velocity, Long noteTime) {
        return new BroadcastEvent(type, channel == BroadcastEvent.ALL_CHANNELS ? BroadcastEvent.ALL_CHANNELS : channelMap[channel], note, velocity, this.getUUID(), this.getDimension(), this.getBlockPos(), this.getBroadcastRange(), noteTime);
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
    public List<Byte> getEnabledChannelsList() {
        return this.enabledChannelsList;
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
        this.onProducerRemoved();
        this.onConsumerRemoved();
    }

    @Override
    public void doHandleEvent(BroadcastEvent message) {
        this.broadcast(mapEvent(message.type, message.channel, message.note, message.velocity, message.eventTime));
    }
    
    @Override
    public Boolean willHandleEvent(BroadcastEvent message) {
        return true;
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
