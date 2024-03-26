package io.github.tofodroid.mods.mimi.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManagerConsumerEventHooks;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileReceiver extends AConfigurableMidiPowerSourceTile {
    public static final String REGISTRY_NAME = "receiver";

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

        if(this.hasLevel() && !this.getLevel().isClientSide) {
            BroadcastManagerConsumerEventHooks.reloadReceiverTileConsumer(this);
        }
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();

        if(!this.getLevel().isClientSide()) {
            BroadcastManager.removeOwnedBroadcastConsumers(this.getUUID());
        }
    }
 
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    
        if(!this.getLevel().isClientSide()) {
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
}
