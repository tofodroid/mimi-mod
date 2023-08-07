package io.github.tofodroid.mods.mimi.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class GeneralUtils {
    public static ItemStack createItemStack(Item item, Integer count, CompoundTag tag) {
        ItemStack newStack = new ItemStack(item, count);
        newStack.setTag(tag.copy());
        return newStack;
    }
}
