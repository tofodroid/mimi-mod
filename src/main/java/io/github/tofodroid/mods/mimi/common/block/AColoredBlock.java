package io.github.tofodroid.mods.mimi.common.block;

import java.util.List;
import java.util.Optional;

import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

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
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            ItemStack itemstack = this.getCloneItemStack(level, pos, state);
            level.addFreshEntity(getItemEntity(level, pos, itemstack));
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    protected ItemEntity getItemEntity(Level level, BlockPos pos, ItemStack itemstack) {
        ItemEntity itementity = new ItemEntity(level, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemstack);
        itementity.setDefaultPickUpDelay();
        return itementity;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        this.appendSettingsTooltip(stack, tooltip);
    }
   
    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getCloneItemStack(LevelReader reader, BlockPos pos, BlockState state) {
        ItemStack itemstack = super.getCloneItemStack(reader, pos, state);
        TagUtils.setOrRemoveInt(itemstack, DYE_ID.getName(), state.getOptionalValue(DYE_ID).orElse(0));
        return itemstack;
    }

    protected void appendSettingsTooltip(ItemStack blockItemStack, List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Dyed:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        tooltip.add(Component.literal("  " + DyeColor.byId(TagUtils.getIntOrDefault(blockItemStack, DYE_ID.getName(), 0)).name()).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
    }

    public static final Integer getDecimalColorFromState(BlockState state) {
        Optional<Integer> dyeId = state.getOptionalValue(DYE_ID);

        if(dyeId.isPresent()) {
            return DyeColor.byId(dyeId.get()).getFireworkColor();
        }
        return DyeColor.WHITE.getFireworkColor();
    }
}
