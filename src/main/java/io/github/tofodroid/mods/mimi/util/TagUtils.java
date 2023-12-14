package io.github.tofodroid.mods.mimi.util;

import java.util.UUID;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public abstract class TagUtils {
    public static UUID getUUIDOrDefault(ItemStack stack, String tag, UUID defaultVal) {
        try {
            return NbtUtils.loadUUID(getTagOrExcept(stack, tag));
        } catch(Exception e){}
        return defaultVal;
    }

    public static Integer getIntOrDefault(ItemStack stack, String tag, Integer defaultVal) {
        try {
            return ((NumericTag)getTagOrExcept(stack, tag)).getAsInt();
        } catch(Exception e){}
        return defaultVal;
    }

    public static Byte getByteOrDefault(ItemStack stack, String tag, Byte defaultVal) {
        try {
            return ((NumericTag)getTagOrExcept(stack, tag)).getAsByte();
        } catch(Exception e){}
        return defaultVal;
    }

    public static Boolean getBooleanOrDefault(ItemStack stack, String tag, Boolean defaultVal) {
        try {
            return ((NumericTag)getTagOrExcept(stack, tag)).getAsByte() != 0;
        } catch(Exception e){}
        return defaultVal;
    }

    public static String getStringOrDefault(ItemStack stack, String tag, String defaultVal) {
        try {
            return getTagOrExcept(stack, tag).getAsString();
        } catch(Exception e){}
        return defaultVal;
    }

    protected static Tag getTagOrExcept(ItemStack stack, String tag) throws NoStackTraceException {
        if(stack != null) {
            Tag nbt = stack.getOrCreateTag().get(tag);
            if(nbt != null) return nbt;
        }
        throw new NoStackTraceException("Tag not found");
    }
    
    public static Byte getByteOrDefault(ItemStack stack, String tag, Integer defaultVal) {
        return getByteOrDefault(stack, tag, defaultVal.byteValue());
    }

    public static Integer getIntOrDefault(ItemStack stack, String tag, Byte defaultVal) {
        return getIntOrDefault(stack, tag, defaultVal.intValue());
    }
}
