package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMechanicalMaestro extends Block {
    public static final String REGISTRY_NAME = "mechanicalmaestro";

    public BlockMechanicalMaestro() {
        super(Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
    }
        /* TODO
    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(!worldIn.isClientSide && !worldIn.hasNeighborSignal(pos)) {
            TileMechanicalMaestro tile = getTileForBlock(worldIn, pos);
            
            if(tile != null) {
                tile.allNotesOff();
            }
        }
    }
    */

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        /* TODO
        if(!worldIn.isClientSide) {
            if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
                return;
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            
            if (blockEntity instanceof TileMechanicalMaestro) {
                ((TileMechanicalMaestro)blockEntity).allNotesOff();
            }
        }
        */

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
        /* TODO
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModTiles.MECHANICALMAESTRO, TileMechanicalMaestro::doTick);
    }
    */
}