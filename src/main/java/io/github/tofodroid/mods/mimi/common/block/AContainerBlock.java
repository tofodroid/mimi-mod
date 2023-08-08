package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.AContainerTile;
import io.github.tofodroid.mods.mimi.common.tile.AStaticInventoryTile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;


public abstract class AContainerBlock<T extends BlockEntity & Container> extends BaseEntityBlock {
    protected AContainerBlock(Properties builder) {
        super(builder);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
			return;
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        
        if (blockEntity instanceof AContainerTile) {
            ((AContainerTile)blockEntity).dropContent();
            worldIn.updateNeighbourForOutputSignal(pos, this);
        } else if (blockEntity instanceof AStaticInventoryTile) {
            ((AStaticInventoryTile)blockEntity).dropContent();
            worldIn.updateNeighbourForOutputSignal(pos, this);
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
    
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!worldIn.isClientSide) {
            T tile = getTileForBlock(worldIn, pos);
            if(tile != null) {
                ServerPlayer serverPlayerEntity = (ServerPlayer) player;
                NetworkHooks.openScreen(serverPlayerEntity, this.getMenuProvider(state, worldIn, pos), (FriendlyByteBuf -> {
                    writeGuiFriendlyByteBuf(state, worldIn, pos, player, hand, FriendlyByteBuf);
                }));
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.SUCCESS;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getTileType().create(pos, state);
    }

    public abstract BlockEntityType<T> getTileType();
    
    public void writeGuiFriendlyByteBuf(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    @SuppressWarnings("unchecked")
    public T getTileForBlock(Level worldIn, BlockPos pos) {
        BlockEntity entity = worldIn.getBlockEntity(pos);
        return entity != null && entity instanceof BlockEntity && entity instanceof Container ? (T)entity : null;
    }
    
    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
       return RenderShape.MODEL;
    }
}
