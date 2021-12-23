package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

// Clone from net.minecraft.item.IDyeableArmorItem
public interface IDyeableInstrumentItem extends DyeableLeatherItem {
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

    static Boolean isDyeableInstrument(ItemStack stack) {
        return (stack.getItem() instanceof IDyeableInstrumentItem) && ((IDyeableInstrumentItem)stack.getItem()).isDyeable();
    }

    static ItemStack dyeItem(ItemStack stack, List<DyeItem> dyes) {
        return DyeableLeatherItem.dyeArmor(stack, dyes);
    }
}
