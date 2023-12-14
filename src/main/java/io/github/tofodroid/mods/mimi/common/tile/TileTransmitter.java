package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class TileTransmitter extends AConfigurableMidiTile {
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
    }

    @Override
    @SuppressWarnings({"null", "resource"})
    public void onLoad() {
        super.onLoad();

        // Create music player for existing tiles from world save
        if(!this.getLevel().isClientSide && !this.getSourceStack().isEmpty()) {
            ServerMusicTransmitterManager.createTransmitter(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    }
    
    public UUID getUUID() {
        if(this.id == null) {
            String idString = "tile-transmitter-" + this.getBlockPos().getX() + "-" + this.getBlockPos().getY() + "-" + this.getBlockPos().getZ();
            this.id = UUID.nameUUIDFromBytes(idString.getBytes());
        }
        return this.id;
    }
}
