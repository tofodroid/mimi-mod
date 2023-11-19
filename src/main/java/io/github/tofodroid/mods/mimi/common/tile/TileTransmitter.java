package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class TileTransmitter extends AConfigurableMidiTile {
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
            ServerMusicPlayerManager.createTransmitter(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    }
    
    public UUID getUUID() {
        String idString = "tile-transmitter-" + this.getBlockPos().getX() + "-" + this.getBlockPos().getY() + "-" + this.getBlockPos().getZ();
        return UUID.nameUUIDFromBytes(idString.getBytes());
    }
}
