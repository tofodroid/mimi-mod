package io.github.tofodroid.mods.mimi.common.tile;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.BroadcastConsumerInventoryHolder;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.IBroadcastConsumer;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileReceiver extends AConfigurableMidiPowerSourceTile implements IBroadcastConsumer {
    public static final String REGISTRY_NAME = "receiver";

    protected UUID linkedId;
    protected List<Byte> enabledChannelsList;

    public TileReceiver(BlockPos pos, BlockState state) {
        super(ModTiles.RECEIVER, pos, state);
    }

    @Override
    public void tick(Level world, BlockPos pos, BlockState state) {
        super.tick(world, pos, state);
    }

    @Override
    public void cacheMidiSettings() {
        super.cacheMidiSettings();

        // Remove old consumers before changing linked ID
        if(this.hasLevel() && !this.getLevel().isClientSide) {
            BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
        }

        this.linkedId = MidiNbtDataUtils.getMidiSource(this.getSourceStack());
        this.enabledChannelsList = MidiNbtDataUtils.getEnabledChannelsList(getSourceStack());

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

        if(this.hasLevel() && !this.getLevel().isClientSide()) {
            BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
        }
    }
 
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    
        if(this.hasLevel() && !this.getLevel().isClientSide()) {
            BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
        }
    }

    @Override
    public Boolean shouldTriggerFromNoteOn(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        return (note == null || MidiNbtDataUtils.isNoteFiltered(filterNote, filterOctMin, filterOctMax, invertFilterNoteOct, note));
    }

    @Override
    public Byte getNoteGroupKey(Byte channel, Byte instrumentId) {
        return channel;
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
    public ResourceKey<Level> getDimension() {
        return this.getLevel().dimension();
    }

    @Override
    public List<Byte> getEnabledChannelsList() {
        return this.enabledChannelsList;
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
}
