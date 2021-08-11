package io.github.tofodroid.mods.mimi.common.block;

import java.util.Random;

import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileListener;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.block.SoundType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;

public class BlockListener extends AContainerBlock<TileListener> {
    private static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;

    public BlockListener() {
        super(Properties.create(Material.IRON).hardnessAndResistance(2.f, 6.f).sound(SoundType.WOOD));
        this.setDefaultState(this.stateContainer.getBaseState().with(POWER, Integer.valueOf(0)));
        this.setRegistryName("listener");
    }

    @Override
    public TileEntityType<TileListener> getTileType() {
        return ModTiles.LISTENER;
    }
    
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(POWER, Integer.valueOf(0));
    }
    
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        if (state.get(POWER) != 0) {
           worldIn.setBlockState(pos, state.with(POWER, Integer.valueOf(0)), 3);
        }
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWER);
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
}
