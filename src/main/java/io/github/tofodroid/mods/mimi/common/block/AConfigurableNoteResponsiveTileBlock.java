package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiNoteResponsiveTile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AConfigurableNoteResponsiveTileBlock<B extends AConfigurableMidiNoteResponsiveTile> extends AConfigurableTileBlock<B> {

    public AConfigurableNoteResponsiveTileBlock(Properties builder) {
        super(builder);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, getTileType(), B::doTick);
    }
}
