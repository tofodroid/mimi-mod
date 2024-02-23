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
public interface IColorableItem extends DyeableLeatherItem {
    public static final Integer DEFAULT_WHITE_COLOR = -1;

    public Boolean isColorable();
    public Integer getDefaultColor();

    default boolean hasColor(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTagElement("display");
        return isColorable() && compoundnbt != null && compoundnbt.contains("color", 99);
    }

    default int getColor(ItemStack stack) {
        if(!isColorable()) {
            return -1;
        }

        CompoundTag compoundnbt = stack.getTagElement("display");
        return compoundnbt != null && compoundnbt.contains("color", 99) ? compoundnbt.getInt("color") : getDefaultColor();
    }

    default void removeColor(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTagElement("display");
        if (isColorable() && compoundnbt != null && compoundnbt.contains("color")) {
            compoundnbt.remove("color");
        }
    }

    default void setColor(ItemStack stack, int color) {
        if(isColorable() && color >= 0) {
            IColorableItem.saveColorToTag(stack.getOrCreateTag(), color);
        }
    }

    public static void saveColorToTag(CompoundTag tag, int color) {
        if(color >= 0) {
            CompoundTag colorT = new CompoundTag();
            colorT.putInt("color", color);
            tag.put("display", colorT);
        }
    }
    
    @SuppressWarnings("null")
    default Boolean washItem(UseOnContext context) {
        if(!context.getPlayer().isCrouching() && ((IColorableItem)context.getItemInHand().getItem()).hasColor(context.getItemInHand()) && context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof LayeredCauldronBlock) {
            if(context.getPlayer() instanceof ServerPlayer) {
                ((IColorableItem)context.getItemInHand().getItem()).clearColor(context.getItemInHand());
                LayeredCauldronBlock.lowerFillLevel(context.getLevel().getBlockState(context.getClickedPos()), context.getLevel(), context.getClickedPos());
            }
            return true;
        }

        return false;
    }

    static Boolean isDyeableInstrument(ItemStack stack) {
        return (stack.getItem() instanceof IColorableItem) && ((IColorableItem)stack.getItem()).isColorable();
    }

    static ItemStack dyeItem(ItemStack stack, List<DyeItem> dyes) {
        return DyeableLeatherItem.dyeArmor(stack, dyes);
    }
}
