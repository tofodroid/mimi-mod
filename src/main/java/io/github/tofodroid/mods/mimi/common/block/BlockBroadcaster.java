package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileBroadcaster;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.material.Material;

public class BlockBroadcaster extends AContainerBlock<TileBroadcaster> {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final String REGISTRY_NAME = "broadcaster";

    public BlockBroadcaster() {
        super(Properties.of(Material.METAL).explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
    }
    
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!worldIn.isClientSide && worldIn instanceof ServerLevel) {
            if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
                return;
            TileBroadcaster tile = getTileForBlock(worldIn, pos);
            ServerMusicPlayerMidiManager.removeMusicPlayer(tile);
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModTiles.BROADCASTER, TileBroadcaster::doTick);
    }
    
    @Override
    public BlockEntityType<TileBroadcaster> getTileType() {
        return ModTiles.BROADCASTER;
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWER, Integer.valueOf(0));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
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
    
    @Override
    public void animateTick(BlockState p_221932_, Level p_221933_, BlockPos p_221934_, RandomSource p_221935_) {
        int i = p_221932_.getValue(POWER);

        if(i != 0) {
            double d0 = (double)p_221934_.getX() + 0.5D;
            double d1 = (double)p_221934_.getY();
            double d2 = (double)p_221934_.getZ() + 0.5D;
            if (p_221935_.nextDouble() < 0.9D) {
                Double dirVal = p_221935_.nextDouble();
                Direction direction;
                if(dirVal < 0.25D) {
                    direction = Direction.NORTH;
                } else if(dirVal < 0.5D) {
                    direction = Direction.EAST;
                } else if(dirVal < 0.75D) {
                    direction = Direction.SOUTH;
                } else {
                    direction = Direction.WEST;
                }
                
                Direction.Axis direction$axis = direction.getAxis();
                double d4 = p_221935_.nextDouble() * 0.6D - 0.3D;
                double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.62D : d4;
                double d6 = p_221935_.nextDouble() * 6.0D / 16.0D;
                double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.62D : d4;
                p_221933_.addParticle(ParticleTypes.NOTE, d0 + d5, d1 + d6, d2 + d7, p_221935_.nextDouble(), p_221935_.nextDouble(), p_221935_.nextDouble());
            }
        }
    }
 }