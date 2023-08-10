package io.github.tofodroid.mods.mimi.common.block;

import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class AConfigurableMidiBlock<B extends AConfigurableMidiTile> extends AContainerBlock<B> {

    public AConfigurableMidiBlock(Properties builder) {
        super(builder);
    }

    protected abstract void openGui(Level worldIn, Player player, B tile);

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        B tile = getTileForBlock(worldIn, pos);
        
        if(tile != null) {
           if(worldIn.isClientSide) {
                this.openGui(worldIn, player, tile);
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.SUCCESS;
    }
    
    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        B tileEntity = getTileForBlock(worldIn, pos);
        if (tileEntity instanceof B) {
            ItemStack newStack = new ItemStack(stack.getItem(), stack.getCount());
            newStack.setTag(stack.getOrCreateTag().copy());
            ((B)tileEntity).setSourceStack(newStack);
        }
    }
}
