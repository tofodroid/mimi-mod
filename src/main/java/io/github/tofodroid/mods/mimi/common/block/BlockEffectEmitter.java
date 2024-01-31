package io.github.tofodroid.mods.mimi.common.block;

import java.util.List;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileEffectEmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BlockEffectEmitter extends AConfigurableTileBlock<TileEffectEmitter> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final String REGISTRY_NAME = "effectemitter";

    public BlockEffectEmitter() {
        super(Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.COPPER));
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED,false));
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
            .setValue(POWERED, Boolean.valueOf(context.getLevel().hasNeighborSignal(context.getClickedPos())));
    }
        
    @Override
    public BlockEntityType<TileEffectEmitter> getTileType() {
        return ModTiles.EFFECTEMITTER;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(POWERED);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, getTileType(), TileEffectEmitter::doTick);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(!worldIn.isClientSide) {
            Boolean wasPowered = state.getValue(POWERED);
            
            if(wasPowered != worldIn.hasNeighborSignal(pos)) {
                worldIn.setBlock(pos, state.cycle(POWERED), 2);
            }
        }
    }

    @Override
    protected void openGui(Level worldIn, Player player, TileEffectEmitter tile) {
        ClientGuiWrapper.openEffectEmitterGui(worldIn, tile.getBlockPos(), tile.getSourceStack());
    }

    @Override
    protected void appendSettingsTooltip(ItemStack blockItemStack, List<Component> tooltip) {
        return;
    }
}