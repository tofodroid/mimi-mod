package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiPowerSourceTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class APoweredConfigurableMidiBlock<B extends AConfigurableMidiPowerSourceTile> extends AConfigurableMidiBlock<B> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    public APoweredConfigurableMidiBlock(Properties builder) {
        this(builder, false, false);
    }

    public APoweredConfigurableMidiBlock(Properties builder, Boolean defaultPowerState, Boolean defaultTriggeredState) {
        super(builder);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(POWERED, defaultPowerState)
                .setValue(TRIGGERED, defaultTriggeredState)
        );
    }

    // POWER
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, getTileType(), B::doTick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(POWERED, TRIGGERED);
    }
    
    @Override
    public int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(BlockState p_55730_) {
        return true;
    }
}
