package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ItemTransmitter extends Item {
    public static final String ENABLED_TAG = "enabled";

    public ItemTransmitter() {
        super(new Properties().group(ModItems.ITEM_GROUP).maxStackSize(1));
        this.setRegistryName("transmitter");
    }
    
    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        final ItemStack heldItem = playerIn.getHeldItem(handIn);

        if(!worldIn.isRemote() && playerIn.isSneaking()) {
            if(!heldItem.getOrCreateTag().contains(ENABLED_TAG)) {
                heldItem.getOrCreateTag().putBoolean(ENABLED_TAG, true);
                playerIn.sendStatusMessage(new StringTextComponent("Transmitter Enabled"), true);
            } else {
                heldItem.getOrCreateTag().remove(ENABLED_TAG);
                playerIn.sendStatusMessage(new StringTextComponent("Transmitter Disabled"), true);
            }
            return new ActionResult<>(ActionResultType.SUCCESS, heldItem);
        }
        return new ActionResult<>(ActionResultType.PASS, heldItem);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if(stack.getOrCreateTag().contains(ENABLED_TAG)) {
            tooltip.add(new StringTextComponent("Enabled"));
        } else {
            tooltip.add(new StringTextComponent("Disabled"));
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return stack.getOrCreateTag().contains(ENABLED_TAG); 
    }
}
