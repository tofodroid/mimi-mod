package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiTile;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public abstract class APoweredConfigurableMidiBlock<B extends AConfigurableMidiTile> extends AConfigurableMidiBlock<B> {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    public APoweredConfigurableMidiBlock(Properties builder) {
        this(builder, 0, false);
    }

    public APoweredConfigurableMidiBlock(Properties builder, Integer defaultPowerLevel, Boolean defaultTriggeredState) {
        super(builder);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(POWER, defaultPowerLevel)
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
        state.add(POWER, TRIGGERED);
    }
    
    @Override
    public int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }
    
    @Override
    public int getDirectSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }

    @Override
    public boolean isSignalSource(BlockState p_55730_) {
        return true;
    }

    public void powerTarget(Level world, BlockState state, int power, BlockPos pos) {
        //if(state.getValue(TRIGGERED))

        if (!world.getBlockTicks().hasScheduledTick(pos, state.getBlock())) {
            world.setBlock(pos, state.setValue(POWER, Integer.valueOf(power)), 3);
            
            for(Direction direction : Direction.values()) {
                world.updateNeighborsAt(pos.relative(direction), this);
            }
            world.scheduleTick(pos, this, 4);
        }
    }
}
