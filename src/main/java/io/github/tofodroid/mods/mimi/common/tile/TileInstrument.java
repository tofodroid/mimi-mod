package io.github.tofodroid.mods.mimi.common.tile;

import org.joml.Vector3d;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.entity.EntitySeat;
import io.github.tofodroid.mods.mimi.common.item.IColorableItem;
import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.instrument.EntityInstrumentConsumerEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class TileInstrument extends AConfigurableTile {
    public static final String REGISTRY_NAME = "instrument";
    public static final String COLOR_TAG = "color";
    protected EntitySeat currentSeat = null;
    protected Integer color;

    public TileInstrument(BlockPos pos, BlockState state) {
        super(ModTiles.INSTRUMENT, pos, state, 1);
    }

    public void attemptSit(Player player) {
        if(player.level().isClientSide) {
            return;
        }

        EntitySeat newSeat = EntitySeat.create(player.level(), this.getBlockPos(), this.getSeatOffset(getBlockState()), player);

        if(newSeat != null) {
            this.currentSeat = newSeat;
        }
    }

    public void ejectPlayer() {
        if(this.currentSeat != null && !this.currentSeat.isRemoved()) {
            this.currentSeat.ejectPassengers();
            this.currentSeat.remove(RemovalReason.DISCARDED);
        }
    }

    public Player getCurrentPlayer() {
        if(this.currentSeat != null && !this.currentSeat.isRemoved()) {
            return this.currentSeat.getRider();
        }
        return null;
    }
    
    protected Vector3d getSeatOffset(BlockState state) {
        switch(state.getValue(BlockInstrument.DIRECTION)) {
            case NORTH:
                return new Vector3d(0.5, 0, 0.05);
            case SOUTH:
                return new Vector3d(0.5, 0, 0.95);
            case EAST:
                return new Vector3d(0.95, 0, 0.5);
            case WEST:
                return new Vector3d(0.05, 0, 0.5);
            default:
                return new Vector3d(0.5, 0, 0.05);
        }
    }

    @Override
    protected void onSourceStackChanged() {
        if(this.blockInstrument().isColorable() && ((IColorableItem)this.getSourceStack().getItem()).hasColor(this.getSourceStack())) {
            this.color = ((IColorableItem)this.getSourceStack().getItem()).getColor(this.getSourceStack());
        }

        Player currentPlayer = this.getCurrentPlayer();

        if(currentPlayer != null) {
            EntityInstrumentConsumerEventHandler.reloadEntityInstrumentConsumers(currentPlayer);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        Player currentPlayer = this.getCurrentPlayer();

        if(currentPlayer != null && !this.getLevel().isClientSide()) {
            this.ejectPlayer();
            EntityInstrumentConsumerEventHandler.reloadEntityInstrumentConsumers(currentPlayer);
        }
    }
 
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        Player currentPlayer = this.getCurrentPlayer();

        if(currentPlayer != null && !this.getLevel().isClientSide()) {
            this.ejectPlayer();
            EntityInstrumentConsumerEventHandler.reloadEntityInstrumentConsumers(currentPlayer);
        }
    }

    public Byte getInstrumentId() {
        return this.blockInstrument().getInstrumentId();
    }

    public Boolean hasColor() {
        return color != null && this.blockInstrument().isColorable();
    }

    public Integer getColor() { 
        if(!this.blockInstrument().isColorable()) {
            return -1;
        }

        return hasColor() ? color : this.blockInstrument().getDefaultColor();
    }

    private BlockInstrument blockInstrument() {
        return (BlockInstrument)getBlockState().getBlock();
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        if(this.color != null) {
            compound.putInt(COLOR_TAG, color);
        }
    }

    public void onItemsLoaded() {
        super.onItemsLoaded();
        this.onSourceStackChanged();
    }

    @Override
    public BlockPos getBlockPos() {
        return this.worldPosition;
    }
}