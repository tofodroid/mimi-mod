package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockMechanicalMaestro extends AContainerBlock<TileMechanicalMaestro> {
    public static final String REGISTRY_NAME = "mechanicalmaestro";

    public BlockMechanicalMaestro() {
        super(Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, getTileType(), TileMechanicalMaestro::doTick);
    }
    
    @Override
    public BlockEntityType<TileMechanicalMaestro> getTileType() {
        return ModTiles.MECHANICALMAESTRO;
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(!worldIn.isClientSide && !worldIn.hasNeighborSignal(pos)) {
            TileMechanicalMaestro tile = getTileForBlock(worldIn, pos);
            
            if(tile != null) {
                tile.allNotesOff();
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!worldIn.isClientSide) {
            if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
                return;
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            
            if (blockEntity instanceof TileMechanicalMaestro) {
                ((TileMechanicalMaestro)blockEntity).allNotesOff();
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
}