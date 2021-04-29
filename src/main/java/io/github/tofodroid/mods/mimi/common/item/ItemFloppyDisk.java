package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public class ItemFloppyDisk extends Item {
    public static final String TITLE_TAG = "title";
    public static final String URL_TAG = "url";
    public static final String AUTHOR_TAG = "author";

    public ItemFloppyDisk() {
        super(new Properties()
            .group(ModItems.ITEM_GROUP)
            .maxStackSize(16)
        );
        this.setRegistryName("disk");
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return stack.getOrCreateTag().contains(TITLE_TAG); 
    }
    
    public ItemStack writeDiskData(ItemStack diskStack, String title, String url, String playerName) {
        if(diskStack != null && this == diskStack.getItem()) {
            diskStack.getOrCreateTag().putString(TITLE_TAG, title);
            diskStack.getOrCreateTag().putString(URL_TAG, url);
            diskStack.getOrCreateTag().putString(AUTHOR_TAG, playerName);
            diskStack.setDisplayName(new StringTextComponent(title));
        }

        return diskStack;
    }
    
    public boolean isEmptyDisk(ItemStack stack) {
        return stack != null && !stack.getOrCreateTag().contains(TITLE_TAG) && !stack.getOrCreateTag().contains(URL_TAG) && !stack.getOrCreateTag().contains(AUTHOR_TAG);
    }
}
