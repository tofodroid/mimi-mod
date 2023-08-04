package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class BlockReceiver extends Block {
    public static final String REGISTRY_NAME = "receiver";
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public BlockReceiver() {
        super(Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWER, Integer.valueOf(0));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(POWER);
    }

    /* TODO
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModTiles.RECEIVER, TileReceiver::doTick);
    }
    */

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
