package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BlockMechanicalMaestro extends AContainerBlock<TileMechanicalMaestro> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final String REGISTRY_NAME = "mechanicalmaestro";

    public BlockMechanicalMaestro(Properties props) {
        super(props.explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, Boolean.valueOf(false)));
    }
        
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!worldIn.isClientSide) {
            TileMechanicalMaestro tile = getTileForBlock(worldIn, pos);

            if(tile != null) {
                // TODO link anything?
            }
        }

        return super.use(state, worldIn, pos, player, hand, hit);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(POWERED, Boolean.valueOf(context.getLevel().hasNeighborSignal(context.getClickedPos())));
    }
        
    @Override
    public BlockEntityType<TileMechanicalMaestro> getTileType() {
        return ModTiles.MECHANICALMAESTRO;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(POWERED);
    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
        if(!worldIn.isClientSide) {
            TileMechanicalMaestro tile = getTileForBlock(worldIn, pos);
            Boolean shouldBePowered = worldIn.hasNeighborSignal(pos);

            if (state.getValue(POWERED) != shouldBePowered) {
                worldIn.setBlock(pos, state.cycle(POWERED), 2);
    
                if(tile != null) {
                    if(!shouldBePowered) {
                        tile.allNotesOff();
                    }
                    tile.refreshMidiReceivers();
                }
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(!worldIn.isClientSide) {
            Boolean wasPowered = state.getValue(POWERED);
            
            if(wasPowered != worldIn.hasNeighborSignal(pos)) {
                worldIn.scheduleTick(pos, this, 4);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!worldIn.isClientSide) {
            if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
                return;
    
            TileMechanicalMaestro tile = getTileForBlock(worldIn, pos);
            
            if (tile != null) {
                BroadcastManager.removeOwnedBroadcastConsumers(tile.getUUID());
                tile.allNotesOff();
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
}