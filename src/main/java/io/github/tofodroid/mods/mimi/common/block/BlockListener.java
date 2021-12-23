package io.github.tofodroid.mods.mimi.common.block;

import java.util.Random;

import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileListener;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;

public class BlockListener extends AContainerBlock<TileListener> {
    private static final IntegerProperty POWER = BlockStateProperties.POWER;

    public BlockListener() {
        super(Properties.of(Material.METAL).explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
        this.setRegistryName("listener");
    }

    @Override
    public BlockEntityType<TileListener> getTileType() {
        return ModTiles.LISTENER;
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
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
        if (state.getValue(POWER) != 0) {
           worldIn.setBlock(pos, state.setValue(POWER, Integer.valueOf(0)), 3);
        }
    }

    /*
    @Override
    public int getWeakPower(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWER);
    }
    
    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    public void powerTarget(IWorld world, BlockState state, int power, BlockPos pos) {
        if (!world.getPendingBlockTicks().isTickScheduled(pos, state.getBlock())) {
            world.setBlockState(pos, state.with(POWER, Integer.valueOf(power)), 3);
            world.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), 4);
        }
    }
    */
}
