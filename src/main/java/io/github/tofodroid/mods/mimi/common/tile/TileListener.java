package io.github.tofodroid.mods.mimi.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.server.events.note.consumer.ServerNoteConsumerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileListener extends AConfigurableMidiPowerSourceTile {
    public static final String REGISTRY_NAME = "listener";

    public TileListener(BlockPos pos, BlockState state) {
        super(ModTiles.LISTENER, pos, state);
    }

    @Override
    protected void tick(Level world, BlockPos pos, BlockState state) {
        super.tick(world, pos, state);
    }

    @Override
    public void cacheMidiSettings() {
        super.cacheMidiSettings();

        if(this.hasLevel() && !this.getLevel().isClientSide) {
            ServerNoteConsumerManager.loadListenerTileConsumer(this);
        }
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();

        if(!this.getLevel().isClientSide()) {
            ServerNoteConsumerManager.removeConsumers(this.getUUID());
        }
    }
 
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    
        if(!this.getLevel().isClientSide()) {
            ServerNoteConsumerManager.removeConsumers(this.getUUID());
        }
    }
    
    @Override
    public Boolean shouldTriggerFromNoteOn(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        return true;
    }

    @Override
    public Byte getNoteGroupKey(Byte channel, Byte instrumentId) {
        return instrumentId;
    }
}
