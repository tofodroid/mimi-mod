package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.block.BlockTransmitter;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter.ServerTransmitterManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class TileTransmitter extends AConfigurableMidiTile {
    public static final String REGISTRY_NAME = "transmitter";
    private UUID id;

    public TileTransmitter(BlockPos pos, BlockState state) {
        super(ModTiles.TRANSMITTER, pos, state, 1);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        // Create music player for existing tiles from world save
        if(this.hasLevel() && !this.getLevel().isClientSide && !this.getSourceStack().isEmpty()) {
            ServerTransmitterManager.createTransmitter(this);
            this.setUnpowered();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        // Create music player for existing tiles from world save
        if(this.hasLevel() && !this.getLevel().isClientSide && !this.getSourceStack().isEmpty()) {
            ServerTransmitterManager.createTransmitter(this);
            this.setUnpowered();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if(!this.getLevel().isClientSide()) {
            BroadcastManager.removeBroadcastProducer(this.getUUID());
        }
    }
 
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        if(!this.getLevel().isClientSide()) {
            BroadcastManager.removeBroadcastProducer(this.getUUID());
        }
    }
    
    public UUID getUUID() {
        if(this.id == null) {
            String idString = "tile-transmitter-" + this.getBlockPos().getX() + "-" + this.getBlockPos().getY() + "-" + this.getBlockPos().getZ();
            this.id = UUID.nameUUIDFromBytes(idString.getBytes());
        }
        return this.id;
    }

    public Boolean isPowered() {
        return this.getBlockState().getValue(BlockTransmitter.POWERED);
    }

    public void setPowered() {
        this.setPowerAndUpdate(true);
    }

    public void setUnpowered() {
        this.setPowerAndUpdate(false);
    }

    protected void setPowerAndUpdate(Boolean powered) {
        this.getLevel().setBlockAndUpdate(
            getBlockPos(), 
            getBlockState()
                .setValue(BlockTransmitter.POWERED, powered)
        );
        this.getLevel().updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
    }
}
