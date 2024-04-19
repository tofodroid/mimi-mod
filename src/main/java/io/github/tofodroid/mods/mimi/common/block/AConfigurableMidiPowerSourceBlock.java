package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiPowerSourceTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class AConfigurableMidiPowerSourceBlock<B extends AConfigurableMidiPowerSourceTile> extends AConfigurableNoteResponsiveTileBlock<B> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

    public AConfigurableMidiPowerSourceBlock(Properties builder) {
        this(builder, false, false, false);
    }

    public AConfigurableMidiPowerSourceBlock(Properties builder, Boolean defaultPowerState, Boolean defaultTriggeredState, Boolean defaultInvertedState) {
        super(builder);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(POWERED, defaultPowerState)
                .setValue(INVERTED, defaultInvertedState)
        );
    }

    // POWER
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(POWERED, INVERTED);
    }
    
    @Override
    public int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) != state.getValue(INVERTED) ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(BlockState p_55730_) {
        return true;
    }
}
