package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemDiskDrive extends Item {
    public ItemDiskDrive() {
        super(new Properties()
            .group(ModItems.ITEM_GROUP)
            .maxStackSize(1)
        );
        this.setRegistryName("drive");
    }

    public boolean isEmptyDrive(ItemStack stack) {
        return true;
    }

    public ItemStack insertDisk(ItemStack diskStack, ItemStack driveStack) {
        ItemStack result = new ItemStack(this, 1);
        result.getOrCreateTag().putString(ItemFloppyDisk.TITLE_TAG, diskStack.getOrCreateTag().getString(ItemFloppyDisk.TITLE_TAG));
        result.getOrCreateTag().putString(ItemFloppyDisk.URL_TAG, diskStack.getOrCreateTag().getString(ItemFloppyDisk.URL_TAG));
        result.getOrCreateTag().putString(ItemFloppyDisk.AUTHOR_TAG, diskStack.getOrCreateTag().getString(ItemFloppyDisk.AUTHOR_TAG));
        return result;
    }

    public ItemStack ejectDisk(ItemStack driveStack) {
        // Validate
        if(driveStack == null || isEmptyDrive(driveStack)) {
            return null;
        }

        /*
        ItemStack result = new ItemStack(ModItems.DISK, 1);
        return ModItems.DISK.writeDiskData(
            result,
            driveStack.getOrCreateTag().getString(ItemFloppyDisk.TITLE_TAG), 
            driveStack.getOrCreateTag().getString(ItemFloppyDisk.URL_TAG), 
            driveStack.getOrCreateTag().getString(ItemFloppyDisk.AUTHOR_TAG)
        );
        */
        return null;
    }
}
