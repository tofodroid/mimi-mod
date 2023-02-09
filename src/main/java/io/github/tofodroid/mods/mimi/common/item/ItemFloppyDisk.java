package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemFloppyDisk extends Item implements IDyeableItem {
    public static final String REGISTRY_NAME = "floppydisk";
    
    public static final String MIDI_URL_TAG = "midi_url";
    public static final String DISK_TITLE_TAG = "disk_title";
    public static final String DISK_AUTHOR_TAG = "disk_author";

    public ItemFloppyDisk() {
        super(new Properties().stacksTo(64));
    }
    
    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        return InteractionResultHolder.pass(playerIn.getItemInHand(handIn));
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
       return ItemFloppyDisk.isWritten(stack);
    }
    
    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext context) {
        if(washItem(context)) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        // Client-side only
        if(worldIn != null && worldIn.isClientSide && ItemFloppyDisk.getDiskAuthor(stack) != null) {
            
            tooltip.add(Component.literal("----------------"));

            // Disk Title
            tooltip.add(Component.literal("Disk Title: " + ItemFloppyDisk.getDiskTitle(stack)));

            // Disk Author
            tooltip.add(Component.literal("Author: " + ItemFloppyDisk.getDiskAuthor(stack)));

            // MIDI URL
            tooltip.add(Component.literal("MIDI URL: " + ItemFloppyDisk.getMidiUrl(stack)));
        }
    }
    
    public static void setMidiUrl(ItemStack stack, String url) {
        if (url != null && !url.isBlank()) {
            stack.getOrCreateTag().putString(MIDI_URL_TAG, url);
        } else if (stack.hasTag()) {
            stack.getTag().remove(MIDI_URL_TAG);
        }
    }

    public static String getMidiUrl(ItemStack stack) {
        if (stackTagContainsKey(stack, MIDI_URL_TAG)) {
            return stack.getTag().getString(MIDI_URL_TAG);
        }

        return null;
    }
    
    public static void setDiskTitle(ItemStack stack, String title) {
        if (title != null && !title.isBlank()) {
            stack.getOrCreateTag().putString(DISK_TITLE_TAG, title);
        } else if (stack.hasTag()) {
            stack.getTag().remove(DISK_TITLE_TAG);
        }
    }

    public static String getDiskTitle(ItemStack stack) {
        if (stackTagContainsKey(stack, DISK_TITLE_TAG)) {
            return stack.getTag().getString(DISK_TITLE_TAG);
        }

        return null;
    }
    
    public static void setDiskAuthor(ItemStack stack, String author) {
        if (author != null && !author.isBlank()) {
            stack.getOrCreateTag().putString(DISK_AUTHOR_TAG, author);
        } else if (stack.hasTag()) {
            stack.getTag().remove(DISK_AUTHOR_TAG);
        }
    }

    public static String getDiskAuthor(ItemStack stack) {
        if (stackTagContainsKey(stack, DISK_AUTHOR_TAG)) {
            return stack.getTag().getString(DISK_AUTHOR_TAG);
        }

        return null;
    }

    public static Boolean isWritten(ItemStack stack) {
        return ItemFloppyDisk.getDiskAuthor(stack) != null;
    }

    protected static Boolean stackTagContainsKey(ItemStack stack, String tag) {
        return stack != null && stack.getTag() != null && stack.getTag().contains(tag);
    }

    @Override
    public Boolean isDyeable() {
        return true;
    }

    @Override
    public Integer getDefaultColor() {
        return 1183244;
    }
}
