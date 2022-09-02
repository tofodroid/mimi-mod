package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.LayeredCauldronBlock;

// Clone from net.minecraft.item.IDyeableArmorItem
public interface IDyeableItem extends DyeableLeatherItem {
    public static final Integer DEFAULT_WHITE_COLOR = -1;

    public Boolean isDyeable();
    public Integer getDefaultColor();

    default boolean hasColor(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTagElement("display");
        return isDyeable() && compoundnbt != null && compoundnbt.contains("color", 99);
    }

    default int getColor(ItemStack stack) {
        if(!isDyeable()) {
            return -1;
        }

        CompoundTag compoundnbt = stack.getTagElement("display");
        return compoundnbt != null && compoundnbt.contains("color", 99) ? compoundnbt.getInt("color") : getDefaultColor();
    }

    default void removeColor(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTagElement("display");
        if (isDyeable() && compoundnbt != null && compoundnbt.contains("color")) {
            compoundnbt.remove("color");
        }
    }

    default void setColor(ItemStack stack, int color) {
        if(isDyeable() && color >= 0) {
            stack.getOrCreateTagElement("display").putInt("color", color);
        }
    }
    
    default Boolean washItem(UseOnContext context) {
        if(!context.getPlayer().isCrouching() && ((IDyeableItem)context.getItemInHand().getItem()).hasColor(context.getItemInHand()) && context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof LayeredCauldronBlock) {
            if(context.getPlayer() instanceof ServerPlayer) {
                ((IDyeableItem)context.getItemInHand().getItem()).clearColor(context.getItemInHand());
                LayeredCauldronBlock.lowerFillLevel(context.getLevel().getBlockState(context.getClickedPos()), context.getLevel(), context.getClickedPos());
            }
            return true;
        }

        return false;
    }

    static Boolean isDyeableInstrument(ItemStack stack) {
        return (stack.getItem() instanceof IDyeableItem) && ((IDyeableItem)stack.getItem()).isDyeable();
    }

    static ItemStack dyeItem(ItemStack stack, List<DyeItem> dyes) {
        return DyeableLeatherItem.dyeArmor(stack, dyes);
    }
}
