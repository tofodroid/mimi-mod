package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;

import net.minecraft.item.DyeItem;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

// Clone from net.minecraft.item.IDyeableArmorItem
public interface IDyeableInstrumentItem extends IDyeableArmorItem {
    public static final Integer DEFAULT_WHITE_COLOR = -1;

    public Boolean isDyeable();
    public Integer getDefaultColor();

    default boolean hasColor(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getChildTag("display");
        return isDyeable() && compoundnbt != null && compoundnbt.contains("color", 99);
    }

    default int getColor(ItemStack stack) {
        if(!isDyeable()) {
            return -1;
        }

        CompoundNBT compoundnbt = stack.getChildTag("display");
        return compoundnbt != null && compoundnbt.contains("color", 99) ? compoundnbt.getInt("color") : getDefaultColor();
    }

    default void removeColor(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getChildTag("display");
        if (isDyeable() && compoundnbt != null && compoundnbt.contains("color")) {
            compoundnbt.remove("color");
        }
    }

    default void setColor(ItemStack stack, int color) {
        if(isDyeable() && color >= 0) {
            stack.getOrCreateChildTag("display").putInt("color", color);
        }
    }

    static Boolean isDyeableInstrument(ItemStack stack) {
        return (stack.getItem() instanceof IDyeableInstrumentItem) && ((IDyeableInstrumentItem)stack.getItem()).isDyeable();
    }

    static ItemStack dyeItem(ItemStack stack, List<DyeItem> dyes) {
        return IDyeableArmorItem.dyeItem(stack, dyes);
    }
}
