package io.github.tofodroid.mods.mimi.common.tile;

import org.joml.Vector3d;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.entity.EntitySeat;
import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.instrument.EntityInstrumentConsumerEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.DyedItemColor;
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
        if(this.getSourceStack().is(ItemTags.DYEABLE) && this.getSourceStack().has(DataComponents.DYED_COLOR)) {
            this.color = this.getSourceStack().get(DataComponents.DYED_COLOR).rgb();
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

    public Integer getColor() {
        if(this.color != null) {
            return this.color;
        } else if(this.blockInstrument().getDefaultColor() != null) {
            return this.blockInstrument().getDefaultColor();
        }
        return -1;
    }

    private BlockInstrument blockInstrument() {
        return (BlockInstrument)getBlockState().getBlock();
    }

    @Override
    public void onItemsLoaded() {
        super.onItemsLoaded();

        // Load color from stack
        Integer stackColor = DyedItemColor.getOrDefault(this.getSourceStack(), -1);
        if(stackColor != -1) {
            this.color = stackColor;
        }
    }
}