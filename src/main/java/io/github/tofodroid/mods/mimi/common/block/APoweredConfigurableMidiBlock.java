package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public abstract class APoweredConfigurableMidiBlock<B extends AConfigurableMidiTile> extends AConfigurableMidiBlock<B> {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public APoweredConfigurableMidiBlock(Properties builder) {
        this(builder, 0);
    }

    public APoweredConfigurableMidiBlock(Properties builder, Integer defaultPowerLevel) {
        super(builder);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, defaultPowerLevel));
    }

    // POWER
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, getTileType(), B::doTick);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWER, Integer.valueOf(0));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(POWER);
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
        if (!world.getBlockTicks().hasScheduledTick(pos, state.getBlock())) {
            world.setBlock(pos, state.setValue(POWER, Integer.valueOf(power)), 3);
            
            for(Direction direction : Direction.values()) {
                world.updateNeighborsAt(pos.relative(direction), this);
            }
            world.scheduleTick(pos, this, 4);
        }
    }
}
