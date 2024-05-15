package io.github.tofodroid.mods.mimi.common.block;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.tile.AConfigurableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

public abstract class AConfigurableTileBlock<B extends AConfigurableTile> extends AContainerBlock<B> {

    public AConfigurableTileBlock(Properties builder) {
        super(builder);
    }

    protected abstract void openGui(Level worldIn, Player player, B tile);
    protected abstract void appendSettingsTooltip(ItemStack blockItemStack, List<Component> tooltip);

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        this.appendSettingsTooltip(stack, tooltip);
    }
    
    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        B tileEntity = getTileForBlock(worldIn, pos);
        if (tileEntity instanceof B) {
            ItemStack newStack = new ItemStack(stack.getItem(), 1);
            newStack.applyComponents(stack.getComponents());
            ((B)tileEntity).setSourceStack(newStack);
        }
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getCloneItemStack(LevelReader reader, BlockPos pos, BlockState state) {
        BlockEntity tile = reader.getBlockEntity(pos);
        
        if(tile != null && tile instanceof AConfigurableTile) {
            return ((AConfigurableTile)tile).getSourceStack();
        }

        return super.getCloneItemStack(reader, pos, state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Arrays.asList();
    }
}
