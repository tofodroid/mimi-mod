package io.github.tofodroid.mods.mimi.common.block;

import java.util.Optional;

import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public abstract class AColoredBlock extends Block {
    public static final IntegerProperty DYE_ID = IntegerProperty.create("dye_id", 0, 15);

    public AColoredBlock(Properties p_49795_) {
        super(p_49795_);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Integer dyeId = TagUtils.getIntOrDefault(context.getItemInHand(), DYE_ID.getName(), 0);

        if(dyeId < 0 || dyeId > 15) {
            dyeId = 0;
        }

        return this.defaultBlockState().setValue(DYE_ID, dyeId);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            ItemStack itemstack = new ItemStack(this.asItem(), 1);
            itemstack.getOrCreateTag().putInt(DYE_ID.getName(), state.getOptionalValue(DYE_ID).orElse(0));

            ItemEntity itementity = new ItemEntity(level, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemstack);
            itementity.setDefaultPickUpDelay();
            level.addFreshEntity(itementity);
        }

      super.playerWillDestroy(level, pos, state, player);
   }
   
    @Override
    public ItemStack getCloneItemStack(BlockGetter getter, BlockPos pos, BlockState state) {
        ItemStack itemstack = new ItemStack(this.asItem(), 1);
        itemstack.getOrCreateTag().putInt(DYE_ID.getName(), state.getOptionalValue(DYE_ID).orElse(0));
        return itemstack;
    }

    public static final Integer getDecimalColorFromState(BlockState state) {
        Optional<Integer> dyeId = state.getOptionalValue(DYE_ID);

        if(dyeId.isPresent()) {
            return DyeColor.byId(dyeId.get()).getFireworkColor();
        }
        return DyeColor.WHITE.getFireworkColor();
    }
}
