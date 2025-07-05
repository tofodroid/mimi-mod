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
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public abstract class AConfigurableMidiPowerSourceBlock<B extends AConfigurableMidiPowerSourceTile> extends AConfigurableNoteResponsiveTileBlock<B> {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

    public AConfigurableMidiPowerSourceBlock(Properties builder) {
        this(builder, false, false, false);
    }

    public AConfigurableMidiPowerSourceBlock(Properties builder, Boolean defaultPowerState, Boolean defaultTriggeredState, Boolean defaultInvertedState) {
        super(builder);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(POWER, defaultPowerState ? 15 : 0)
                .setValue(INVERTED, defaultInvertedState)
        );
    }

    // POWER
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(POWER, INVERTED);
    }
    
    @Override
    public int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
        return Math.abs((state.getValue(INVERTED) ? 15 : 0) - state.getValue(POWER));
    }

    @Override
    public boolean isSignalSource(BlockState p_55730_) {
        return true;
    }
}
