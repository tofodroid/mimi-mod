package io.github.tofodroid.mods.mimi.common.tile;

import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.api.event.broadcast.BroadcastConsumerInventoryHolder;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.IBroadcastConsumer;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
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
    public BlockPos getConsumePos() {
        return this.getBlockPos();
    }

    @Override
    public ResourceKey<Level> getConsumeDimension() {
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
    public void doHandleEvent(BroadcastEvent message) {
        switch(message.type) {
            case NOTE_ON:
                this.onNoteOn(message.channel, message.note, message.velocity, null, message.eventTime);
                break;
            case NOTE_OFF:
                this.onNoteOff(message.channel, message.note, message.velocity, null, message.eventTime);
                break;
            case RESET:
                this.onReset(message.channel, null, message.eventTime);
                break;
            default: break;
        }
    }

    @Override
    public Boolean willHandleEvent(BroadcastEvent message) {
        switch(message.type) {
            case NOTE_ON:
                return (message.note == null || MidiNbtDataUtils.isNoteFiltered(filterNote, filterOctMin, filterOctMax, invertFilterNoteOct, message.note));
            case NOTE_OFF:
                return this.isHeld() && (message.note == null || MidiNbtDataUtils.isNoteFiltered(filterNote, filterOctMin, filterOctMax, invertFilterNoteOct, message.note));
            case RESET:
                return this.isHeld();
            default:
                return false;
        }
    }
}
